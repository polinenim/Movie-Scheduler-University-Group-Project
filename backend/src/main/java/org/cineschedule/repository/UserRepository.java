package org.cineschedule.repository;

import io.vertx.core.Future;
import org.cineschedule.domain.User;

import java.util.List;

public interface UserRepository {
  Future<List<User>> list();
  Future<User> findById(long id);
  Future<User> findByEmail(String email);
  Future<Long> create(String name, String email);
  Future<Long> createWithPassword(String name, String email, String passwordHash);
}
