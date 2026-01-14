package cinema.backend.mapper;

import cinema.backend.dto.hall.HallDto;
import cinema.backend.dto.hall.HallRequest;
import cinema.backend.model.Hall;

public class HallMapper {

    private HallMapper() {

    }

    public static Hall fromRequest(HallRequest request) {
        Hall hall = new Hall();
        hall.setName(request.getName());
        hall.setCapacity(request.getCapacity());
        return hall;
    }

    public static HallDto toDto(Hall hall) {
        HallDto dto = new HallDto();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setCapacity(hall.getCapacity());
        return dto;
    }
}
