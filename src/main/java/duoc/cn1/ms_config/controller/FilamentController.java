package duoc.cn1.ms_config.controller;

import duoc.cn1.ms_config.dto.request.FilamentCreateRequest;
import duoc.cn1.ms_config.dto.request.FilamentStatusUpdateRequest;
import duoc.cn1.ms_config.dto.request.FilamentUpdateRequest;
import duoc.cn1.ms_config.dto.response.FilamentResponse;
import duoc.cn1.ms_config.service.FilamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config/filaments")
@RequiredArgsConstructor
@Tag(name = "Filamentos", description = "API para gestión de filamentos de impresión 3D")
public class FilamentController {

	private final FilamentService filamentService;

	@GetMapping
	@Operation(summary = "Obtener todos los filamentos", description = "Retorna una lista con todos los filamentos registrados")
	public ResponseEntity<List<FilamentResponse>> getAllFilaments() {
		return ResponseEntity.ok(filamentService.getAllFilaments());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener filamento por ID", description = "Retorna los detalles de un filamento específico")
	public ResponseEntity<FilamentResponse> getFilamentById(@PathVariable Long id) {
		return ResponseEntity.ok(filamentService.getFilamentById(id));
	}

	@PostMapping
	@Operation(summary = "Crear nuevo filamento", description = "Crea un nuevo filamento con los datos proporcionados")
	public ResponseEntity<FilamentResponse> createFilament(@Valid @RequestBody FilamentCreateRequest request) {
		FilamentResponse response = filamentService.createFilament(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar filamento", description = "Actualiza los datos de un filamento existente")
	public ResponseEntity<FilamentResponse> updateFilament(
			@PathVariable Long id,
			@Valid @RequestBody FilamentUpdateRequest request) {
		return ResponseEntity.ok(filamentService.updateFilament(id, request));
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Actualizar estado de filamento", description = "Actualiza el estado (ACTIVE/INACTIVE) de un filamento")
	public ResponseEntity<FilamentResponse> updateFilamentStatus(
			@PathVariable Long id,
			@Valid @RequestBody FilamentStatusUpdateRequest request) {
		return ResponseEntity.ok(filamentService.updateFilamentStatus(id, request));
	}
}
