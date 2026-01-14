package cinema.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screening")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime creationDate;

    @ManyToOne(optional = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    private ScreeningState state;

    private String filmTitle;
    private String filmCast;
    private String filmGenres;
    private int filmDurationMinutes;

    private String auditoriumName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne
    private User submitter;

    @ManyToOne
    private User handler;

    private Integer reviewScore;

    @Column(length = 2000)
    private String reviewComments;

    @Column(length = 2000)
    private String approvalNotes;

    @Column(length = 2000)
    private String rejectionReason;

    private boolean finalSubmitted;

    public Screening() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }

    public ScreeningState getState() { return state; }
    public void setState(ScreeningState state) { this.state = state; }

    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }

    public String getFilmCast() { return filmCast; }
    public void setFilmCast(String filmCast) { this.filmCast = filmCast; }

    public String getFilmGenres() { return filmGenres; }
    public void setFilmGenres(String filmGenres) { this.filmGenres = filmGenres; }

    public int getFilmDurationMinutes() { return filmDurationMinutes; }
    public void setFilmDurationMinutes(int filmDurationMinutes) { this.filmDurationMinutes = filmDurationMinutes; }

    public String getAuditoriumName() { return auditoriumName; }
    public void setAuditoriumName(String auditoriumName) { this.auditoriumName = auditoriumName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public User getSubmitter() { return submitter; }
    public void setSubmitter(User submitter) { this.submitter = submitter; }

    public User getHandler() { return handler; }
    public void setHandler(User handler) { this.handler = handler; }

    public Integer getReviewScore() { return reviewScore; }
    public void setReviewScore(Integer reviewScore) { this.reviewScore = reviewScore; }

    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }

    public String getApprovalNotes() { return approvalNotes; }
    public void setApprovalNotes(String approvalNotes) { this.approvalNotes = approvalNotes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public boolean isFinalSubmitted() { return finalSubmitted; }
    public void setFinalSubmitted(boolean finalSubmitted) { this.finalSubmitted = finalSubmitted; }
}
