package cinema.backend.service;

import cinema.backend.dto.screening.ScreeningSearchRequest;
import cinema.backend.model.Screening;

import java.util.List;

public interface ScreeningService {

    Screening createScreening(Long programId, Screening screening);

    Screening updateScreening(Long screeningId, Screening updated);

    void withdrawScreening(Long screeningId);

    Screening submitScreening(Long screeningId);

    Screening assignHandler(Long screeningId, String staffUsername);

    Screening reviewScreening(Long screeningId, Integer score, String comments);

    Screening approveScreening(Long screeningId, String approvalNotes);

    Screening rejectScreening(Long screeningId, String reason);

    Screening finalSubmitScreening(Long screeningId);

    Screening acceptScreening(Long screeningId);

    Screening getScreeningById(Long screeningId);

    List<Screening> getAllForProgram(Long programId);

    List<Screening> searchScreenings(Long programId, ScreeningSearchRequest criteria);
}
