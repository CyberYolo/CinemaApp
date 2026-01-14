package cinema.backend.controller;

import cinema.backend.dto.hall.HallDto;
import cinema.backend.dto.hall.HallRequest;
import cinema.backend.mapper.HallMapper;
import cinema.backend.model.Hall;
import cinema.backend.service.HallService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HallDto createHall(@RequestBody HallRequest request) {
        Hall hall = HallMapper.fromRequest(request);
        Hall saved = hallService.createHall(hall);
        return HallMapper.toDto(saved);
    }

    @GetMapping
    public List<HallDto> getAllHalls() {
        return hallService.getAllHalls().stream()
                .map(HallMapper::toDto)
                .collect(Collectors.toList());
    }
}
