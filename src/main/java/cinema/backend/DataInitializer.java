package cinema.backend.util;

import cinema.backend.model.Role;
import cinema.backend.model.User;
import cinema.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        createUserIfMissing("visitor",  "Visitor User",   "visitor",   Role.VISITOR);
        createUserIfMissing("user1",    "Regular User 1", "user1",     Role.USER);
        createUserIfMissing("prog1",    "Programmer One", "prog1",     Role.USER);
        createUserIfMissing("staff1",   "Staff One",      "staff1",    Role.USER);
        createUserIfMissing("submitter","Submitter One",  "submitter", Role.USER);
        createUserIfMissing("admin",    "Admin User",     "admin",     Role.USER);
    }

    private void createUserIfMissing(String username,
                                     String fullName,
                                     String rawPassword,
                                     Role role) {
        userRepository.findByUsername(username).ifPresent(u -> {

        });

        if (userRepository.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setFullName(fullName);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setRole(role);
            userRepository.save(u);
        }
    }
}
