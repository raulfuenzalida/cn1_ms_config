package duoc.cn1.ms_config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configuración de Kafka para el consumidor de inventario.
 *
 * - kafkaListenerContainerFactory: errores transitorios reintentan con
 *   backoff (2s x 3 intentos) y luego DeadLetterPublishingRecoverer envía
 *   el registro al topic <topic>.DLT.
 * - dltListenerContainerFactory: factory simple para el auditor del DLT
 *   (sin error handler: si falla el log, el offset no avanza y reintenta).
 */
@Configuration
public class KafkaConfig {

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory,
			KafkaTemplate<String, String> kafkaTemplate) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		DeadLetterPublishingRecoverer recoverer =
			new DeadLetterPublishingRecoverer(kafkaTemplate);
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
			recoverer, new FixedBackOff(2000L, 3L));
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> dltListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
}
