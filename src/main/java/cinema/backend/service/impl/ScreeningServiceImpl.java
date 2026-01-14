package cinema.backend.service.impl;

import cinema.backend.dto.screening.ScreeningSearchRequest;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import cinema.backend.model.Role;
import cinema.backend.model.Screening;
import cinema.backend.model.ScreeningState;
import cinema.backend.model.User;
import cinema.backend.repository.ProgramRepository;
import cinema.backend.repository.ScreeningRepository;
import cinema.backend.service.ScreeningService;
import cinema.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScreeningServiceImpl implements ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;
    private final UserService userService;

    public ScreeningServiceImpl(ScreeningRepository screeningRepository,
                                ProgramRepository programRepository,
                                UserService userService) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
        this.userService = userService;
    }


    @Override
    public Screening createScreening(Long programId, Screening screening) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));

        if (program.getState() != ProgramState.SUBMISSION) {
            throw new IllegalStateException("Screenings can be created only while program is in SUBMISSION");
        }

        User currentUser = userService.getOrCreateCurrentUser();

        if (currentUser.getRole() == null || currentUser.getRole() == Role.VISITOR) {
            throw new IllegalStateException("Only authenticated users can create screenings");
        }


        boolean isAssignedProgrammer = program.getProgrammers() != null &&
                program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        if (isAssignedProgrammer) {
            throw new IllegalStateException("Programmer cannot submit screenings in his/her own program");
        }


        boolean isStaff = program.getStaff() != null &&
                program.getStaff().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        if (isStaff) {
            throw new IllegalStateException("Staff members cannot submit screenings in their own program");
        }

        screening.setId(null);
        screening.setProgram(program);
        screening.setSubmitter(currentUser);
        screening.setState(ScreeningState.CREATED);
        screening.setCreationDate(LocalDateTime.now());

        screening.setHandler(null);
        screening.setReviewScore(null);
        screening.setReviewComments(null);
        screening.setApprovalNotes(null);
        screening.setRejectionReason(null);
        screening.setFinalSubmitted(false);

        validateDuration(screening);

        return screeningRepository.save(screening);
    }

    @Override
    public Screening updateScreening(Long screeningId, Screening updated) {
        Screening existing = findByIdOrThrow(screeningId);
        User currentUser = userService.getOrCreateCurrentUser();

        if (existing.getSubmitter() == null || !existing.getSubmitter().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the submitter can update this screening");
        }

        if (existing.getState() != ScreeningState.CREATED) {
            throw new IllegalStateException("Screening can be updated only in CREATED state");
        }

        existing.setFilmTitle(updated.getFilmTitle());
        existing.setFilmCast(updated.getFilmCast());
        existing.setFilmGenres(updated.getFilmGenres());
        existing.setFilmDurationMinutes(updated.getFilmDurationMinutes());
        existing.setAuditoriumName(updated.getAuditoriumName());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());

        validateDuration(existing);

        return screeningRepository.save(existing);
    }

    @Override
    public void withdrawScreening(Long screeningId) {
        Screening existing = findByIdOrThrow(screeningId);
        User currentUser = userService.getOrCreateCurrentUser();

        if (existing.getSubmitter() == null || !existing.getSubmitter().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the submitter can withdraw this screening");
        }

        if (existing.getState() != ScreeningState.CREATED) {
            throw new IllegalStateException("Only screenings in CREATED state can be withdrawn");
        }

        screeningRepository.delete(existing);
    }

    @Override
    public Screening submitScreening(Long screeningId) {
        Screening existing = findByIdOrThrow(screeningId);
        User currentUser = userService.getOrCreateCurrentUser();

        if (existing.getSubmitter() == null || !existing.getSubmitter().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the submitter can submit this screening");
        }

        if (existing.getState() != ScreeningState.CREATED) {
            throw new IllegalStateException("Only screenings in CREATED state can be submitted");
        }

        Program program = existing.getProgram();
        if (program == null || program.getState() != ProgramState.SUBMISSION) {
            throw new IllegalStateException("Screenings can be submitted only while program is in SUBMISSION state");
        }

        validateComplete(existing);

        existing.setState(ScreeningState.SUBMITTED);
        return screeningRepository.save(existing);
    }


    @Override
    public Screening assignHandler(Long screeningId, String staffUsername) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();


        if (currentUser.getRole() != Role.PROGRAMMER) {
            throw new IllegalStateException("Only PROGRAMMER can assign handlers");
        }


        boolean isProgrammerOfThisProgram = program.getProgrammers() != null &&
                program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        if (!isProgrammerOfThisProgram) {
            throw new IllegalStateException("Only the assigned PROGRAMMER of this program can assign handlers");
        }

        if (program.getState() != ProgramState.ASSIGNMENT) {
            throw new IllegalStateException("Handlers can be assigned only during ASSIGNMENT state");
        }

        if (screening.getState() != ScreeningState.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED screenings can be assigned to staff");
        }

        if (screening.getHandler() != null) {
            throw new IllegalStateException("Handler has already been assigned for this screening");
        }

        User staffUser = userService.getUserByUsername(staffUsername);

        if (staffUser.getRole() != Role.STAFF) {
            throw new IllegalStateException("Assigned handler must have STAFF role");
        }


        if (program.getStaff() == null) {
            program.setStaff(new HashSet<>());
        }
        boolean alreadyStaff = program.getStaff().stream().anyMatch(u -> u.getId().equals(staffUser.getId()));
        if (!alreadyStaff) {
            program.getStaff().add(staffUser);
            programRepository.save(program);
        }

        screening.setHandler(staffUser);
        return screeningRepository.save(screening);
    }

    @Override
    public Screening reviewScreening(Long screeningId, Integer score, String comments) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();

        if (program.getState() != ProgramState.REVIEW) {
            throw new IllegalStateException("Screenings can be reviewed only while program is in REVIEW state");
        }


        if (screening.getHandler() == null || !screening.getHandler().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the assigned handler can review this screening");
        }

        if (screening.getState() != ScreeningState.SUBMITTED && screening.getState() != ScreeningState.REVIEWED) {
            throw new IllegalStateException("Only SUBMITTED/REVIEWED screenings can be reviewed");
        }

        screening.setReviewScore(score);
        screening.setReviewComments(comments);
        screening.setState(ScreeningState.REVIEWED);

        return screeningRepository.save(screening);
    }

    @Override
    public Screening approveScreening(Long screeningId, String approvalNotes) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();

        if (program.getState() != ProgramState.SCHEDULING) {
            throw new IllegalStateException("Screenings can be approved only while program is in SCHEDULING state");
        }

        boolean isSubmitter = screening.getSubmitter() != null &&
                screening.getSubmitter().getId().equals(currentUser.getId());
        if (!isSubmitter) {
            throw new IllegalStateException("Only the submitter can approve the reviewed screening");
        }

        if (screening.getState() != ScreeningState.REVIEWED) {
            throw new IllegalStateException("Only REVIEWED screenings can be approved");
        }

        screening.setApprovalNotes(approvalNotes);
        screening.setRejectionReason(null);
        screening.setState(ScreeningState.APPROVED);

        return screeningRepository.save(screening);
    }

    @Override
    public Screening rejectScreening(Long screeningId, String reason) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();


        if (currentUser.getRole() != Role.PROGRAMMER) {
            throw new IllegalStateException("Only PROGRAMMER can reject screenings");
        }
        boolean isProgrammerOfThisProgram = program.getProgrammers() != null &&
                program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        if (!isProgrammerOfThisProgram) {
            throw new IllegalStateException("Only the assigned PROGRAMMER of this program can reject screenings");
        }

        if (program.getState() != ProgramState.SCHEDULING && program.getState() != ProgramState.DECISION) {
            throw new IllegalStateException("Screenings can be rejected only in SCHEDULING or DECISION state");
        }

        if (screening.getState() == ScreeningState.SCHEDULED || screening.getState() == ScreeningState.REJECTED) {
            throw new IllegalStateException("Cannot reject an already scheduled or rejected screening");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        screening.setRejectionReason(reason);
        screening.setState(ScreeningState.REJECTED);

        return screeningRepository.save(screening);
    }

    @Override
    public Screening finalSubmitScreening(Long screeningId) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();

        if (program.getState() != ProgramState.FINAL_PUBLICATION) {
            throw new IllegalStateException("Final submission allowed only in FINAL_PUBLICATION state");
        }

        if (screening.getSubmitter() == null || !screening.getSubmitter().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the submitter can perform final submission");
        }

        if (screening.getState() != ScreeningState.APPROVED) {
            throw new IllegalStateException("Only APPROVED screenings can be finally submitted");
        }

        screening.setFinalSubmitted(true);
        return screeningRepository.save(screening);
    }

    @Override
    public Screening acceptScreening(Long screeningId) {
        Screening screening = findByIdOrThrow(screeningId);
        Program program = screening.getProgram();
        User currentUser = userService.getOrCreateCurrentUser();

        if (currentUser.getRole() != Role.PROGRAMMER) {
            throw new IllegalStateException("Only PROGRAMMER can accept screenings");
        }
        boolean isProgrammerOfThisProgram = program.getProgrammers() != null &&
                program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        if (!isProgrammerOfThisProgram) {
            throw new IllegalStateException("Only the assigned PROGRAMMER of this program can accept screenings");
        }

        if (program.getState() != ProgramState.DECISION) {
            throw new IllegalStateException("Screenings can be accepted only during DECISION state");
        }

        if (screening.getState() != ScreeningState.APPROVED || !screening.isFinalSubmitted()) {
            throw new IllegalStateException("Only APPROVED and finally submitted screenings can be scheduled");
        }

        screening.setState(ScreeningState.SCHEDULED);
        return screeningRepository.save(screening);
    }


    @Override
    public Screening getScreeningById(Long screeningId) {
        return findByIdOrThrow(screeningId);
    }

    @Override
    public List<Screening> getAllForProgram(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));
        return screeningRepository.findByProgramId(program.getId());
    }

    @Override
    public List<Screening> searchScreenings(Long programId, ScreeningSearchRequest criteria) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));

        ScreeningSearchRequest effectiveCriteria =
                (criteria != null) ? criteria : new ScreeningSearchRequest();

        User currentUser = userService.getOrCreateCurrentUser();

        List<Screening> all = screeningRepository.findByProgramId(programId);


        boolean isProgrammerOfThisProgram = currentUser.getRole() == Role.PROGRAMMER
                && program.getProgrammers() != null
                && program.getProgrammers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

        boolean isStaffInProgram = program.getStaff() != null &&
                program.getStaff().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));

        return all.stream()
                .filter(s -> userCanSeeScreeningInSearch(program, s, currentUser, isProgrammerOfThisProgram, isStaffInProgram))
                .filter(s -> matchesText(s.getFilmTitle(), effectiveCriteria.getFilmTitle()))
                .filter(s -> matchesText(s.getFilmCast(), effectiveCriteria.getCast()))
                .filter(s -> matchesText(s.getFilmGenres(), effectiveCriteria.getGenre()))
                .filter(s -> matchesDateRange(s, effectiveCriteria.getDateFrom(), effectiveCriteria.getDateTo()))
                .sorted(Comparator
                        .comparing(Screening::getFilmGenres, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(Screening::getFilmTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }



    private Screening findByIdOrThrow(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found: " + id));
    }

    private void validateDuration(Screening screening) {
        if (screening.getStartTime() != null && screening.getEndTime() != null) {
            int durationMinutes = screening.getFilmDurationMinutes();
            if (durationMinutes <= 0) return;

            long minutes = Duration.between(screening.getStartTime(), screening.getEndTime()).toMinutes();
            if (minutes < durationMinutes) {
                throw new IllegalArgumentException("End time is before movie duration is complete");
            }
        }
    }

    private void validateComplete(Screening screening) {
        if (screening.getFilmTitle() == null || screening.getFilmTitle().isBlank()) {
            throw new IllegalArgumentException("Film title is required before submission");
        }
        if (screening.getAuditoriumName() == null || screening.getAuditoriumName().isBlank()) {
            throw new IllegalArgumentException("Auditorium name is required before submission");
        }
        if (screening.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required before submission");
        }
        if (screening.getFilmDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Film duration must be positive");
        }

        if (screening.getEndTime() == null) {
            screening.setEndTime(screening.getStartTime().plusMinutes(screening.getFilmDurationMinutes()));
        }

        validateDuration(screening);
    }

    private boolean matchesText(String field, String search) {
        if (search == null || search.isBlank()) return true;
        if (field == null) return false;

        String lowerField = field.toLowerCase();
        String[] words = search.toLowerCase().split("\\s+");
        for (String w : words) {
            if (!lowerField.contains(w)) return false;
        }
        return true;
    }

    private boolean matchesDateRange(Screening s, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (s.getStartTime() == null) return false;

        LocalDate date = s.getStartTime().toLocalDate();
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private boolean userCanSeeScreeningInSearch(Program program,
                                                Screening screening,
                                                User currentUser,
                                                boolean isProgrammerOfThisProgram,
                                                boolean isStaffInProgram) {

        // PROGRAMMER
        if (isProgrammerOfThisProgram) {
            return true;
        }

        // STAFF
        if (isStaffInProgram) {
            boolean isAssigned = screening.getHandler() != null &&
                    screening.getHandler().getId().equals(currentUser.getId());
            if (isAssigned) return true;
        }

        // SUBMITTER
        boolean isSubmitter = screening.getSubmitter() != null &&
                screening.getSubmitter().getId().equals(currentUser.getId());
        if (isSubmitter) return true;

        // VISITOR/USER
        return program.getState() == ProgramState.ANNOUNCED
                && screening.getState() == ScreeningState.SCHEDULED;
    }
}
