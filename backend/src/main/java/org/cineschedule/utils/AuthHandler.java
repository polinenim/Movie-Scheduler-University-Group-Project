package org.cineschedule.utils;

import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler implements io.vertx.core.Handler<RoutingContext> {
  private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);
  private final TokenService jwt;

  public AuthHandler(TokenService jwt) { this.jwt = jwt; }

  @Override
  public void handle(RoutingContext ctx) {
    String auth = ctx.request().getHeader("Authorization");
    log.debug("AuthHandler: Authorization header present={}", auth != null);
    if (auth == null || !auth.startsWith("Bearer ")) {
      log.warn("Auth denied: missing bearer token path={} remote={}", ctx.request().path(), ctx.request().remoteAddress());
      unauthorized(ctx, "Missing bearer token");
      return;
    }
    String token = auth.substring("Bearer ".length());
    log.debug("AuthHandler: token length={}", token != null ? token.length() : -1);
    log.debug("AuthHandler: token preview='{}'", token != null && token.length() > 20 ? token.substring(0, 20) + "..." : token);
    jwt.verify(token).onSuccess(claims -> {
      Object sub = claims.getValue("sub");
      if (sub == null) {
        log.warn("Auth denied: no sub claim path={} remote={}", ctx.request().path(), ctx.request().remoteAddress());
        unauthorized(ctx, "Invalid token");
        return;
      }
      try {
        long userId = Long.parseLong(sub.toString());
        ctx.put("userId", userId);
        log.info("Auth ok: userId={} path={} remote={}", userId, ctx.request().path(), ctx.request().remoteAddress());
        ctx.next();
      } catch (Exception e) {
        log.warn("Auth denied: invalid sub claim path={} remote={}", ctx.request().path(), ctx.request().remoteAddress());
        unauthorized(ctx, "Invalid token");
      }
    }).onFailure(err -> {
      log.warn("Auth denied: invalid token path={} remote={} reason={}", ctx.request().path(), ctx.request().remoteAddress(), err.getMessage());
      log.debug("AuthHandler verify failure stack:", err);
      unauthorized(ctx, "Invalid token");
    });
  }

  private void unauthorized(RoutingContext ctx, String msg) {
    ctx.response().setStatusCode(401).putHeader("content-type", "application/json").end(new io.vertx.core.json.JsonObject().put("error", msg).encode());
  }
}
