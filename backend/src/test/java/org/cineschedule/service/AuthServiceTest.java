package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.config.Config;
import org.cineschedule.domain.User;
import org.cineschedule.repository.UserRepository;
import org.cineschedule.utils.PasswordHasher;
import org.cineschedule.utils.TokenService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
  static class FakeRepo implements UserRepository {
    private final List<User> users = new CopyOnWriteArrayList<>();
    private long seq = 1;

    @Override
    public Future<List<User>> list() { return Future.succeededFuture(users); }

    @Override
    public Future<User> findById(long id) {
      return Future.succeededFuture(users.stream().filter(u -> id == u.getId()).findFirst().orElse(null));
    }

    @Override
    public Future<User> findByEmail(String email) {
      return Future.succeededFuture(users.stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null));
    }

    @Override
    public Future<Long> create(String name, String email) {
      long id = seq++;
      users.add(new User(id, name, email, Instant.now()));
      return Future.succeededFuture(id);
    }

    @Override
    public Future<Long> createWithPassword(String name, String email, String passwordHash) {
      long id = seq++;
      users.add(new User(id, name, email, Instant.now(), passwordHash));
      return Future.succeededFuture(id);
    }
  }

  static class FakeTokenService implements TokenService {
    @Override
    public String generateToken(java.util.Map<String, Object> claims, int expiresInSeconds) { return "token"; }
    @Override
    public Future<io.vertx.core.json.JsonObject> verify(String token) { return Future.failedFuture("not needed"); }
  }

  static class FakePasswordHasher implements PasswordHasher {
    @Override
    public String hash(String password) { return "hash"; }
    @Override
    public boolean verify(String password, String hash) { return true; }
  }

  @Test
  void rejects_invalid_email() {
    AuthService auth = new DefaultAuthService(new FakeRepo(), new Config(), new FakeTokenService(), new FakePasswordHasher());
    var fut = auth.register("Tester", "bad-email", "secret12");
    assertThat(fut.failed()).isTrue();
    assertThat(fut.cause().getMessage()).contains("invalid email");
  }
}
