package org.cineschedule;

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
import org.cineschedule.utils.BCryptPasswordHasher;
import org.cineschedule.utils.JwtService;
import org.cineschedule.utils.PasswordHasher;
import org.cineschedule.utils.TokenService;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class MainApp {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        Config config = new Config();

        HikariDataSource dataSource = createDataSource(config);

        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        Vertx vertx = Vertx.vertx();
        Database database = new Database(dataSource, vertx);

        UserRepository userRepository = new JdbcUserRepository(database);
        UserService userService = new DefaultUserService(userRepository);

        TokenService tokenService = new JwtService(vertx, config.getJwtSecret());
        PasswordHasher passwordHasher = new BCryptPasswordHasher();

        AuthService authService = new DefaultAuthService(userRepository, config, tokenService, passwordHasher);


        // Watchlist wiring
        WatchListRepository watchListRepository = new JdbcWatchListRepository(database);
        WatchListService watchListService = new DefaultWatchListService(watchListRepository);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            vertx.close();
            dataSource.close();
        }));

        DeploymentOptions options = new DeploymentOptions();
        vertx.deployVerticle(() -> new WebVerticle(config, userService, authService, watchListService, tokenService), options, res -> {
            if (res.succeeded()) {
                log.info("WebVerticle deployed: {}", res.result());
            } else {
                log.error("Failed to deploy WebVerticle", res.cause());
                vertx.close();
                dataSource.close();
            }
        });
    }

    private static HikariDataSource createDataSource(Config config) {
        String dbUrl = config.getJdbcUrl();
        boolean isMySql = dbUrl.startsWith("jdbc:mysql");

        // Try to connect to the configured database
        try {
            HikariConfig hcfg = new HikariConfig();
            hcfg.setJdbcUrl(dbUrl);
            hcfg.setUsername(config.getDbUser());
            hcfg.setPassword(config.getDbPass());
            hcfg.setMaximumPoolSize(10);
            hcfg.setConnectionTimeout(5000); // 5 seconds timeout
            // Explicitly load the MySQL driver when using MySQL to avoid "driver not found" issues
            if (isMySql) {
                hcfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
            }

            HikariDataSource ds = new HikariDataSource(hcfg);
            // Test the connection
            ds.getConnection().close();
            log.info("Connected to database: {}", dbUrl);
            return ds;
        } catch (Exception e) {
            log.warn("Failed to connect to configured database ({}): {}", dbUrl, e.getMessage());
            log.info("Falling back to H2 file database: ./data/demodb");

            // Fallback to H2 file database
            HikariConfig fallbackCfg = new HikariConfig();
            fallbackCfg.setJdbcUrl("jdbc:h2:file:./data/demodb;AUTO_SERVER=TRUE");
            fallbackCfg.setUsername("sa");
            fallbackCfg.setPassword("");
            fallbackCfg.setMaximumPoolSize(10);
            return new HikariDataSource(fallbackCfg);
        }
    }
}
