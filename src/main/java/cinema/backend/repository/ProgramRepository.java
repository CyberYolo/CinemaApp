package cinema.backend.repository;

import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByName(String name);

    List<Program> findByState(ProgramState state);

    @Query("select p from Program p where p.creator.id = :userId")
    List<Program> findProgramsCreatedBy(@Param("userId") Long userId);

    @Query("select distinct p from Program p join p.programmers u where u.id = :userId")
    List<Program> findProgramsWhereUserIsProgrammer(@Param("userId") Long userId);

    @Query("select distinct p from Program p join p.staff u where u.id = :userId")
    List<Program> findProgramsWhereUserIsStaff(@Param("userId") Long userId);


    @Query("select distinct p from Program p join p.screenings s join s.handler h where h.id = :userId")
    List<Program> findProgramsWhereUserIsHandler(@Param("userId") Long userId);


    @Query("select p from Program p where p.programmers is empty")
    List<Program> findProgramsWithNoProgrammers();
}
