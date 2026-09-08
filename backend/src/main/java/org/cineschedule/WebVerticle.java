package org.cineschedule;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.cineschedule.service.AuthService;
import org.cineschedule.config.Config;
import org.cineschedule.service.UserService;
import org.cineschedule.service.WatchListService;
import org.cineschedule.utils.AuthHandler;
import org.cineschedule.utils.TokenService;
import org.cineschedule.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  private final Config config;
  private final UserService userService;
  private final AuthService authService;
  private final WatchListService watchListService;
  private final TokenService tokenService;

  public WebVerticle(Config config, UserService userService, AuthService authService, WatchListService watchListService, TokenService tokenService) {
    this.config = config;
    this.userService = userService;
    this.authService = authService;
    this.watchListService = watchListService;
    this.tokenService = tokenService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    CorsHandler cors = CorsHandler.create()
        .addOrigin("*")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.PUT)
        .allowedMethod(HttpMethod.PATCH)
        .allowedMethod(HttpMethod.DELETE)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Authorization")
        .allowedHeader("Content-Type")
        .allowedHeader("Accept");
    router.route().handler(cors);

    // Public controllers
    new HealthController().mount(router, vertx);
    new UsersController(userService).mount(router, vertx);
    new AuthController(authService).mount(router, vertx);

    // Protected routes: JWT required
    // Ensure both exact path and sub-paths are protected
    AuthHandler authHandler = new AuthHandler(tokenService);
    router.route("/api/watchlist").handler(authHandler);
    router.route("/api/watchlist/*").handler(authHandler);
    new WatchListController(watchListService).mount(router, vertx);

    int desiredPort = config.getServerPort();
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router).listen(desiredPort, ar -> {
      if (ar.succeeded()) {
        int actualPort = ar.result().actualPort();
        String serverUrl = "http://localhost:" + actualPort;
        new OpenApiBridge(serverUrl).mount(router, vertx);
        vertx.sharedData().getLocalMap("app.info").put("port", actualPort);
        log.info("HTTP server running on port {}", actualPort);
        log.info("API docs available at {}/docs", serverUrl);
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
