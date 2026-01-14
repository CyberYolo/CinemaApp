package cinema.backend.controller;

import cinema.backend.dto.movie.MovieDto;
import cinema.backend.dto.movie.MovieRequest;
import cinema.backend.mapper.MovieMapper;
import cinema.backend.model.Movie;
import cinema.backend.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDto createMovie(@RequestBody MovieRequest request) {
        Movie movie = MovieMapper.fromRequest(request);
        Movie saved = movieService.createMovie(movie);
        return MovieMapper.toDto(saved);
    }

    @GetMapping
    public List<MovieDto> getAllMovies() {
        return movieService.getAllMovies().stream()
                .map(MovieMapper::toDto)
                .collect(Collectors.toList());
    }
}
