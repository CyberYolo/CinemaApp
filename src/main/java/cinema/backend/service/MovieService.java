package cinema.backend.service;

import cinema.backend.model.Movie;

import java.util.List;

public interface MovieService {

    Movie createMovie(Movie movie);

    List<Movie> getAllMovies();
}
