package org.cineschedule.web;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
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
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class WebAppTest {
  static class TestConfig extends Config {
    @Override public int getServerPort() { return 0; } // random
    @Override public String getJdbcUrl() { return "jdbc:h2:mem:webapptest;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"; }
    @Override public String getDbUser() { return "sa"; }
    @Override public String getDbPass() { return ""; }
  }

  private static Vertx vertx;
  private static HikariDataSource ds;
  private static int port;

  @BeforeAll
  static void setup() throws Exception {
    vertx = Vertx.vertx();

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
    var deploy = vertx.deployVerticle(() -> verticle, new DeploymentOptions());
    deploy.toCompletionStage().toCompletableFuture().get();

    Object p = vertx.sharedData().getLocalMap("app.info").get("port");
    assertThat(p).isInstanceOf(Integer.class);
    port = (Integer) p;
  }

  @AfterAll
  static void teardown() {
    if (ds != null) ds.close();
    if (vertx != null) vertx.close();
  }

  @Test
  void health_endpoint_works() throws Exception {
    WebClient client = WebClient.create(vertx);
    var fut = client.get(port, "localhost", "/health").as(BodyCodec.string()).send();
    var resp = fut.toCompletionStage().toCompletableFuture().get();
    assertThat(resp.statusCode()).isEqualTo(200);
    assertThat(resp.body()).contains("\"status\":\"ok\"");
  }

  @Test
  void openapi_served() throws Exception {
    WebClient client = WebClient.create(vertx);
    var fut = client.get(port, "localhost", "/openapi.json").as(BodyCodec.string()).send();
    var resp = fut.toCompletionStage().toCompletableFuture().get();
    assertThat(resp.statusCode()).isEqualTo(200);
    assertThat(resp.body()).contains("\"openapi\":");
    assertThat(resp.body()).contains("/api/users");
  }

  @Test
  void users_list_includes_seed_data() throws Exception {
    WebClient client = WebClient.create(vertx);
    var fut = client.get(port, "localhost", "/api/users").as(BodyCodec.string()).send();
    var resp = fut.toCompletionStage().toCompletableFuture().get();
    assertThat(resp.statusCode()).isEqualTo(200);
    assertThat(resp.body()).contains("Alice");
  }
}
