package cinema.backend.service.impl;

import cinema.backend.dto.program.ProgramSearchRequest;
import cinema.backend.exception.ForbiddenException;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import cinema.backend.model.Role;
import cinema.backend.model.User;
import cinema.backend.repository.ProgramRepository;
import cinema.backend.repository.UserRepository;
import cinema.backend.service.ProgramService;
import cinema.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    private static final Map<ProgramState, ProgramState> NEXT = Map.of(
            ProgramState.CREATED, ProgramState.SUBMISSION,
            ProgramState.SUBMISSION, ProgramState.ASSIGNMENT,
            ProgramState.ASSIGNMENT, ProgramState.REVIEW,
            ProgramState.REVIEW, ProgramState.SCHEDULING,
            ProgramState.SCHEDULING, ProgramState.FINAL_PUBLICATION,
            ProgramState.FINAL_PUBLICATION, ProgramState.DECISION,
            ProgramState.DECISION, ProgramState.ANNOUNCED
    );

    @Override
    public Program createProgram(Program program) {
        User current = userService.getOrCreateCurrentUser();

        if (current.getRole() == null || current.getRole() == Role.VISITOR) {
            throw new ForbiddenException("Only authenticated users can create programs");
        }


        program.setId(null);


        if (program.getProgrammers() == null) program.setProgrammers(new HashSet<>());
        if (program.getStaff() == null) program.setStaff(new HashSet<>());

        program.setCreator(current);

        if (program.getCreationDate() == null) {
            program.setCreationDate(LocalDateTime.now());
        }
        if (program.getState() == null) {
            program.setState(ProgramState.CREATED);
        }


        List<User> programmers = userRepository.findAllByRole(Role.PROGRAMMER);
        program.getProgrammers().addAll(programmers);


        if (current.getRole() == Role.PROGRAMMER) {
            program.getProgrammers().add(current);
        }

        return programRepository.save(program);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Program> getAllPrograms() {
        User current = userService.getOrCreateCurrentUser();

        // VISITOR / USER
        if (current.getRole() == null || current.getRole() == Role.VISITOR || current.getRole() == Role.USER) {
            return programRepository.findByState(ProgramState.ANNOUNCED);
        }

        // SUBMITTER
        if (current.getRole() == Role.SUBMITTER) {
            Set<Program> out = new LinkedHashSet<>();
            out.addAll(programRepository.findByState(ProgramState.ANNOUNCED));
            out.addAll(programRepository.findProgramsCreatedBy(current.getId()));
            return new ArrayList<>(out);
        }

        // STAFF
        if (current.getRole() == Role.STAFF) {
            Set<Program> out = new LinkedHashSet<>();
            out.addAll(programRepository.findByState(ProgramState.ANNOUNCED));
            out.addAll(programRepository.findProgramsWhereUserIsStaff(current.getId()));
            out.addAll(programRepository.findProgramsWhereUserIsHandler(current.getId()));
            return new ArrayList<>(out);
        }

        // PROGRAMMER
        if (current.getRole() == Role.PROGRAMMER) {
            Set<Program> out = new LinkedHashSet<>();
            out.addAll(programRepository.findByState(ProgramState.ANNOUNCED));
            out.addAll(programRepository.findProgramsWhereUserIsProgrammer(current.getId()));
            out.addAll(programRepository.findProgramsCreatedBy(current.getId()));
            // optional:
            out.addAll(programRepository.findProgramsWithNoProgrammers());
            return new ArrayList<>(out);
        }

        // fallback
        return programRepository.findByState(ProgramState.ANNOUNCED);
    }

    @Override
    @Transactional(readOnly = true)
    public Program getProgramById(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + id));

        User current = userService.getOrCreateCurrentUser();


        if (program.getState() == ProgramState.ANNOUNCED) return program;

        if (current.getRole() == null || current.getRole() == Role.VISITOR || current.getRole() == Role.USER) {
            throw new ForbiddenException("You are not allowed to view this program");
        }

        boolean isCreator = program.getCreator() != null && program.getCreator().getId().equals(current.getId());
        boolean isProgrammer = program.getProgrammers() != null && program.getProgrammers().stream()
                .anyMatch(u -> u.getId().equals(current.getId()));
        boolean isStaff = program.getStaff() != null && program.getStaff().stream()
                .anyMatch(u -> u.getId().equals(current.getId()));

        // SUBMITTER
        if (current.getRole() == Role.SUBMITTER && isCreator) return program;

        // PROGRAMMER
        if (current.getRole() == Role.PROGRAMMER && (isProgrammer || isCreator)) return program;

        // STAFF
        if (current.getRole() == Role.STAFF && isStaff) return program;

        throw new ForbiddenException("You are not allowed to view this program");
    }

    @Override
    public Program updateProgram(Long id, Program updated) {
        Program existing = getProgramById(id);
        User current = userService.getOrCreateCurrentUser();

        boolean isProgrammer = existing.getProgrammers() != null &&
                existing.getProgrammers().stream().anyMatch(u -> u.getId().equals(current.getId()));

        if (current.getRole() != Role.PROGRAMMER || !isProgrammer) {
            throw new ForbiddenException("Only assigned programmer can update program");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        return programRepository.save(existing);
    }

    @Override
    public void deleteProgram(Long id) {
        Program existing = getProgramById(id);
        User current = userService.getOrCreateCurrentUser();

        boolean isProgrammer = existing.getProgrammers() != null &&
                existing.getProgrammers().stream().anyMatch(u -> u.getId().equals(current.getId()));

        if (current.getRole() != Role.PROGRAMMER || !isProgrammer) {
            throw new ForbiddenException("Only assigned programmer can delete program");
        }

        programRepository.delete(existing);
    }

    @Override
    public Program addProgrammer(Long programId, String username) {
        Program p = getProgramById(programId);
        User current = userService.getOrCreateCurrentUser();

        boolean isProgrammer = p.getProgrammers() != null &&
                p.getProgrammers().stream().anyMatch(u -> u.getId().equals(current.getId()));

        if (current.getRole() != Role.PROGRAMMER || !isProgrammer) {
            throw new ForbiddenException("Only programmer of this program can add programmers");
        }

        User u = userService.getUserByUsername(username);
        if (p.getProgrammers() == null) p.setProgrammers(new HashSet<>());
        p.getProgrammers().add(u);

        return programRepository.save(p);
    }

    @Override
    public Program addStaff(Long programId, String username) {
        Program p = getProgramById(programId);
        User current = userService.getOrCreateCurrentUser();

        boolean isProgrammer = p.getProgrammers() != null &&
                p.getProgrammers().stream().anyMatch(u -> u.getId().equals(current.getId()));

        if (current.getRole() != Role.PROGRAMMER || !isProgrammer) {
            throw new ForbiddenException("Only programmer of this program can add staff");
        }

        User u = userService.getUserByUsername(username);
        if (p.getStaff() == null) p.setStaff(new HashSet<>());
        p.getStaff().add(u);

        return programRepository.save(p);
    }

    @Override
    public Program changeProgramState(Long programId, ProgramState newState) {
        Program p = getProgramById(programId);
        User current = userService.getOrCreateCurrentUser();

        boolean isProgrammer = p.getProgrammers() != null &&
                p.getProgrammers().stream().anyMatch(u -> u.getId().equals(current.getId()));

        if (current.getRole() != Role.PROGRAMMER || !isProgrammer) {
            throw new ForbiddenException("Only assigned programmer can change program state");
        }


        ProgramState expected = NEXT.get(p.getState());
        if (expected != null && newState != expected) {
            throw new IllegalStateException("Invalid transition: " + p.getState() + " -> " + newState +
                    " (expected: " + expected + ")");
        }

        p.setState(newState);
        return programRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Program> searchPrograms(ProgramSearchRequest criteria) {
        List<Program> base = getAllPrograms();
        if (criteria == null) return base;

        return base.stream()
                .filter(p -> criteria.getName() == null || criteria.getName().isBlank()
                        || (p.getName() != null && p.getName().toLowerCase().contains(criteria.getName().toLowerCase())))
                .collect(Collectors.toList());
    }
}
