package duoc.cn1.ms_config.repository;

import duoc.cn1.ms_config.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

	boolean existsByOrderIdAndEventType(Long orderId, String eventType);
}
