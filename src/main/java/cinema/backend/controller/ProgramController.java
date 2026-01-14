package cinema.backend.controller;

import cinema.backend.dto.program.*;
import cinema.backend.mapper.ProgramMapper;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import cinema.backend.service.ProgramService;
import cinema.backend.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    // LIST
    @GetMapping
    public List<ProgramPublicDto> getAllPrograms() {
        return programService.getAllPrograms()
                .stream()
                .map(ProgramMapper::toPublicDto)
                .collect(Collectors.toList());
    }

    // SEARCH
    @PostMapping("/search")
    public List<ProgramPublicDto> searchPrograms(@RequestBody ProgramSearchRequest criteria,
                                                 HttpServletRequest request) {
        RateLimiter.checkRate("program-search:" + request.getRemoteAddr(), 10, Duration.ofSeconds(10));

        return programService.searchPrograms(criteria)
                .stream()
                .map(ProgramMapper::toPublicDto)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Object getProgramById(@PathVariable Long id) {
        Program program = programService.getProgramById(id);
        // service already enforces visibility; controller can return details always,
        // but keep as you want (public vs details).
        return ProgramMapper.toDetailsDto(program);
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramDetailsDto createProgram(@RequestBody ProgramCreateRequest request) {
        Program program = ProgramMapper.fromCreateRequest(request);
        Program saved = programService.createProgram(program);
        return ProgramMapper.toDetailsDto(saved);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ProgramDetailsDto updateProgram(@PathVariable Long id,
                                           @RequestBody ProgramUpdateRequest request) {
        Program existing = programService.getProgramById(id);
        ProgramMapper.updateProgramFromRequest(request, existing);
        Program updated = programService.updateProgram(id, existing);
        return ProgramMapper.toDetailsDto(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
    }

    // ROLE ASSIGNMENTS
    @PostMapping("/{id}/programmers/{username}")
    public ProgramDetailsDto addProgrammer(@PathVariable Long id,
                                           @PathVariable String username) {
        Program updated = programService.addProgrammer(id, username);
        return ProgramMapper.toDetailsDto(updated);
    }

    @PostMapping("/{id}/staff/{username}")
    public ProgramDetailsDto addStaff(@PathVariable Long id,
                                      @PathVariable String username) {
        Program updated = programService.addStaff(id, username);
        return ProgramMapper.toDetailsDto(updated);
    }

    // STATE
    @PostMapping("/{id}/state")
    public ProgramDetailsDto changeProgramState(@PathVariable Long id,
                                                @RequestParam("newState") ProgramState newState) {
        Program updated = programService.changeProgramState(id, newState);
        return ProgramMapper.toDetailsDto(updated);
    }
}
