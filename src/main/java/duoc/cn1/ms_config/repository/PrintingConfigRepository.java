package duoc.cn1.ms_config.repository;

import duoc.cn1.ms_config.model.PrintingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrintingConfigRepository extends JpaRepository<PrintingConfig, Long> {

	Optional<PrintingConfig> findFirstByOrderByIdAsc();
}
