package duoc.cn1.ms_config.service;

import duoc.cn1.ms_config.event.FilamentConsumptionEvent;
import duoc.cn1.ms_config.model.ProcessedEvent;
import duoc.cn1.ms_config.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orquesta el consumo de inventario de filamento de forma atómica:
 * descuenta todos los filamentos del evento y registra el processed_event
 * en una sola transacción. Si algo falla, no se descuenta nada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

	private final FilamentService filamentService;
	private final ProcessedEventRepository processedEventRepository;

	@Transactional
	public boolean processFilamentConsumption(FilamentConsumptionEvent event) {
		Long orderId = event.getOrderId();

		if (processedEventRepository.existsByOrderIdAndEventType(
				orderId, FilamentConsumptionEvent.EVENT_TYPE)) {
			log.info("Evento FILAMENT_CONSUMPTION ya procesado para orderId={}. Se omite.",
				orderId);
			return false;
		}

		List<FilamentConsumptionEvent.FilamentConsumption> filaments =
			event.getFilaments();

		for (FilamentConsumptionEvent.FilamentConsumption filament : filaments) {
			filamentService.consumeStock(
				filament.getFilamentId(),
				filament.getGrams()
			);
		}

		processedEventRepository.save(
			new ProcessedEvent(orderId, FilamentConsumptionEvent.EVENT_TYPE)
		);

		log.info("Consumo de filamento aplicado para orderId={} ({} filamentos)",
			orderId, filaments.size());

		return true;
	}
}
