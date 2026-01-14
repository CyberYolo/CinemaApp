package cinema.backend.service.impl;

import cinema.backend.model.Role;
import cinema.backend.model.User;
import cinema.backend.repository.UserRepository;
import cinema.backend.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getOrCreateCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;


        if (username == null || "anonymousUser".equals(username)) {
            return userRepository.findByUsername("visitor")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setUsername("visitor");
                        u.setPassword("dummy");
                        u.setRole(Role.VISITOR);
                        u.setFullName("Visitor User");
                        return userRepository.save(u);
                    });
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
