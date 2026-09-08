package org.cineschedule.domain;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieTest {
  @Test
  public void jsonRoundtrip() {
    Movie m = new Movie(1L, "tt1234567", false, null);
    JsonObject j = m.toJson();
    assertThat(j.getLong("id")).isEqualTo(1L);
    assertThat(j.getString("omdb_id")).isEqualTo("tt1234567");
    assertThat(j.getBoolean("watched")).isFalse();

    // simulate DB/JSON with watched fields
    JsonObject in = new JsonObject().put("id", 2).put("omdb_id", "tt7654321").put("watched", true).put("watched_at", Instant.now().toString());
    Movie m2 = Movie.fromJson(in);
    assertThat(m2).isNotNull();
    assertThat(m2.getOmdbId()).isEqualTo("tt7654321");
    assertThat(m2.isWatched()).isTrue();
    assertThat(m2.getWatchedAt()).isNotNull();
  }
}

