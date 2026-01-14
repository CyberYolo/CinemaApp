package cinema.backend.mapper;

import cinema.backend.dto.program.ProgramCreateRequest;
import cinema.backend.dto.program.ProgramDetailsDto;
import cinema.backend.dto.program.ProgramPublicDto;
import cinema.backend.dto.program.ProgramUpdateRequest;
import cinema.backend.model.Program;
import cinema.backend.model.ProgramState;
import cinema.backend.model.Screening;
import cinema.backend.model.ScreeningState;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProgramMapper {

    private ProgramMapper() {

    }


    public static ProgramPublicDto toPublicDto(Program program) {
        if (program == null) {
            return null;
        }

        ProgramPublicDto dto = new ProgramPublicDto();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());
        dto.setStartDate(program.getStartDate());
        dto.setEndDate(program.getEndDate());


        ProgramState state = program.getState();
        dto.setState(state != null ? state.name() : null);


        if (program.getProgrammers() != null) {
            dto.setProgrammerUsernames(
                    program.getProgrammers().stream()
                            .filter(Objects::nonNull)
                            .map(u -> u.getUsername())
                            .filter(Objects::nonNull)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList())
            );
        }


        if (program.getScreenings() != null) {
            dto.setAuditoriums(
                    program.getScreenings().stream()
                            .filter(Objects::nonNull)
                            .filter(s -> s.getState() == ScreeningState.SCHEDULED)
                            .map(Screening::getAuditoriumName)
                            .filter(a -> a != null && !a.isBlank())
                            .distinct()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }


    public static ProgramDetailsDto toDetailsDto(Program program) {
        if (program == null) {
            return null;
        }

        ProgramDetailsDto dto = new ProgramDetailsDto();

        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());
        dto.setStartDate(program.getStartDate());
        dto.setEndDate(program.getEndDate());

        ProgramState state = program.getState();
        dto.setState(state != null ? state.name() : null);

        dto.setCreationDate(program.getCreationDate());
        if (program.getCreator() != null) {
            dto.setCreatorUsername(program.getCreator().getUsername());
        }


        if (program.getProgrammers() != null) {
            dto.setProgrammerUsernames(
                    program.getProgrammers().stream()
                            .filter(Objects::nonNull)
                            .map(u -> u.getUsername())
                            .filter(Objects::nonNull)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList())
            );
        }
        if (program.getStaff() != null) {
            dto.setStaffUsernames(
                    program.getStaff().stream()
                            .filter(Objects::nonNull)
                            .map(u -> u.getUsername())
                            .filter(Objects::nonNull)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList())
            );
        }

        // μετρητές για programmers / staff / screenings
        int programmersCount = program.getProgrammers() != null
                ? program.getProgrammers().size()
                : 0;

        int staffCount = program.getStaff() != null
                ? program.getStaff().size()
                : 0;

        int screeningsCount = program.getScreenings() != null
                ? program.getScreenings().size()
                : 0;

        dto.setProgrammersCount(programmersCount);
        dto.setStaffCount(staffCount);
        dto.setScreeningsCount(screeningsCount);

        return dto;
    }


    public static Program fromCreateRequest(ProgramCreateRequest request) {
        if (request == null) {
            return null;
        }

        Program program = new Program();
        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setStartDate(request.getStartDate());
        program.setEndDate(request.getEndDate());
        return program;
    }

    public static void updateProgramFromRequest(ProgramUpdateRequest request, Program program) {
        if (request == null || program == null) {
            return;
        }

        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setStartDate(request.getStartDate());
        program.setEndDate(request.getEndDate());
    }
}
