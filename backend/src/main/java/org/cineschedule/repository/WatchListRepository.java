package org.cineschedule.repository;

import io.vertx.core.Future;
import org.cineschedule.domain.Movie;

import java.time.Instant;
import java.util.List;

public interface WatchListRepository {
  Future<List<Movie>> listByUser(long userId);
  Future<Long> addOrUpdate(long userId, String omdbId, boolean watched, Instant watchedAt);
  Future<Integer> markWatched(long userId, String omdbId, boolean watched, Instant watchedAt);
  Future<Movie> findByUserAndOmdb(long userId, String omdbId);
  Future<Integer> remove(long userId, String omdbId);
  Future<Long> schedule(long userId, String omdbId, Instant scheduledAt);
}
