package duoc.cn1.ms_config.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "filaments", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"name", "color"})
})
public class Filament {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_filament")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "color", nullable = false)
	private String color;

	@Column(name = "price_per_kg", nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerKg;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private FilamentStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Filament() {
	}

	public Filament(String name, String color, BigDecimal pricePerKg, FilamentStatus status) {
		this.name = name;
		this.color = color;
		this.pricePerKg = pricePerKg;
		this.status = status;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public BigDecimal getPricePerKg() {
		return pricePerKg;
	}

	public FilamentStatus getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setPricePerKg(BigDecimal pricePerKg) {
		this.pricePerKg = pricePerKg;
	}

	public void setStatus(FilamentStatus status) {
		this.status = status;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
