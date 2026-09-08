package org.cineschedule.utils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public interface TokenService {
  String generateToken(Map<String, Object> claims, int expiresInSeconds);
  Future<JsonObject> verify(String token);
}
