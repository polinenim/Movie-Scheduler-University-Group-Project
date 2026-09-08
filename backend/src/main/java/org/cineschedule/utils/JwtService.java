package org.cineschedule.utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;

import java.util.Map;
import java.util.Objects;

/**
 * Lightweight wrapper around Vert.x JWTAuth to generate and verify tokens.
 */
public class JwtService implements TokenService {
  private final JWTAuth jwtAuth;

  public JwtService(Vertx vertx, String hmacSecret) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(hmacSecret);
    this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions().setAlgorithm("HS256").setBuffer(hmacSecret)));
  }

  /**
   * Generate a signed token. Expires in seconds.
   */
  @Override
  public String generateToken(Map<String, Object> claims, int expiresInSeconds) {
    JsonObject payload = new JsonObject();
    if (claims != null) payload = new JsonObject(claims);
    JWTOptions opts = new JWTOptions().setExpiresInSeconds(expiresInSeconds);
    return jwtAuth.generateToken(payload, opts);
  }

  /**
   * Verify token asynchronously and return the principal (claims) as JsonObject.
   */
  @Override
  public Future<JsonObject> verify(String token) {
    if (token == null || token.isBlank()) return Future.failedFuture("token is null or empty");
    // JWTAuth expects credentials key "token" (TokenCredentials) not "jwt"
    JsonObject authInfo = new JsonObject().put("token", token);
    return jwtAuth.authenticate(authInfo).map(User::principal);
  }

  public JWTAuth provider() { return jwtAuth; }
}
