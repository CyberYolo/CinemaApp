package cinema.backend.dto.screening;

import cinema.backend.model.ScreeningState;

import java.time.LocalDateTime;

public class ScreeningDetailsDto {

    private Long id;
    private Long programId;

    private String filmTitle;
    private String filmCast;
    private String filmGenres;
    private int filmDurationMinutes;

    private String auditoriumName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private ScreeningState state;

    private String submitterUsername;
    private String handlerUsername;

    private Integer reviewScore;
    private String reviewComments;
    private String approvalNotes;
    private String rejectionReason;

    private boolean finalSubmitted;

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

    public ScreeningState getState() {
        return state;
    }

    public void setState(ScreeningState state) {
        this.state = state;
    }

    public String getSubmitterUsername() {
        return submitterUsername;
    }

    public void setSubmitterUsername(String submitterUsername) {
        this.submitterUsername = submitterUsername;
    }

    public String getHandlerUsername() {
        return handlerUsername;
    }

    public void setHandlerUsername(String handlerUsername) {
        this.handlerUsername = handlerUsername;
    }

    public Integer getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(Integer reviewScore) {
        this.reviewScore = reviewScore;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isFinalSubmitted() {
        return finalSubmitted;
    }

    public void setFinalSubmitted(boolean finalSubmitted) {
        this.finalSubmitted = finalSubmitted;
    }
}
