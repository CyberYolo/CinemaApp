package cinema.backend.service.impl;

import cinema.backend.model.Hall;
import cinema.backend.repository.HallRepository;
import cinema.backend.service.HallService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;

    public HallServiceImpl(HallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    @Override
    public Hall createHall(Hall hall) {
        return hallRepository.save(hall);
    }

    @Override
    public List<Hall> getAllHalls() {
        return hallRepository.findAll();
    }
}
