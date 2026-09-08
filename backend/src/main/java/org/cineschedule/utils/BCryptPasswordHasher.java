package org.cineschedule.utils;

public class BCryptPasswordHasher implements PasswordHasher {
  @Override
  public String hash(String password) {
    return PasswordUtil.hash(password);
  }

  @Override
  public boolean verify(String password, String hash) {
    return PasswordUtil.verify(password, hash);
  }
}
