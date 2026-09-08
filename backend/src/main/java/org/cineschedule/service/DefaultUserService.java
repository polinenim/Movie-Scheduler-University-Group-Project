package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.domain.User;
import org.cineschedule.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultUserService implements UserService {
  private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);
  private final UserRepository repo;

  public DefaultUserService(UserRepository repo) { this.repo = repo; }

  @Override
  public Future<List<User>> list() {
    log.info("UserService.list start");
    return repo.list()
        .onSuccess(list -> log.info("UserService.list ok count={}", list.size()))
        .onFailure(err -> log.warn("UserService.list fail: {}", err.getMessage()));
  }

  @Override
  public Future<User> get(long id) {
    log.info("UserService.get start id={}", id);
    return repo.findById(id)
        .onSuccess(u -> log.info("UserService.get ok id={} found={}", id, u != null))
        .onFailure(err -> log.warn("UserService.get fail id={}: {}", id, err.getMessage()));
  }

  @Override
  public Future<User> create(String name, String email) {
    log.info("UserService.create start name={} email={}", name, email);
    if (name == null || name.isBlank()) return Future.failedFuture("name is required");
    if (email == null || email.isBlank()) return Future.failedFuture("email is required");
    return repo.create(name, email)
        .compose(repo::findById)
        .onSuccess(u -> log.info("UserService.create ok id={}", u != null ? u.getId() : null))
        .onFailure(err -> log.warn("UserService.create fail email={}: {}", email, err.getMessage()));
  }
}
