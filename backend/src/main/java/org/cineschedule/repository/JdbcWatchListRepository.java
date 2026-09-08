package org.cineschedule.repository;

import io.vertx.core.Future;
import org.cineschedule.db.Database;
import org.cineschedule.domain.Movie;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcWatchListRepository implements WatchListRepository {
  private final Database db;

  public JdbcWatchListRepository(Database db) { this.db = db; }

  @Override
  public Future<List<Movie>> listByUser(long userId) {
    return db.query("SELECT id, user_id, omdb_id, watched, watched_at, scheduled_at, added_at FROM watchlist WHERE user_id = ? ORDER BY id", userId)
        .map(rows -> rows.stream().map(Movie::fromJson).collect(Collectors.toList()));
  }

  @Override
  public Future<Long> addOrUpdate(long userId, String omdbId, boolean watched, Instant watchedAt) {
    String sql = "INSERT INTO watchlist (user_id, omdb_id, watched, watched_at, added_at) " +
        "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP) " +
        "ON DUPLICATE KEY UPDATE watched = VALUES(watched), watched_at = VALUES(watched_at)";
    return db.executeUpdate(sql, userId, omdbId, watched, toTimestamp(watchedAt))
        .compose(_v -> db.fetchOne("SELECT id FROM watchlist WHERE user_id = ? AND omdb_id = ?", userId, omdbId))
        .map(j -> j != null ? j.getLong("id") : null);
  }

  @Override
  public Future<Integer> markWatched(long userId, String omdbId, boolean watched, Instant watchedAt) {
    return db.executeUpdate("UPDATE watchlist SET watched = ?, watched_at = ? WHERE user_id = ? AND omdb_id = ?", watched, toTimestamp(watchedAt), userId, omdbId);
  }

  @Override
  public Future<Movie> findByUserAndOmdb(long userId, String omdbId) {
    return db.fetchOne("SELECT id, user_id, omdb_id, watched, watched_at, scheduled_at, added_at FROM watchlist WHERE user_id = ? AND omdb_id = ?", userId, omdbId)
        .map(Movie::fromJson);
  }

  @Override
  public Future<Integer> remove(long userId, String omdbId) {
    return db.executeUpdate("DELETE FROM watchlist WHERE user_id = ? AND omdb_id = ?", userId, omdbId);
  }

  @Override
  public Future<Long> schedule(long userId, String omdbId, Instant scheduledAt) {
    String sql = "INSERT INTO watchlist (user_id, omdb_id, watched, watched_at, scheduled_at, added_at) " +
        "VALUES(?, ?, false, NULL, ?, CURRENT_TIMESTAMP) " +
        "ON DUPLICATE KEY UPDATE scheduled_at = VALUES(scheduled_at)";
    return db.executeUpdate(sql, userId, omdbId, toTimestamp(scheduledAt))
        .compose(_v -> db.fetchOne("SELECT id FROM watchlist WHERE user_id = ? AND omdb_id = ?", userId, omdbId))
        .map(j -> j != null ? j.getLong("id") : null);
  }

  private Timestamp toTimestamp(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
