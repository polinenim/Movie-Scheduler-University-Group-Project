package org.cineschedule.web;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.cineschedule.service.WatchListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class WatchListController implements Controller {
  private static final Logger log = LoggerFactory.getLogger(WatchListController.class);
  private final WatchListService service;

  public WatchListController(WatchListService service) { this.service = service; }

  @Override
  public void mount(Router router, Vertx vertx) {
    // list a user's watchlist
    router.get("/api/watchlist").handler(ctx -> {
      Long userId = ctx.get("userId");
      if (userId == null) { log.warn("Watchlist list unauthorized: path={} remote={}", ctx.request().path(), ctx.request().remoteAddress()); unauthorized(ctx); return; }
      service.list(userId).onSuccess(list -> {
        JsonArray arr = new JsonArray();
        list.forEach(m -> arr.add(m.toJson()));
        ctx.response().putHeader("content-type", "application/json").end(arr.encode());
      }).onFailure(err -> {
        log.warn("Watchlist list failed userId={}: {}", userId, err.getMessage());
        error(ctx, 500, err.getMessage());
      });
    });
    // schedule a movie watch
      router.post("/api/watchlist/schedule").handler(ctx -> {
          Long userId = ctx.get("userId");
          if (userId == null) {
              log.warn("Watchlist schedule unauthorized: path={} remote={}",
                      ctx.request().path(), ctx.request().remoteAddress());
              unauthorized(ctx);
              return;
          }

          JsonObject body = ctx.body().asJsonObject();
          String omdbId = body != null ? body.getString("omdb_id") : null;
          String scheduledAtStr = body != null ? body.getString("scheduled_at") : null;

          // Parse scheduledAt into a final variable
          final Instant scheduledAt;
          if (scheduledAtStr != null && !scheduledAtStr.isBlank()) {
              Instant temp;
              try {
                  temp = Instant.parse(scheduledAtStr); // ISO 8601 with Z
              } catch (Exception e) {
                  try {
                      LocalDateTime ldt = LocalDateTime.parse(scheduledAtStr); // no zone
                      temp = ldt.atZone(ZoneId.systemDefault()).toInstant();
                  } catch (Exception ex) {
                      error(ctx, 400, "scheduled_at must be ISO-8601 (e.g. 2025-12-31T20:00:00Z)");
                      return;
                  }
              }
              scheduledAt = temp;
          } else {
              scheduledAt = null;
          }

          service.schedule(userId, omdbId, scheduledAt)
                  .onSuccess(id -> {
                      ctx.response()
                              .setStatusCode(201)
                              .putHeader("content-type", "application/json")
                              .end(
                                      new JsonObject()
                                              .put("id", id)
                                              .put("scheduled_at", scheduledAt != null ? scheduledAt.toString() : null)
                                              .encode()
                              );
                  })
                  .onFailure(err -> {
                      log.warn("Watchlist schedule failed userId={}: {}", userId, err.getMessage());
                      error(ctx, 400, err.getMessage());
                  });
      });

    // add a movie to watchlist
    router.post("/api/watchlist").handler(ctx -> {
      Long userId = ctx.get("userId");
      if (userId == null) { log.warn("Watchlist add unauthorized: path={} remote={}", ctx.request().path(), ctx.request().remoteAddress()); unauthorized(ctx); return; }
      JsonObject body = ctx.body().asJsonObject();
      String omdbId = body != null ? body.getString("omdb_id") : null;
      service.add(userId, omdbId).onSuccess(id -> {
        ctx.response().setStatusCode(201).putHeader("content-type", "application/json").end(new JsonObject().put("id", id).encode());
      }).onFailure(err -> {
        log.warn("Watchlist add failed userId={}: {}", userId, err.getMessage());
        error(ctx, 400, err.getMessage());
      });
    });

    // mark watched
    router.post("/api/watchlist/watch").handler(ctx -> {
      Long userId = ctx.get("userId");
      if (userId == null) { log.warn("Watchlist watch unauthorized: path={} remote={}", ctx.request().path(), ctx.request().remoteAddress()); unauthorized(ctx); return; }
      JsonObject body = ctx.body().asJsonObject();
      String omdbId = body != null ? body.getString("omdb_id") : null;
      service.markWatched(userId, omdbId).onSuccess(rows -> {
        ctx.response().putHeader("content-type", "application/json").end(new JsonObject().put("updated", rows).encode());
      }).onFailure(err -> {
        log.warn("Watchlist watch failed userId={}: {}", userId, err.getMessage());
        error(ctx, 400, err.getMessage());
      });
    });

    // remove
    router.delete("/api/watchlist").handler(ctx -> {
      Long userId = ctx.get("userId");
      if (userId == null) { log.warn("Watchlist delete unauthorized: path={} remote={}", ctx.request().path(), ctx.request().remoteAddress()); unauthorized(ctx); return; }
      String omdbId = ctx.request().getParam("omdb_id");
      service.remove(userId, omdbId).onSuccess(rows -> {
        ctx.response().putHeader("content-type", "application/json").end(new JsonObject().put("deleted", rows).encode());
      }).onFailure(err -> {
        log.warn("Watchlist delete failed userId={}: {}", userId, err.getMessage());
        error(ctx, 400, err.getMessage());
      });
    });
  }

  private void error(io.vertx.ext.web.RoutingContext ctx, int code, String msg) { ctx.response().setStatusCode(code).putHeader("content-type", "application/json").end(new JsonObject().put("error", msg).encode()); }
  private void unauthorized(io.vertx.ext.web.RoutingContext ctx) { error(ctx, 401, "Unauthorized"); }
}

