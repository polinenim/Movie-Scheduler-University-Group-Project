package org.cineschedule.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WatchList {
  private Long id;
  private Long userId;
  private List<Movie> movies; // list of movies (with watched flag)
  private Instant createdAt;

  public WatchList() {
    this.movies = new ArrayList<>();
  }

  public WatchList(Long id, Long userId, List<Movie> movies, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.movies = movies != null ? movies : new ArrayList<>();
    this.createdAt = createdAt;
  }

  /**
   * Construct from a JSON representation. Expected shape:
   * { "id": ..., "user_id": ..., "created_at": ..., "movies": [ {..entry..}, ... ] }
   */
  public static WatchList fromJson(JsonObject obj) {
    if (obj == null) return null;
    WatchList wl = new WatchList();
    wl.id = obj.getLong("id") != null ? obj.getLong("id") : obj.getLong("ID");
    wl.userId = obj.getLong("user_id") != null ? obj.getLong("user_id") : obj.getLong("USER_ID");
    Object ca = obj.getValue("created_at") != null ? obj.getValue("created_at") : obj.getValue("CREATED_AT");
    wl.createdAt = parseInstant(ca);

    JsonArray arr = obj.getJsonArray("movies") != null ? obj.getJsonArray("movies") : obj.getJsonArray("items");
    if (arr != null) {
      List<Movie> list = new ArrayList<>();
      for (int i = 0; i < arr.size(); i++) {
        Object el = arr.getValue(i);
        if (el instanceof JsonObject) {
          Movie m = Movie.fromJson((JsonObject) el);
          if (m != null) list.add(m);
        }
      }
      wl.movies = list;
    }
    return wl;
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

  public JsonObject toJson() {
    JsonObject o = new JsonObject()
        .put("id", id)
        .put("user_id", userId)
        .put("created_at", createdAt != null ? createdAt.toString() : null);
    JsonArray arr = new JsonArray();
    if (movies != null) {
      for (Movie m : movies) {
        arr.add(m.toJson());
      }
    }
    o.put("movies", arr);
    return o;
  }

  // getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public List<Movie> getMovies() { return movies == null ? Collections.emptyList() : movies; }
  public void setMovies(List<Movie> movies) { this.movies = movies; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
