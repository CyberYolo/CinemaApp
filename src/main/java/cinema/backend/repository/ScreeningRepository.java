package cinema.backend.repository;

import cinema.backend.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByProgramId(Long programId);


    boolean existsByProgramIdAndHandlerId(Long programId, Long handlerId);
}
