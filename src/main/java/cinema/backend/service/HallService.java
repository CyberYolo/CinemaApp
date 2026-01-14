package cinema.backend.service;

import cinema.backend.model.Hall;

import java.util.List;

public interface HallService {

    Hall createHall(Hall hall);

    List<Hall> getAllHalls();
}
