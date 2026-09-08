package org.cineschedule.domain;

import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class User {
  private Long id;
  private String name;
  private String email;
  private Instant createdAt;
  private String passwordHash;

  public User() {}

  public User(Long id, String name, String email, Instant createdAt) {
    this(id, name, email, createdAt, null);
  }

  public User(Long id, String name, String email, Instant createdAt, String passwordHash) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.createdAt = createdAt;
    this.passwordHash = passwordHash;
  }

  public static User fromRow(JsonObject row) {
    if (row == null) return null;
    Long id = row.getLong("ID") != null ? row.getLong("ID") : row.getLong("id");
    String name = row.getString("NAME") != null ? row.getString("NAME") : row.getString("name");
    String email = row.getString("EMAIL") != null ? row.getString("EMAIL") : row.getString("email");
    Object ca = row.getValue("CREATED_AT") != null ? row.getValue("CREATED_AT") : row.getValue("created_at");
    Instant createdAt = parseInstant(ca);
    String passwordHash = row.getString("PASSWORD_HASH") != null ? row.getString("PASSWORD_HASH") : row.getString("password_hash");
    return new User(id, name, email, createdAt, passwordHash);
  }

  private static Instant parseInstant(Object ca) {
    if (ca == null) return null;
    try {
      if (ca instanceof Instant) return (Instant) ca;
      if (ca instanceof java.sql.Timestamp) return ((java.sql.Timestamp) ca).toInstant();
      if (ca instanceof LocalDateTime) return ((LocalDateTime) ca).atZone(ZoneId.systemDefault()).toInstant();
      // String or other types
      return Instant.parse(ca.toString());
    } catch (Exception e) {
      return null;
    }
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("id", id)
        .put("name", name)
        .put("email", email)
        .put("created_at", createdAt != null ? createdAt.toString() : null);
  }

  // getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
