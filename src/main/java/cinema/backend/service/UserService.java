package cinema.backend.service;

import cinema.backend.model.User;

public interface UserService {

    User getOrCreateCurrentUser();

    User getUserByUsername(String username);
}
