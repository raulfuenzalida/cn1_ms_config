package duoc.cn1.ms_config.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import duoc.cn1.ms_config.event.FilamentConsumptionEvent;
import duoc.cn1.ms_config.exception.FilamentNotFoundException;
import duoc.cn1.ms_config.exception.InsufficientStockException;
import duoc.cn1.ms_config.kinesis.KinesisStreamConsumer;
import duoc.cn1.ms_config.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.Record;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Consumidor del stream printworks.filament-consumption.
 *
 * Flujo por registro (patrón de la guía 3.3.3):
 *  - payload inválido / filamento inexistente / stock insuficiente
 *    → error permanente: se envía al dead-letter stream y se avanza.
 *  - error transitorio (Kinesis, BD) → la excepción sube, el iterador
 *    del shard no avanza y el registro se reintenta en el próximo poll.
 *  - orderId ya procesado → idempotencia, se omite.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FilamentConsumptionConsumer {

	private final KinesisStreamConsumer streamConsumer;
	private final InventoryService inventoryService;
	private final KinesisClient kinesisClient;
	private final ObjectMapper objectMapper;

	@Value("${kinesis.stream.filament-consumption}")
	private String streamName;

	@Value("${kinesis.stream.filament-consumption-dls}")
	private String dlsStreamName;

	@Scheduled(
		fixedDelayString = "${kinesis.consumer.poll-interval-ms:2000}",
		initialDelayString = "${kinesis.consumer.initial-delay-ms:5000}"
	)
	public void poll() {
		try {
			streamConsumer.poll(streamName, this::processRecord);
		} catch (Exception e) {
			log.error("Error en polling del stream {}: {}", streamName, e.getMessage());
		}
	}

	private void processRecord(Record record) {
		String partitionKey = record.partitionKey();
		String data = record.data().asString(StandardCharsets.UTF_8);

		FilamentConsumptionEvent event;
		try {
			event = objectMapper.readValue(data, FilamentConsumptionEvent.class);
		} catch (Exception e) {
			log.error("[VALIDA] JSON inválido en {} (pk={}): {}", streamName, partitionKey, e.getMessage());
			sendToDls(record, "INVALID_JSON");
			return;
		}

		if (!isValid(event)) {
			log.error("[VALIDA] Evento inválido orderId={} -> DLS", event.getOrderId());
			sendToDls(record, "INVALID_EVENT");
			return;
		}

		try {
			inventoryService.processFilamentConsumption(event);
		} catch (InsufficientStockException | FilamentNotFoundException e) {
			log.error("[STOCK] {} orderId={} -> DLS", e.getMessage(), event.getOrderId());
			sendToDls(record, e.getClass().getSimpleName());
		} catch (DataIntegrityViolationException e) {
			log.warn("[IDEMP] orderId={} ya registrado (constraint) - se omite", event.getOrderId());
		}
		// Cualquier otra excepción sube al poll(): el iterador no avanza
		// y el registro se reintenta en el próximo ciclo.
	}

	private boolean isValid(FilamentConsumptionEvent event) {
		if (event.getOrderId() == null || event.getFilaments() == null
				|| event.getFilaments().isEmpty()) {
			return false;
		}

		return event.getFilaments().stream().allMatch(f ->
			f.getFilamentId() != null
				&& f.getGrams() != null
				&& f.getGrams().compareTo(BigDecimal.ZERO) > 0
		);
	}

	private void sendToDls(Record original, String reason) {
		try {
			PutRecordRequest request = PutRecordRequest.builder()
				.streamName(dlsStreamName)
				.partitionKey(original.partitionKey())
				.data(SdkBytes.fromString(
					"{\"dlsReason\":\"" + reason + "\",\"originalData\":"
						+ original.data().asString(StandardCharsets.UTF_8) + "}",
					StandardCharsets.UTF_8))
				.build();

			kinesisClient.putRecord(request);
			log.warn("[DLS] Registro enviado a {} (motivo={})", dlsStreamName, reason);
		} catch (Exception e) {
			log.error("Error enviando registro a DLS {}: {}", dlsStreamName, e.getMessage());
		}
	}
}
