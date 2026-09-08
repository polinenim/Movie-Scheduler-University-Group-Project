package org.cineschedule.service;

import io.vertx.core.Future;
import org.cineschedule.config.Config;
import org.cineschedule.domain.User;
import org.cineschedule.repository.UserRepository;
import org.cineschedule.utils.PasswordHasher;
import org.cineschedule.utils.BCryptPasswordHasher;
import org.cineschedule.utils.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultAuthService implements AuthService {
  private static final Logger log = LoggerFactory.getLogger(DefaultAuthService.class);
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private final UserRepository users;
  private final Config config;
  private final TokenService tokens;
  private final PasswordHasher passwordHasher;

  public DefaultAuthService(UserRepository users, Config config, TokenService tokens, PasswordHasher passwordHasher) {
    this.users = users;
    this.config = config;
    this.tokens = tokens;
    this.passwordHasher = passwordHasher;
  }

  public DefaultAuthService(UserRepository users, Config config, TokenService tokens) {
    this(users, config, tokens, new BCryptPasswordHasher());
  }

  @Override
  public Future<AuthResult> register(String name, String email, String password) {
    log.info("Register start: email={}", email);
    if (name == null || name.isBlank()) return Future.failedFuture("name is required");
    if (email == null || email.isBlank()) return Future.failedFuture("email is required");
    if (!isValidEmail(email)) return Future.failedFuture("invalid email");
    if (password == null || password.length() < 6) return Future.failedFuture("password must be at least 6 chars");
    return users.findByEmail(email).compose(existing -> {
      if (existing != null) {
        log.warn("Register failed: email already in use: {}", email);
        return Future.failedFuture("email already in use");
      }
      String hash = passwordHasher.hash(password);
      log.debug("Register creating user: email={} (hashLen={})", email, hash.length());
      return users.createWithPassword(name, email, hash)
          .compose(id -> users.findByEmail(email))
          .compose(user -> {
            if (user == null || user.getId() == null) return Future.failedFuture("user creation failed");
            AuthResult result = new AuthResult(user, issueToken(user.getId()));
            return Future.succeededFuture(result);
          })
          .onSuccess(result -> log.info("Register success: email={} id={}", email, result != null && result.user() != null ? result.user().getId() : null))
          .onFailure(err -> log.warn("Register DB failure for {}: {}", email, err.getMessage()));
    });
  }

  @Override
  public Future<String> login(String email, String password) {
    log.info("Login start: email={}", email);
    if (email == null || password == null) return Future.failedFuture("invalid credentials");
    return users.findByEmail(email).compose(user -> {
      if (user == null) {
        log.warn("Login failed: user not found: {}", email);
        return Future.failedFuture("invalid credentials");
      }
      String stored = user.getPasswordHash();
      if (stored == null) {
        log.warn("Login failed: no password set for {} (seeded user?)", email);
        return Future.failedFuture("invalid credentials");
      }
      boolean ok = passwordHasher.verify(password, stored);

      if (!ok) {
        log.warn("Login failed: bad password for {}", email);
        return Future.failedFuture("invalid credentials");
      }
      Long id = user.getId();
      if (id == null) {
        log.warn("Login failed: user id missing for {}", email);
        return Future.failedFuture("invalid credentials");
      }
      String token = issueToken(id);
      log.info("Login success: email={} sub={}", email, id);
      return Future.succeededFuture(token);
    });
  }

  private boolean isValidEmail(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  private String issueToken(long userId) {
    long exp = Instant.now().plus(config.getJwtExpMinutes(), ChronoUnit.MINUTES).getEpochSecond();
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", String.valueOf(userId));
    claims.put("exp", exp);
    int expSeconds = (int) (config.getJwtExpMinutes() * 60L);
    return tokens.generateToken(claims, expSeconds);
  }
}
