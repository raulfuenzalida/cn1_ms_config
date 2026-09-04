package duoc.cn1.ms_config.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrintingConfigUpdateRequest {

	@NotNull(message = "El precio de electricidad por kWh es obligatorio")
	@DecimalMin(value = "0.01", message = "El precio de electricidad por kWh debe ser mayor que cero")
	private BigDecimal electricityPriceKwh;

	@NotNull(message = "El consumo de la impresora en kWh es obligatorio")
	@DecimalMin(value = "0.01", message = "El consumo de la impresora en kWh debe ser mayor que cero")
	private BigDecimal printerConsumptionKwh;
}
