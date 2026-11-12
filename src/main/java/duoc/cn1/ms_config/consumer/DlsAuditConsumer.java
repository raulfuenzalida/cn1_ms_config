package duoc.cn1.ms_config.consumer;

import duoc.cn1.ms_config.kinesis.KinesisStreamConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.model.Record;

import java.nio.charset.StandardCharsets;

/**
 * Consumidor del dead-letter stream. Solo audita: registra en log
 * los eventos que fueron redirigidos por el consumidor principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlsAuditConsumer {

	private final KinesisStreamConsumer streamConsumer;

	@Value("${kinesis.stream.filament-consumption-dls}")
	private String dlsStreamName;

	@Scheduled(
		fixedDelayString = "${kinesis.consumer.poll-interval-ms:2000}",
		initialDelayString = "${kinesis.consumer.initial-delay-ms:8000}"
	)
	public void poll() {
		try {
			streamConsumer.poll(dlsStreamName, this::auditRecord);
		} catch (Exception e) {
			log.error("Error en polling del DLS {}: {}", dlsStreamName, e.getMessage());
		}
	}

	private void auditRecord(Record record) {
		log.error("[DLS] Evento muerto recibido - partitionKey={} sequence={}",
			record.partitionKey(), record.sequenceNumber());
		log.error("[DLS] payload: {}",
			record.data().asString(StandardCharsets.UTF_8));
	}
}
