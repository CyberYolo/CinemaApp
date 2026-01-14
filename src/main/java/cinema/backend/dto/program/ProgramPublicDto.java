package cinema.backend.dto.program;

import java.time.LocalDate;

public class ProgramPublicDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private String state;


    private java.util.List<String> programmerUsernames;

    private java.util.List<String> auditoriums;


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

    public java.util.List<String> getProgrammerUsernames() {
        return programmerUsernames;
    }

    public void setProgrammerUsernames(java.util.List<String> programmerUsernames) {
        this.programmerUsernames = programmerUsernames;
    }

    public java.util.List<String> getAuditoriums() {
        return auditoriums;
    }

    public void setAuditoriums(java.util.List<String> auditoriums) {
        this.auditoriums = auditoriums;
    }
}
