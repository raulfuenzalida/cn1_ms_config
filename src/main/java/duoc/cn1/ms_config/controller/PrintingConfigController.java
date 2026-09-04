package duoc.cn1.ms_config.controller;

import duoc.cn1.ms_config.dto.request.PrintingConfigUpdateRequest;
import duoc.cn1.ms_config.dto.response.PrintingConfigResponse;
import duoc.cn1.ms_config.service.PrintingConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/config/printing")
@RequiredArgsConstructor
@Tag(name = "Configuración de Impresión", description = "API para gestión de configuración energética de impresión")
public class PrintingConfigController {

	private final PrintingConfigService printingConfigService;

	@GetMapping
	@Operation(summary = "Obtener configuración de impresión", description = "Retorna la configuración actual de costos energéticos")
	public ResponseEntity<PrintingConfigResponse> getPrintingConfig() {
		return ResponseEntity.ok(printingConfigService.getPrintingConfig());
	}

	@PutMapping
	@Operation(summary = "Actualizar configuración de impresión", description = "Actualiza los costos energéticos de impresión")
	public ResponseEntity<PrintingConfigResponse> updatePrintingConfig(
			@Valid @RequestBody PrintingConfigUpdateRequest request) {
		return ResponseEntity.ok(printingConfigService.updatePrintingConfig(request));
	}
}
