package cinema.backend.service;

import cinema.backend.dto.program.ProgramSearchRequest;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;

import java.util.List;

public interface ProgramService {

    Program createProgram(Program program);


    List<Program> getAllPrograms();

    Program getProgramById(Long id);

    Program updateProgram(Long id, Program updated);

    void deleteProgram(Long id);

    Program addProgrammer(Long programId, String username);

    Program addStaff(Long programId, String username);

    Program changeProgramState(Long programId, ProgramState newState);


    List<Program> searchPrograms(ProgramSearchRequest criteria);
}
