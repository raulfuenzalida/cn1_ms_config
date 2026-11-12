package duoc.cn1.ms_config.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"order_id", "event_type"})
})
public class ProcessedEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_processed_event")
	private Long id;

	@Column(name = "order_id", nullable = false)
	private Long orderId;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Column(name = "processed_at", nullable = false)
	private LocalDateTime processedAt;

	public ProcessedEvent() {
	}

	public ProcessedEvent(Long orderId, String eventType) {
		this.orderId = orderId;
		this.eventType = eventType;
		this.processedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getEventType() {
		return eventType;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}
}
