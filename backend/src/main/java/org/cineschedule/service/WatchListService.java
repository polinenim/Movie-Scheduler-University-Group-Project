package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.domain.Movie;

import java.time.Instant;
import java.util.List;

public interface WatchListService {
  Future<List<Movie>> list(long userId);
  Future<Long> add(long userId, String omdbId);
  Future<Integer> markWatched(long userId, String omdbId);
  Future<Integer> remove(long userId, String omdbId);
  Future<Movie> find(long userId, String omdbId);
  Future<Long> schedule(long userId, String omdbId, Instant scheduledAt);
}
