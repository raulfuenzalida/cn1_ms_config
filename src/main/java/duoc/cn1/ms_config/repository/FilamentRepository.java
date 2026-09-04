package duoc.cn1.ms_config.repository;

import duoc.cn1.ms_config.model.Filament;
import duoc.cn1.ms_config.model.FilamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilamentRepository extends JpaRepository<Filament, Long> {

	Optional<Filament> findByNameAndColor(String name, String color);

	boolean existsByNameAndColor(String name, String color);
}
