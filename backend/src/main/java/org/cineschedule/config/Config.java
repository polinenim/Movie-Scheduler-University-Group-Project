package org.cineschedule.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
  private final Properties props = new Properties();

  public Config() {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
      if (in != null) props.load(in);
    } catch (IOException ignored) {}
  }

  private String prop(String key, String def) {
    String env = System.getenv(key.toUpperCase().replace('.', '_'));
    if (env != null && !env.isBlank()) return env;
    return props.getProperty(key, def);
  }

  public int getServerPort() { return Integer.parseInt(prop("server.port", "8080")); }

  public String getJdbcUrl() { return prop("jdbc.url", "jdbc:h2:file:./data/demodb;AUTO_SERVER=TRUE"); }
  public String getDbUser() { return prop("db.user", "sa"); }
  public String getDbPass() { return prop("db.pass", ""); }

  public String getJwtSecret() { return prop("jwt.secret", "dev-secret-change-me"); }
  public int getJwtExpMinutes() { return Integer.parseInt(prop("jwt.exp.minutes", "60")); }
}
