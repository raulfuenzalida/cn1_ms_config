package duoc.cn1.ms_config.service;

import duoc.cn1.ms_config.client.ProductServiceClient;
import duoc.cn1.ms_config.dto.request.PrintingConfigUpdateRequest;
import duoc.cn1.ms_config.dto.response.PrintingConfigResponse;
import duoc.cn1.ms_config.exception.PrintingConfigNotFoundException;
import duoc.cn1.ms_config.model.PrintingConfig;
import duoc.cn1.ms_config.repository.PrintingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PrintingConfigService {

	private final PrintingConfigRepository printingConfigRepository;
	private final ProductServiceClient productServiceClient;

	public PrintingConfigResponse getPrintingConfig() {
		PrintingConfig config = printingConfigRepository
			.findFirstByOrderByIdAsc()
			.orElseThrow(PrintingConfigNotFoundException::new);

		return mapToResponse(config);
	}

	public PrintingConfigResponse updatePrintingConfig(
			PrintingConfigUpdateRequest request) {

		PrintingConfig config = printingConfigRepository
			.findFirstByOrderByIdAsc()
			.orElseThrow(PrintingConfigNotFoundException::new);

		BigDecimal currentElectricityPrice =
			config.getElectricityPriceKwh();

		BigDecimal newElectricityPrice =
			request.getElectricityPriceKwh();

		BigDecimal currentPrinterConsumption =
			config.getPrinterConsumptionKwh();

		BigDecimal newPrinterConsumption =
			request.getPrinterConsumptionKwh();

		boolean electricityPriceChanged =
			hasChanged(
				currentElectricityPrice,
				newElectricityPrice
			);

		boolean printerConsumptionChanged =
			hasChanged(
				currentPrinterConsumption,
				newPrinterConsumption
			);

		config.setElectricityPriceKwh(
			newElectricityPrice
		);

		config.setPrinterConsumptionKwh(
			newPrinterConsumption
		);

		PrintingConfig updatedConfig =
			printingConfigRepository.save(config);

		if (
			electricityPriceChanged ||
			printerConsumptionChanged
		) {
			productServiceClient
				.invalidateProductsByPrinting();
		}

		return mapToResponse(updatedConfig);
	}

	private boolean hasChanged(
			BigDecimal currentValue,
			BigDecimal newValue) {

		if (currentValue == null && newValue == null) {
			return false;
		}

		if (currentValue == null || newValue == null) {
			return true;
		}

		return currentValue.compareTo(newValue) != 0;
	}

	private PrintingConfigResponse mapToResponse(
			PrintingConfig config) {

		return new PrintingConfigResponse(
			config.getId(),
			config.getElectricityPriceKwh(),
			config.getPrinterConsumptionKwh(),
			config.getCreatedAt(),
			config.getUpdatedAt()
		);
	}
}