package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.domain.User;
import org.cineschedule.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {
  static class FakeRepo implements UserRepository {
    List<User> store = new ArrayList<>();
    long idSeq = 1;

    @Override
    public Future<List<User>> list() { return Future.succeededFuture(new ArrayList<>(store)); }

    @Override
    public Future<User> findById(long id) {
      return Future.succeededFuture(store.stream().filter(j -> j.getId() == id).findFirst().orElse(null));
    }

    @Override
    public Future<User> findByEmail(String email) {
      return Future.succeededFuture(store.stream().filter(j -> email.equals(j.getEmail())).findFirst().orElse(null));
    }

    @Override
    public Future<Long> create(String name, String email) {
      long id = idSeq++;
      store.add(new User(id, name, email, Instant.parse("2024-01-01T00:00:00Z")));
      return Future.succeededFuture(id);
    }

    @Override
    public Future<Long> createWithPassword(String name, String email, String passwordHash) {
      long id = idSeq++;
      store.add(new User(id, name, email, Instant.parse("2024-01-01T00:00:00Z"), passwordHash));
      return Future.succeededFuture(id);
    }
  }

  @Test
  void validate_and_create_user() {
    FakeRepo repo = new FakeRepo();
    UserService svc = new DefaultUserService(repo);

    var f1 = svc.create("", "x@x");
    assertThat(f1.failed()).isTrue();

    var f2 = svc.create("Alice", "");
    assertThat(f2.failed()).isTrue();

    var f3 = svc.create("Alice", "alice@cineschedule.com").result();
    assertThat(f3.getId()).isEqualTo(1L);

    var list = svc.list().result();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("Alice");
  }
}
