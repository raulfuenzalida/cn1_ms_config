package duoc.cn1.ms_config.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilamentConsumptionEvent {

	public static final String EVENT_TYPE = "FILAMENT_CONSUMPTION";

	private String eventType;
	private Long orderId;
	private String orderNumber;
	private List<FilamentConsumption> filaments;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FilamentConsumption {
		private Long filamentId;
		private BigDecimal grams;
	}
}
