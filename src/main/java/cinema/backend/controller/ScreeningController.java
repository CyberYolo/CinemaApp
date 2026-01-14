package cinema.backend.controller;

import cinema.backend.dto.screening.*;
import cinema.backend.exception.ForbiddenException;
import cinema.backend.mapper.ScreeningMapper;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import cinema.backend.model.Role;
import cinema.backend.model.Screening;
import cinema.backend.model.ScreeningState;
import cinema.backend.model.User;
import cinema.backend.service.ProgramService;
import cinema.backend.service.ScreeningService;
import cinema.backend.service.UserService;
import cinema.backend.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programs/{programId}/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final UserService userService;
    private final ProgramService programService;

    public ScreeningController(ScreeningService screeningService, UserService userService, ProgramService programService) {
        this.screeningService = screeningService;
        this.userService = userService;
        this.programService = programService;
    }

    // LIST
    @GetMapping
    public List<?> getScreenings(@PathVariable Long programId) {
        Program program = programService.getProgramById(programId);
        User currentUser = userService.getOrCreateCurrentUser();

        boolean isVisitor = (currentUser.getRole() == null || currentUser.getRole() == Role.VISITOR);

        boolean isProgrammer = program.getProgrammers() != null &&
                program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

        boolean isStaffInProgram = program.getStaff() != null &&
                program.getStaff().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

        List<Screening> all = screeningService.getAllForProgram(programId).stream()
                .sorted(Comparator.comparing(Screening::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // VISITOR
        if (isVisitor) {
            return all.stream()
                    .filter(s -> program.getState() == ProgramState.ANNOUNCED && s.getState() == ScreeningState.SCHEDULED)
                    .map(ScreeningMapper::toPublicDto)
                    .collect(Collectors.toList());
        }

        // PROGRAMMER
        if (isProgrammer) {
            return all.stream().map(ScreeningMapper::toDetailsDto).collect(Collectors.toList());
        }

        // STAFF / SUBMITTER / USER
        return all.stream().map(s -> {
            boolean isSubmitter = s.getSubmitter() != null && s.getSubmitter().getId().equals(currentUser.getId());
            boolean isAssignedStaff = s.getHandler() != null && s.getHandler().getId().equals(currentUser.getId());


            if (isAssignedStaff || isSubmitter) {
                return ScreeningMapper.toDetailsDto(s);
            }


            if (isStaffInProgram) {
                return ScreeningMapper.toPublicDto(s);
            }


            if (program.getState() == ProgramState.ANNOUNCED && s.getState() == ScreeningState.SCHEDULED) {
                return ScreeningMapper.toPublicDto(s);
            }


            return null;
        }).filter(x -> x != null).collect(Collectors.toList());
    }

    // DETAILS
    @GetMapping("/{screeningId}")
    public Object getScreening(@PathVariable Long programId,
                               @PathVariable Long screeningId) {
        Screening screening = screeningService.getScreeningById(screeningId);

        if (screening.getProgram() == null || !screening.getProgram().getId().equals(programId)) {
            throw new IllegalArgumentException("Screening " + screeningId + " does not belong to program " + programId);
        }

        User currentUser = userService.getOrCreateCurrentUser();

        boolean isProgrammer = screening.getProgram().getProgrammers() != null &&
                screening.getProgram().getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

        boolean isSubmitter = screening.getSubmitter() != null &&
                screening.getSubmitter().getId().equals(currentUser.getId());

        boolean isAssignedStaff = screening.getHandler() != null &&
                screening.getHandler().getId().equals(currentUser.getId());

        if (isProgrammer || isSubmitter || isAssignedStaff) {
            return ScreeningMapper.toDetailsDto(screening);
        }

        if (screening.getProgram().getState() == ProgramState.ANNOUNCED &&
                screening.getState() == ScreeningState.SCHEDULED) {
            return ScreeningMapper.toPublicDto(screening);
        }

        throw new ForbiddenException("You are not allowed to view this screening");
    }
    // create / update / withdraw / submit
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningDetailsDto createScreening(@PathVariable Long programId,
                                               @RequestBody ScreeningCreateRequest request) {
        Screening screening = ScreeningMapper.fromCreateRequest(request);
        Screening saved = screeningService.createScreening(programId, screening);
        return ScreeningMapper.toDetailsDto(saved);
    }

    @PutMapping("/{screeningId}")
    public ScreeningDetailsDto updateScreening(@PathVariable Long programId,
                                               @PathVariable Long screeningId,
                                               @RequestBody ScreeningUpdateRequest request) {
        Screening updated = ScreeningMapper.fromUpdateRequest(request);
        Screening result = screeningService.updateScreening(screeningId, updated);
        return ScreeningMapper.toDetailsDto(result);
    }

    @DeleteMapping("/{screeningId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawScreening(@PathVariable Long programId,
                                  @PathVariable Long screeningId) {
        screeningService.withdrawScreening(screeningId);
    }

    @PostMapping("/{screeningId}/submit")
    public ScreeningDetailsDto submitScreening(@PathVariable Long programId,
                                               @PathVariable Long screeningId,
                                               HttpServletRequest request) {
        RateLimiter.checkRate("screening-submit:" + request.getRemoteAddr(), 5, Duration.ofSeconds(10));
        Screening result = screeningService.submitScreening(screeningId);
        return ScreeningMapper.toDetailsDto(result);
    }

    // assign / review / approve / reject / final-submit / accept
    @PostMapping("/{screeningId}/assign-handler")
    public ScreeningDetailsDto assignHandler(@PathVariable Long programId,
                                             @PathVariable Long screeningId,
                                             @RequestParam("username") String staffUsername) {
        Screening result = screeningService.assignHandler(screeningId, staffUsername);
        return ScreeningMapper.toDetailsDto(result);
    }

    @PostMapping("/{screeningId}/review")
    public ScreeningDetailsDto reviewScreening(@PathVariable Long programId,
                                               @PathVariable Long screeningId,
                                               @RequestBody ScreeningReviewRequest request) {
        Screening result = screeningService.reviewScreening(screeningId, request.getScore(), request.getComments());
        return ScreeningMapper.toDetailsDto(result);
    }

    @PostMapping("/{screeningId}/approve")
    public ScreeningDetailsDto approveScreening(@PathVariable Long programId,
                                                @PathVariable Long screeningId,
                                                @RequestParam(value = "notes", required = false) String notes) {
        Screening result = screeningService.approveScreening(screeningId, notes);
        return ScreeningMapper.toDetailsDto(result);
    }

    @PostMapping("/{screeningId}/reject")
    public ScreeningDetailsDto rejectScreening(@PathVariable Long programId,
                                               @PathVariable Long screeningId,
                                               @RequestParam("reason") String reason) {
        Screening result = screeningService.rejectScreening(screeningId, reason);
        return ScreeningMapper.toDetailsDto(result);
    }

    @PostMapping("/{screeningId}/final-submit")
    public ScreeningDetailsDto finalSubmitScreening(@PathVariable Long programId,
                                                    @PathVariable Long screeningId,
                                                    HttpServletRequest request) {
        RateLimiter.checkRate("screening-final-submit:" + request.getRemoteAddr(), 5, Duration.ofSeconds(10));
        Screening result = screeningService.finalSubmitScreening(screeningId);
        return ScreeningMapper.toDetailsDto(result);
    }

    @PostMapping("/{screeningId}/accept")
    public ScreeningDetailsDto acceptScreening(@PathVariable Long programId,
                                               @PathVariable Long screeningId) {
        Screening result = screeningService.acceptScreening(screeningId);
        return ScreeningMapper.toDetailsDto(result);
    }

    // SEARCH
    @PostMapping("/search")
    public List<?> searchScreenings(@PathVariable Long programId,
                                    @RequestBody ScreeningSearchRequest criteria,
                                    HttpServletRequest request) {
        RateLimiter.checkRate("screening-search:" + request.getRemoteAddr(), 10, Duration.ofSeconds(10));

        User currentUser = userService.getOrCreateCurrentUser();
        boolean isVisitor = (currentUser.getRole() == null || currentUser.getRole() == Role.VISITOR);

        List<Screening> list = screeningService.searchScreenings(programId, criteria);

        if (isVisitor) {
            return list.stream().map(ScreeningMapper::toPublicDto).collect(Collectors.toList());
        }
        return list.stream().map(ScreeningMapper::toDetailsDto).collect(Collectors.toList());
    }
}
