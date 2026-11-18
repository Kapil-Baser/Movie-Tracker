package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.MovieViewDto;
import com.example.movieapi.dto.MovieWindowView;
import com.example.movieapi.entity.CollectionType;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.pagination.MovieCursor;
import com.example.movieapi.pagination.MovieCursorCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class MovieWindowService {
    private final MovieViewAssemblerService viewAssembler;
    private final MovieCollectionService collectionService;
    private final MovieCursorCodec cursorCodec;

    @Autowired
    public MovieWindowService(MovieViewAssemblerService viewAssembler, MovieCollectionService collectionService, MovieCursorCodec cursorCodec) {
        this.viewAssembler = viewAssembler;
        this.collectionService = collectionService;
        this.cursorCodec = cursorCodec;
    }

    public MovieWindowView prepareView(AuthenticatedUser authenticatedUser,
                                                  CollectionType collectionType,
                                                  String cursor) {

        ScrollPosition scrollPosition =  resolvePosition(cursor);

        Window<MovieDto> moviesWindow = collectionService.getMoviesByCollectionType(collectionType, scrollPosition, 20);

        String nextCursor = generateNextCursor(moviesWindow);

        List<MovieViewDto> movieViewDtos = viewAssembler.buildMovieView(authenticatedUser, moviesWindow.getContent());
        return new MovieWindowView(movieViewDtos, moviesWindow.hasNext(), nextCursor);
    }

    private ScrollPosition resolvePosition(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return ScrollPosition.keyset();
        }

        MovieCursor movieCursor = cursorCodec.decode(cursor);
        return cursorCodec.toScrollPosition(movieCursor);
    }

    private String generateNextCursor(Window<MovieDto> window) {
        if (!window.hasNext() || window.isEmpty()) {
            return null;
        }

        ScrollPosition nextScrollPosition = window.positionAt(window.size() - 1);

        KeysetScrollPosition keysetScrollPosition = (KeysetScrollPosition) nextScrollPosition;

        MovieCursor nextCursor = cursorCodec.toMovieCursor(keysetScrollPosition);

        return cursorCodec.encode(nextCursor);
    }

}
