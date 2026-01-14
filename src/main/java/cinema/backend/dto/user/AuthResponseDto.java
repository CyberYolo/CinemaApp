package cinema.backend.dto.user;

import java.util.Set;

public class AuthResponseDto {

    private String username;
    private Set<String> roles;
    private String fullName;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String username, Set<String> roles, String fullName) {
        this.username = username;
        this.roles = roles;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
