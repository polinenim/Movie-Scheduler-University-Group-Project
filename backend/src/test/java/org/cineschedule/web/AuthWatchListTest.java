package org.cineschedule.web;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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

public class AuthWatchListTest {
  static class TestConfig extends Config {
    @Override public int getServerPort() { return 0; }
    @Override public String getJdbcUrl() { return "jdbc:h2:mem:autotest;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"; }
    @Override public String getDbUser() { return "sa"; }
    @Override public String getDbPass() { return ""; }
    @Override public String getJwtSecret() { return "test-secret"; }
    @Override public int getJwtExpMinutes() { return 60; }
  }

  private static Vertx vertx;
  private static HikariDataSource ds;
  private static int port;
  private static TokenService tokenService;

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

    Database database = new Database(ds, vertx);
    UserRepository userRepo = new JdbcUserRepository(database);
    UserService userSvc = new DefaultUserService(userRepo);
    TokenService jwtService = new JwtService(vertx, cfg.getJwtSecret());
    AuthService authSvc = new DefaultAuthService(userRepo, cfg, jwtService);
    AuthWatchListTest.tokenService = jwtService;
    WatchListRepository watchListRepo = new JdbcWatchListRepository(database);
    WatchListService watchListSvc = new DefaultWatchListService(watchListRepo);

    WebVerticle verticle = new WebVerticle(cfg, userSvc, authSvc, watchListSvc, jwtService);
    var deploy = vertx.deployVerticle(() -> verticle, new DeploymentOptions());
    deploy.toCompletionStage().toCompletableFuture().get();

    Object p = vertx.sharedData().getLocalMap("app.info").get("port");
    port = (Integer) p;
  }

  @AfterAll
  static void teardown() {
    if (ds != null) ds.close();
    if (vertx != null) vertx.close();
  }

  @Test
  void register_login_and_use_protected_watchlist() throws Exception {
    WebClient client = WebClient.create(vertx);

    // register
    JsonObject reg = new JsonObject().put("name", "Tester").put("email", "tester@cineschedule.com").put("password", "secret12");
    var regResp = client.post(port, "localhost", "/api/auth/register").as(BodyCodec.jsonObject()).sendJsonObject(reg).toCompletionStage().toCompletableFuture().get();
    assertThat(regResp.statusCode()).isEqualTo(201);
    JsonObject created = regResp.body();
    String token = created.getString("token");
    Long createdId = created.getJsonObject("user").getLong("id");
    assertThat(token).isNotBlank();

    // add movie to watchlist
    JsonObject watchReq = new JsonObject().put("omdb_id", "tt0111161");
    var addResp = client.post(port, "localhost", "/api/watchlist")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonObject())
        .sendJsonObject(watchReq)
        .toCompletionStage().toCompletableFuture().get();
    assertThat(addResp.statusCode()).isEqualTo(201);
    assertThat(addResp.body().getLong("id")).isNotNull();

    // list watchlist and confirm entry exists
    var listResp = client.get(port, "localhost", "/api/watchlist")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonArray())
        .send()
        .toCompletionStage().toCompletableFuture().get();
    assertThat(listResp.statusCode()).isEqualTo(200);
    JsonArray current = listResp.body();
    assertThat(current).hasSize(1);
    JsonObject movie = current.getJsonObject(0);
    assertThat(movie.getString("omdb_id")).isEqualTo("tt0111161");
    assertThat(movie.getBoolean("watched")).isFalse();

    // mark watched
    var watchResp = client.post(port, "localhost", "/api/watchlist/watch")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonObject())
        .sendJsonObject(watchReq)
        .toCompletionStage().toCompletableFuture().get();
    assertThat(watchResp.statusCode()).isEqualTo(200);
    assertThat(watchResp.body().getInteger("updated")).isEqualTo(1);

    // verify watchlist shows watched=true
    var watchedList = client.get(port, "localhost", "/api/watchlist")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonArray())
        .send()
        .toCompletionStage().toCompletableFuture().get();
    JsonObject watchedMovie = watchedList.body().getJsonObject(0);
    assertThat(watchedMovie.getBoolean("watched")).isTrue();
    assertThat(watchedMovie.getString("watched_at")).isNotBlank();

    // remove movie from watchlist
    var deleteResp = client.delete(port, "localhost", "/api/watchlist")
        .addQueryParam("omdb_id", "tt0111161")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonObject())
        .send()
        .toCompletionStage().toCompletableFuture().get();
    assertThat(deleteResp.statusCode()).isEqualTo(200);
    assertThat(deleteResp.body().getInteger("deleted")).isEqualTo(1);

    // final list should be empty
    var finalList = client.get(port, "localhost", "/api/watchlist")
        .putHeader("Authorization", "Bearer " + token)
        .as(BodyCodec.jsonArray())
        .send()
        .toCompletionStage().toCompletableFuture().get();
    assertThat(finalList.body()).isEmpty();
  }
}
