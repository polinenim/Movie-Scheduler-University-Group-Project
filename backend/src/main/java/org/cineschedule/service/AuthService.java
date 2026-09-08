package org.cineschedule.service;

import io.vertx.core.Future;

public interface AuthService {
  Future<AuthResult> register(String name, String email, String password);
  Future<String> login(String email, String password);
}
