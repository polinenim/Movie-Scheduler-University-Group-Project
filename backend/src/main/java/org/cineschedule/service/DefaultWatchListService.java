package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.domain.Movie;
import org.cineschedule.repository.WatchListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class DefaultWatchListService implements WatchListService {
  private static final Logger log = LoggerFactory.getLogger(DefaultWatchListService.class);
  private final WatchListRepository repo;

  public DefaultWatchListService(WatchListRepository repo) { this.repo = repo; }

  @Override
  public Future<List<Movie>> list(long userId) {
    log.info("WatchListService.list start userId={}", userId);
    return repo.listByUser(userId)
        .onSuccess(list -> log.info("WatchListService.list ok userId={} count={}", userId, list.size()))
        .onFailure(err -> log.warn("WatchListService.list fail userId={}: {}", userId, err.getMessage()));
  }

  @Override
  public Future<Long> add(long userId, String omdbId) {
    if (omdbId == null || omdbId.isBlank()) return Future.failedFuture("omdb_id is required");
    return repo.addOrUpdate(userId, omdbId, false, null);
  }

  @Override
  public Future<Integer> markWatched(long userId, String omdbId) {
    return repo.markWatched(userId, omdbId, true, Instant.now());
  }

  @Override
  public Future<Integer> remove(long userId, String omdbId) {
    return repo.remove(userId, omdbId);
  }

  @Override
  public Future<Movie> find(long userId, String omdbId) {
    return repo.findByUserAndOmdb(userId, omdbId);
  }

  @Override
  public Future<Long> schedule(long userId, String omdbId, Instant scheduledAt) {
    if (omdbId == null || omdbId.isBlank()) return Future.failedFuture("omdb_id is required");
    return repo.schedule(userId, omdbId, scheduledAt)
        .onSuccess(id -> log.info("Scheduled movie userId={} omdbId={} id={} at={}", userId, omdbId, id, scheduledAt))
        .onFailure(err -> log.warn("Failed to schedule userId={} omdbId={}: {}", userId, omdbId, err.getMessage()));
  }
}
