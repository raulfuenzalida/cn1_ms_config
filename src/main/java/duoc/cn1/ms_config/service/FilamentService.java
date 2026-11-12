package duoc.cn1.ms_config.service;

import duoc.cn1.ms_config.client.ProductServiceClient;
import duoc.cn1.ms_config.dto.request.FilamentCreateRequest;
import duoc.cn1.ms_config.dto.request.FilamentStatusUpdateRequest;
import duoc.cn1.ms_config.dto.request.FilamentUpdateRequest;
import duoc.cn1.ms_config.dto.response.FilamentResponse;
import duoc.cn1.ms_config.exception.FilamentDuplicateException;
import duoc.cn1.ms_config.exception.FilamentNotFoundException;
import duoc.cn1.ms_config.exception.InsufficientStockException;
import duoc.cn1.ms_config.model.Filament;
import duoc.cn1.ms_config.repository.FilamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FilamentService {

	private final FilamentRepository filamentRepository;
	private final ProductServiceClient productServiceClient;

	public List<FilamentResponse> getAllFilaments() {
		return filamentRepository.findAll().stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	public FilamentResponse getFilamentById(Long id) {
		Filament filament = filamentRepository.findById(id)
			.orElseThrow(() -> new FilamentNotFoundException(id));

		return mapToResponse(filament);
	}

	public FilamentResponse createFilament(FilamentCreateRequest request) {
		if (filamentRepository.existsByNameAndColor(
				request.getName(),
				request.getColor())) {

			throw new FilamentDuplicateException(
				request.getName(),
				request.getColor()
			);
		}

		Filament filament = new Filament(
			request.getName(),
			request.getColor(),
			request.getPricePerKg(),
			request.getStatus()
		);

		filament.setStockGrams(request.getStockGrams());

		Filament savedFilament =
			filamentRepository.save(filament);

		return mapToResponse(savedFilament);
	}

	public FilamentResponse updateFilament(
			Long id,
			FilamentUpdateRequest request) {

		Filament filament = filamentRepository.findById(id)
			.orElseThrow(() -> new FilamentNotFoundException(id));

		if (
			!filament.getName().equals(request.getName()) ||
			!filament.getColor().equals(request.getColor())
		) {
			if (
				filamentRepository.existsByNameAndColor(
					request.getName(),
					request.getColor()
				)
			) {
				throw new FilamentDuplicateException(
					request.getName(),
					request.getColor()
				);
			}
		}

		boolean priceChanged = false;

		if (request.getPricePerKg() != null) {
			BigDecimal currentPrice =
				filament.getPricePerKg();

			BigDecimal newPrice =
				request.getPricePerKg();

			priceChanged =
				currentPrice == null ||
				currentPrice.compareTo(newPrice) != 0;
		}

		filament.setName(request.getName());
		filament.setColor(request.getColor());

		if (request.getPricePerKg() != null) {
			filament.setPricePerKg(
				request.getPricePerKg()
			);
		}

		if (request.getStockGrams() != null) {
			filament.setStockGrams(
				request.getStockGrams()
			);
		}

		Filament updatedFilament =
			filamentRepository.save(filament);

		if (priceChanged) {
			productServiceClient
				.invalidateProductsByFilament(id);
		}

		return mapToResponse(updatedFilament);
	}

	public FilamentResponse updateFilamentStatus(
			Long id,
			FilamentStatusUpdateRequest request) {

		Filament filament = filamentRepository.findById(id)
			.orElseThrow(() -> new FilamentNotFoundException(id));

		filament.setStatus(request.getStatus());

		Filament updatedFilament =
			filamentRepository.save(filament);

		return mapToResponse(updatedFilament);
	}

	/**
	 * Descuenta gramos de stock del filamento indicado.
	 * Usado por el consumidor de eventos de consumo de filamento.
	 *
	 * @throws FilamentNotFoundException si el filamento no existe
	 * @throws InsufficientStockException si el stock es nulo, insuficiente o negativo
	 */
	public void consumeStock(Long filamentId, BigDecimal grams) {
		Filament filament = filamentRepository.findById(filamentId)
			.orElseThrow(() -> new FilamentNotFoundException(filamentId));

		BigDecimal stock = filament.getStockGrams();

		if (stock == null || grams == null || grams.signum() <= 0) {
			throw new InsufficientStockException(
				filamentId,
				grams,
				stock
			);
		}

		if (stock.compareTo(grams) < 0) {
			throw new InsufficientStockException(
				filamentId,
				grams,
				stock
			);
		}

		filament.setStockGrams(stock.subtract(grams));
		filamentRepository.save(filament);
	}

	private FilamentResponse mapToResponse(Filament filament) {
		return new FilamentResponse(
			filament.getId(),
			filament.getName(),
			filament.getColor(),
			filament.getPricePerKg(),
			filament.getStockGrams(),
			filament.getStatus(),
			filament.getCreatedAt(),
			filament.getUpdatedAt()
		);
	}
}