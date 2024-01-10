package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JsonTest {
  private static final TestObject TEST_OBJECT = new AutoValue_JsonTest_TestObject("foo", 42);
  private static final String TEST_JSON = "{\"string\":\"foo\",\"number\":42}";

  @Test
  void write(@TempDir Path directory) {
    Path file = directory.resolve("test.json");

    Json.write(file, TEST_OBJECT);

    assertThat(file).content(UTF_8).isEqualTo(TEST_JSON);
  }

  @Test
  void writeFailure(@TempDir Path directory) {
    assertThatThrownBy(() -> Json.write(directory, TEST_OBJECT))
        .isInstanceOf(UncheckedIOException.class)
        .hasMessageContaining("Failure writing to '%s'", directory)
        .hasCauseInstanceOf(FileNotFoundException.class);
  }

  @Test
  void read(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("test.json");

    Files.writeString(file, TEST_JSON, UTF_8);

    assertThat(Json.read(file, TestObject.class)).isEqualTo(TEST_OBJECT);
  }

  @Test
  void readFailure(@TempDir Path directory) {
    assertThatThrownBy(() -> Json.read(directory, TestObject.class))
        .isInstanceOf(UncheckedIOException.class)
        .hasMessageContaining("Failure reading from '%s'", directory)
        .hasCauseInstanceOf(FileNotFoundException.class);
  }

  @AutoValue
  @JsonDeserialize(as = AutoValue_JsonTest_TestObject.class)
  abstract static class TestObject {
    abstract String string();

    abstract int number();
  }
}
