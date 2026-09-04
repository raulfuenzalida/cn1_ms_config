package duoc.cn1.ms_config.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "printing_config")
public class PrintingConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_printing_config")
	private Long id;

	@Column(name = "electricity_price_kwh", nullable = false, precision = 10, scale = 2)
	private BigDecimal electricityPriceKwh;

	@Column(name = "printer_consumption_kwh", nullable = false, precision = 10, scale = 2)
	private BigDecimal printerConsumptionKwh;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public PrintingConfig() {
	}

	public PrintingConfig(BigDecimal electricityPriceKwh, BigDecimal printerConsumptionKwh) {
		this.electricityPriceKwh = electricityPriceKwh;
		this.printerConsumptionKwh = printerConsumptionKwh;
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

	public BigDecimal getElectricityPriceKwh() {
		return electricityPriceKwh;
	}

	public BigDecimal getPrinterConsumptionKwh() {
		return printerConsumptionKwh;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setElectricityPriceKwh(BigDecimal electricityPriceKwh) {
		this.electricityPriceKwh = electricityPriceKwh;
	}

	public void setPrinterConsumptionKwh(BigDecimal printerConsumptionKwh) {
		this.printerConsumptionKwh = printerConsumptionKwh;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
