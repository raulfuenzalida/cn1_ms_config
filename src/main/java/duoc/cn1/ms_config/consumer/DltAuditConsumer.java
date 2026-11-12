package duoc.cn1.ms_config.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Auditor del dead-letter topic printworks.filament-consumption.DLT.
 * Solo registra en log los eventos que fueron redirigidos, con sus
 * headers de origen (dltReason, originalTopic, originalPartition,
 * originalOffset).
 */
@Slf4j
@Component
public class DltAuditConsumer {

	@KafkaListener(
		topics = "${kafka.topic.filament-consumption-dlt}",
		groupId = "${spring.kafka.consumer.group-id}-dlt-audit",
		containerFactory = "dltListenerContainerFactory"
	)
	public void audit(ConsumerRecord<String, String> record) {
		String reason = headerValue(record, "dltReason");
		String originalTopic = headerValue(record, "originalTopic");
		String originalPartition = headerValue(record, "originalPartition");
		String originalOffset = headerValue(record, "originalOffset");

		log.error("[DLT] Evento muerto recibido - topic={} partition={} offset={} key={}"
				+ " reason={} origen={}:{}:{}",
			record.topic(), record.partition(), record.offset(), record.key(),
			reason, originalTopic, originalPartition, originalOffset);
		log.error("[DLT] payload: {}", record.value());
	}

	private String headerValue(ConsumerRecord<String, String> record, String key) {
		var header = record.headers().lastHeader(key);
		return header != null ? new String(header.value()) : "N/A";
	}
}
