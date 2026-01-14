package cinema.backend.dto.screening;

import java.time.LocalDateTime;

public class ScreeningUpdateRequest {

    private String filmTitle;
    private String filmCast;
    private String filmGenres;
    private int filmDurationMinutes;

    private String auditoriumName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getFilmTitle() {
        return filmTitle;
    }

    public void setFilmTitle(String filmTitle) {
        this.filmTitle = filmTitle;
    }

    public String getFilmCast() {
        return filmCast;
    }

    public void setFilmCast(String filmCast) {
        this.filmCast = filmCast;
    }

    public String getFilmGenres() {
        return filmGenres;
    }

    public void setFilmGenres(String filmGenres) {
        this.filmGenres = filmGenres;
    }

    public int getFilmDurationMinutes() {
        return filmDurationMinutes;
    }

    public void setFilmDurationMinutes(int filmDurationMinutes) {
        this.filmDurationMinutes = filmDurationMinutes;
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
}
