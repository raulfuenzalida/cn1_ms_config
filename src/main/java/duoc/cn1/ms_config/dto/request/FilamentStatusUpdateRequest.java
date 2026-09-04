package duoc.cn1.ms_config.dto.request;

import duoc.cn1.ms_config.model.FilamentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FilamentStatusUpdateRequest {

	@NotNull(message = "El estado es obligatorio")
	private FilamentStatus status;
}
