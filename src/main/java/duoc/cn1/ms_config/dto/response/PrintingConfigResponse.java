package duoc.cn1.ms_config.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrintingConfigResponse {

	private Long id;
	private BigDecimal electricityPriceKwh;
	private BigDecimal printerConsumptionKwh;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
