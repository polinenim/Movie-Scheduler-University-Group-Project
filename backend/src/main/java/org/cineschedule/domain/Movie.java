package org.cineschedule.domain;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Movie {
  private Long id;
  private String omdbId; // OMDB's imdbID value
  private boolean watched;
  private Instant watchedAt;
  private Instant scheduledAt;

  public Movie() {}

  public Movie(Long id, String omdbId) {
    this.id = id;
    this.omdbId = omdbId;
  }
public Movie(Long id, String omdbId, boolean watched, Instant watchedAt) {
    this(id, omdbId, watched, watchedAt, null);
}
  public Movie(Long id, String omdbId, boolean watched, Instant watchedAt, Instant scheduledAt) {
    this.id = id;
    this.omdbId = omdbId;
    this.watched = watched;
    this.watchedAt = watchedAt;
    this.scheduledAt = scheduledAt;
  }

  public static Movie fromRow(JsonObject row) {
    // DB rows come as JsonObject in this project; reuse fromJson
    return fromJson(row);
  }

  public static Movie fromJson(JsonObject obj) {
    if (obj == null) return null;
    Movie m = new Movie();
    m.id = obj.getLong("ID") != null ? obj.getLong("ID") : obj.getLong("id");
    // Accept OMDB/IMDB id column names to be tolerant
    m.omdbId = obj.getString("OMDB_ID") != null ? obj.getString("OMDB_ID") : obj.getString("omdb_id");
    if (m.omdbId == null) {
      m.omdbId = obj.getString("IMDB_ID") != null ? obj.getString("IMDB_ID") : obj.getString("imdb_id");
    }
    Object w = obj.getValue("WATCHED") != null ? obj.getValue("WATCHED") : obj.getValue("watched");
    m.watched = w instanceof Boolean ? (Boolean) w : (w != null && Integer.parseInt(w.toString()) != 0);
    Object wa = obj.getValue("WATCHED_AT") != null ? obj.getValue("WATCHED_AT") : obj.getValue("watched_at");
    m.watchedAt = parseInstant(wa);

    Object sa = obj.getValue("SCHEDULED_AT") != null ? obj.getValue("SCHEDULED_AT") : obj.getValue("scheduled_at");
    m.scheduledAt = parseInstant(sa);

    return m;
  }

  private static Instant parseInstant(Object ca) {
    if (ca == null) return null;
    try {
      if (ca instanceof Instant) return (Instant) ca;
      if (ca instanceof java.sql.Timestamp) return ((java.sql.Timestamp) ca).toInstant();
      if (ca instanceof LocalDateTime) return ((LocalDateTime) ca).atZone(ZoneId.systemDefault()).toInstant();
      return Instant.parse(ca.toString());
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Mark this movie as watched (sets watched=true and watchedAt). If when==null uses now().
   */
  public void markWatched(Instant when) {
    this.watched = true;
    this.watchedAt = when != null ? when : Instant.now();
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("id", id)
        .put("omdb_id", omdbId)
        .put("watched", watched)
        .put("watched_at", watchedAt != null ? watchedAt.toString() : null)
        .put("scheduled_at", scheduledAt != null ? scheduledAt.toString() : null);
  }

  // getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getOmdbId() { return omdbId; }
  public boolean isWatched() { return watched; }
  public Instant getWatchedAt() { return watchedAt; }
  public Instant getScheduledAt() { return scheduledAt; }
  public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
}
