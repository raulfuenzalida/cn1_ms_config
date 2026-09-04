package duoc.cn1.ms_config.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FilamentUpdateRequest {

	@NotBlank(message = "El nombre del filamento es obligatorio")
	private String name;

	@NotBlank(message = "El color del filamento es obligatorio")
	private String color;

	@DecimalMin(value = "0.01", message = "El precio por kilogramo debe ser mayor que cero")
	private BigDecimal pricePerKg;
}
