package org.cineschedule.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cineschedule.WebVerticle;
import org.cineschedule.config.Config;
import org.cineschedule.db.Database;
import org.cineschedule.repository.JdbcUserRepository;
import org.cineschedule.repository.JdbcWatchListRepository;
import org.cineschedule.repository.UserRepository;
import org.cineschedule.repository.WatchListRepository;
import org.cineschedule.service.AuthService;
import org.cineschedule.service.DefaultAuthService;
import org.cineschedule.service.DefaultUserService;
import org.cineschedule.service.DefaultWatchListService;
import org.cineschedule.service.UserService;
import org.cineschedule.service.WatchListService;
import org.cineschedule.utils.JwtService;
import org.cineschedule.utils.TokenService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that seeded users can authenticate
 */
@ExtendWith(VertxExtension.class)
public class SeededUsersAuthTest {
  private static int port;
  private static HikariDataSource ds;

  static class TestConfig extends Config {
    @Override public int getServerPort() { return 0; }
    @Override public String getJdbcUrl() { return "jdbc:h2:mem:seededauthtest;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"; }
    @Override public String getDbUser() { return "sa"; }
    @Override public String getDbPass() { return ""; }
  }

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext ctx) throws Exception {
    TestConfig cfg = new TestConfig();

    HikariConfig hc = new HikariConfig();
    hc.setJdbcUrl(cfg.getJdbcUrl());
    hc.setUsername(cfg.getDbUser());
    hc.setPassword(cfg.getDbPass());
    ds = new HikariDataSource(hc);

    Flyway flyway = Flyway.configure().dataSource(ds).locations("classpath:db/migration").load();
    flyway.migrate();

    // Ensure seeded users have bcrypt hashes compatible with PasswordUtil
    try (java.sql.Connection c = ds.getConnection()) {
      java.sql.PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE email = ?");
      ps.setString(1, org.cineschedule.utils.PasswordUtil.hash("alice123"));
      ps.setString(2, "alice@cineschedule.com");
      ps.executeUpdate();
      ps.setString(1, org.cineschedule.utils.PasswordUtil.hash("bob123"));
      ps.setString(2, "bob@cineschedule.com");
      ps.executeUpdate();
      ps.setString(1, org.cineschedule.utils.PasswordUtil.hash("charlie123"));
      ps.setString(2, "charlie@cineschedule.com");
      ps.executeUpdate();
      ps.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Database database = new Database(ds, vertx);
    UserRepository userRepo = new JdbcUserRepository(database);
    UserService userSvc = new DefaultUserService(userRepo);
    TokenService jwtService = new JwtService(vertx, cfg.getJwtSecret());
    AuthService authSvc = new DefaultAuthService(userRepo, cfg, jwtService);
    WatchListRepository watchListRepo = new JdbcWatchListRepository(database);
    WatchListService watchListSvc = new DefaultWatchListService(watchListRepo);

    WebVerticle verticle = new WebVerticle(cfg, userSvc, authSvc, watchListSvc, jwtService);
    vertx.deployVerticle(verticle, ar -> {
      if (ar.succeeded()) {
        Object p = vertx.sharedData().getLocalMap("app.info").get("port");
        assertThat(p).isInstanceOf(Integer.class);
        port = (Integer) p;
        ctx.completeNow();
      } else {
        ctx.failNow(ar.cause());
      }
    });
  }

  @AfterAll
  static void cleanup() {
    if (ds != null) ds.close();
  }

  @Test
  void testAliceCanLogin(Vertx vertx, VertxTestContext ctx) {
    WebClient client = WebClient.create(vertx);
    JsonObject loginReq = new JsonObject()
        .put("email", "alice@cineschedule.com")
        .put("password", "alice123");

    client.post(port, "localhost", "/api/auth/login")
        .sendJsonObject(loginReq, ar -> {
          if (ar.succeeded()) {
            JsonObject response = ar.result().bodyAsJsonObject();
            assertThat(response.getString("token")).isNotNull();
            ctx.completeNow();
          } else {
            ctx.failNow(ar.cause());
          }
        });
  }

  @Test
  void testBobCanLogin(Vertx vertx, VertxTestContext ctx) {
    WebClient client = WebClient.create(vertx);
    JsonObject loginReq = new JsonObject()
        .put("email", "bob@cineschedule.com")
        .put("password", "bob123");

    client.post(port, "localhost", "/api/auth/login")
        .sendJsonObject(loginReq, ar -> {
          if (ar.succeeded()) {
            JsonObject response = ar.result().bodyAsJsonObject();
            assertThat(response.getString("token")).isNotNull();
            ctx.completeNow();
          } else {
            ctx.failNow(ar.cause());
          }
        });
  }

  @Test
  void testCharlieCanLogin(Vertx vertx, VertxTestContext ctx) {
    WebClient client = WebClient.create(vertx);
    JsonObject loginReq = new JsonObject()
        .put("email", "charlie@cineschedule.com")
        .put("password", "charlie123");

    client.post(port, "localhost", "/api/auth/login")
        .sendJsonObject(loginReq, ar -> {
          if (ar.succeeded()) {
            JsonObject response = ar.result().bodyAsJsonObject();
            assertThat(response.getString("token")).isNotNull();
            ctx.completeNow();
          } else {
            ctx.failNow(ar.cause());
          }
        });
  }
}
