package cinema.backend.mapper;

import cinema.backend.dto.screening.ScreeningCreateRequest;
import cinema.backend.dto.screening.ScreeningDetailsDto;
import cinema.backend.dto.screening.ScreeningPublicDto;
import cinema.backend.dto.screening.ScreeningUpdateRequest;
import cinema.backend.model.Screening;

public class ScreeningMapper {

    private ScreeningMapper() {

    }


    public static Screening fromCreateRequest(ScreeningCreateRequest request) {
        Screening screening = new Screening();
        screening.setFilmTitle(request.getFilmTitle());
        screening.setFilmCast(request.getFilmCast());
        screening.setFilmGenres(request.getFilmGenres());
        screening.setFilmDurationMinutes(request.getFilmDurationMinutes());
        screening.setAuditoriumName(request.getAuditoriumName());
        screening.setStartTime(request.getStartTime());
        screening.setEndTime(request.getEndTime());
        return screening;
    }

    public static Screening fromUpdateRequest(ScreeningUpdateRequest request) {
        Screening screening = new Screening();
        screening.setFilmTitle(request.getFilmTitle());
        screening.setFilmCast(request.getFilmCast());
        screening.setFilmGenres(request.getFilmGenres());
        screening.setFilmDurationMinutes(request.getFilmDurationMinutes());
        screening.setAuditoriumName(request.getAuditoriumName());
        screening.setStartTime(request.getStartTime());
        screening.setEndTime(request.getEndTime());
        return screening;
    }



    public static ScreeningPublicDto toPublicDto(Screening screening) {
        ScreeningPublicDto dto = new ScreeningPublicDto();
        dto.setId(screening.getId());
        dto.setProgramId(screening.getProgram() != null ? screening.getProgram().getId() : null);
        dto.setFilmTitle(screening.getFilmTitle());
        dto.setFilmGenres(screening.getFilmGenres());
        dto.setAuditoriumName(screening.getAuditoriumName());
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setState(screening.getState());
        return dto;
    }

    public static ScreeningDetailsDto toDetailsDto(Screening screening) {
        ScreeningDetailsDto dto = new ScreeningDetailsDto();

        dto.setId(screening.getId());
        dto.setProgramId(screening.getProgram() != null ? screening.getProgram().getId() : null);

        dto.setFilmTitle(screening.getFilmTitle());
        dto.setFilmCast(screening.getFilmCast());
        dto.setFilmGenres(screening.getFilmGenres());
        dto.setFilmDurationMinutes(screening.getFilmDurationMinutes());

        dto.setAuditoriumName(screening.getAuditoriumName());

        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());

        dto.setState(screening.getState());

        if (screening.getSubmitter() != null) {
            dto.setSubmitterUsername(screening.getSubmitter().getUsername());
        }
        if (screening.getHandler() != null) {
            dto.setHandlerUsername(screening.getHandler().getUsername());
        }

        dto.setReviewScore(screening.getReviewScore());
        dto.setReviewComments(screening.getReviewComments());
        dto.setApprovalNotes(screening.getApprovalNotes());
        dto.setRejectionReason(screening.getRejectionReason());

        dto.setFinalSubmitted(screening.isFinalSubmitted());

        return dto;
    }
}
