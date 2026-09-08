package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.domain.User;

import java.util.List;

public interface UserService {
  Future<List<User>> list();
  Future<User> get(long id);
  Future<User> create(String name, String email);
}
