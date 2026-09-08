package org.cineschedule.repository;

import io.vertx.core.Future;
import org.cineschedule.db.Database;
import org.cineschedule.domain.User;

import java.util.List;
import java.util.stream.Collectors;

public class JdbcUserRepository implements UserRepository {
  private final Database db;

  public JdbcUserRepository(Database db) { this.db = db; }

  @Override
  public Future<List<User>> list() {
    return db.query("SELECT id, name, email, created_at FROM users ORDER BY id")
        .map(rows -> rows.stream().map(User::fromRow).collect(Collectors.toList()));
  }

  @Override
  public Future<User> findById(long id) {
    return db.fetchOne("SELECT id, name, email, created_at FROM users WHERE id = ?", id)
        .map(User::fromRow);
  }

  @Override
  public Future<Long> create(String name, String email) {
    return db.insert("INSERT INTO users(name, email, created_at) VALUES(?, ?, CURRENT_TIMESTAMP())", name, email);
  }

  @Override
  public Future<User> findByEmail(String email) {
    return db.fetchOne("SELECT id, name, email, created_at, password_hash FROM users WHERE email = ?", email)
        .map(User::fromRow);
  }

  @Override
  public Future<Long> createWithPassword(String name, String email, String passwordHash) {
    return db.insert("INSERT INTO users(name, email, password_hash, created_at) VALUES(?, ?, ?, CURRENT_TIMESTAMP())", name, email, passwordHash);
  }
}
