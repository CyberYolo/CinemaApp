package cinema.backend.dto.screening;

import cinema.backend.model.ScreeningState;

import java.time.LocalDateTime;

public class ScreeningPublicDto {

    private Long id;
    private Long programId;

    private String filmTitle;
    private String filmGenres;
    private String auditoriumName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private ScreeningState state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public String getFilmTitle() {
        return filmTitle;
    }

    public void setFilmTitle(String filmTitle) {
        this.filmTitle = filmTitle;
    }

    public String getFilmGenres() {
        return filmGenres;
    }

    public void setFilmGenres(String filmGenres) {
        this.filmGenres = filmGenres;
    }

    public String getAuditoriumName() {
        return auditoriumName;
    }

    public void setAuditoriumName(String auditoriumName) {
        this.auditoriumName = auditoriumName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ScreeningState getState() {
        return state;
    }

    public void setState(ScreeningState state) {
        this.state = state;
    }
}
