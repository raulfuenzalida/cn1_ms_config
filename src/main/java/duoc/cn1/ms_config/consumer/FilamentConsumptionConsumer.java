package duoc.cn1.ms_config.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import duoc.cn1.ms_config.event.FilamentConsumptionEvent;
import duoc.cn1.ms_config.exception.FilamentNotFoundException;
import duoc.cn1.ms_config.exception.InsufficientStockException;
import duoc.cn1.ms_config.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Consumidor del topic printworks.filament-consumption (consumer group
 * ms-config-filament-inventory).
 *
 * Manejo de errores (guía 3.3.3):
 *  - payload inválido / evento incompleto → error permanente: se envía
 *    al DLT manualmente y el offset avanza.
 *  - filamento inexistente / stock insuficiente → error permanente: DLT.
 *  - orderId ya procesado → idempotencia, se omite.
 *  - cualquier otra excepción → se relanza: DefaultErrorHandler reintenta
 *    con backoff y luego DeadLetterPublishingRecoverer envía al .DLT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FilamentConsumptionConsumer {

	private final InventoryService inventoryService;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${kafka.topic.filament-consumption}")
	private String topic;

	@Value("${kafka.topic.filament-consumption-dlt}")
	private String dltTopic;

	@KafkaListener(
		topics = "${kafka.topic.filament-consumption}",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void consume(ConsumerRecord<String, String> record) {
		String key = record.key();
		String value = record.value();
		long offset = record.offset();

		FilamentConsumptionEvent event;
		try {
			event = objectMapper.readValue(value, FilamentConsumptionEvent.class);
		} catch (Exception e) {
			log.error("[VALIDA] JSON inválido en topic={} partition={} offset={} key={}: {}",
				topic, record.partition(), offset, key, e.getMessage());
			sendToDlt(record, "INVALID_JSON");
			return;
		}

		if (!isValid(event)) {
			log.error("[VALIDA] Evento inválido orderId={} -> DLT", event.getOrderId());
			sendToDlt(record, "INVALID_EVENT");
			return;
		}

		try {
			inventoryService.processFilamentConsumption(event);
		} catch (InsufficientStockException | FilamentNotFoundException e) {
			log.error("[STOCK] {} orderId={} -> DLT", e.getMessage(), event.getOrderId());
			sendToDlt(record, e.getClass().getSimpleName());
		} catch (DataIntegrityViolationException e) {
			log.warn("[IDEMP] orderId={} ya registrado (constraint) - se omite", event.getOrderId());
		}
		// Cualquier otra excepción sube al ErrorHandler: retry con backoff
		// y luego DeadLetterPublishingRecoverer envía al .DLT.
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

	private void sendToDlt(ConsumerRecord<String, String> original, String reason) {
		try {
			ProducerRecord<String, String> dltRecord = new ProducerRecord<>(
				dltTopic, null, original.key(), original.value());
			dltRecord.headers().add(new RecordHeader(
				"dltReason", reason.getBytes(StandardCharsets.UTF_8)));
			dltRecord.headers().add(new RecordHeader(
				"originalTopic",
				original.topic().getBytes(StandardCharsets.UTF_8)));
			dltRecord.headers().add(new RecordHeader(
				"originalPartition",
				String.valueOf(original.partition()).getBytes(StandardCharsets.UTF_8)));
			dltRecord.headers().add(new RecordHeader(
				"originalOffset",
				String.valueOf(original.offset()).getBytes(StandardCharsets.UTF_8)));

			kafkaTemplate.send(dltRecord);
			log.warn("[DLT] Registro enviado a {} (motivo={})", dltTopic, reason);
		} catch (Exception e) {
			log.error("Error enviando registro a DLT {}: {}", dltTopic, e.getMessage());
			throw e;
		}
	}
}
