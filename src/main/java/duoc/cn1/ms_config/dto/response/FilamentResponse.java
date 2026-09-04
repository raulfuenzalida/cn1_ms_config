package duoc.cn1.ms_config.dto.response;

import duoc.cn1.ms_config.model.FilamentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilamentResponse {

	private Long id;
	private String name;
	private String color;
	private BigDecimal pricePerKg;
	private FilamentStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
