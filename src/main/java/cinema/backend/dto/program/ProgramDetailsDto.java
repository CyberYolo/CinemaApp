package cinema.backend.dto.program;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProgramDetailsDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String state;

    private LocalDateTime creationDate;
    private String creatorUsername;

    private java.util.List<String> programmerUsernames;
    private java.util.List<String> staffUsernames;

    private Integer programmersCount;
    private Integer staffCount;
    private Integer screeningsCount;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public java.util.List<String> getProgrammerUsernames() {
        return programmerUsernames;
    }

    public void setProgrammerUsernames(java.util.List<String> programmerUsernames) {
        this.programmerUsernames = programmerUsernames;
    }

    public java.util.List<String> getStaffUsernames() {
        return staffUsernames;
    }

    public void setStaffUsernames(java.util.List<String> staffUsernames) {
        this.staffUsernames = staffUsernames;
    }

    public Integer getProgrammersCount() {
        return programmersCount;
    }

    public void setProgrammersCount(Integer programmersCount) {
        this.programmersCount = programmersCount;
    }

    public Integer getStaffCount() {
        return staffCount;
    }

    public void setStaffCount(Integer staffCount) {
        this.staffCount = staffCount;
    }

    public Integer getScreeningsCount() {
        return screeningsCount;
    }

    public void setScreeningsCount(Integer screeningsCount) {
        this.screeningsCount = screeningsCount;
    }
}
