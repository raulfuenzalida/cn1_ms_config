package duoc.cn1.ms_config.dto.request;

import duoc.cn1.ms_config.model.FilamentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FilamentCreateRequest {

	@NotBlank(message = "El nombre del filamento es obligatorio")
	private String name;

	@NotBlank(message = "El color del filamento es obligatorio")
	private String color;

	@NotNull(message = "El precio por kilogramo es obligatorio")
	@DecimalMin(value = "0.01", message = "El precio por kilogramo debe ser mayor que cero")
	private BigDecimal pricePerKg;

	@NotNull(message = "El estado es obligatorio")
	private FilamentStatus status;
}
