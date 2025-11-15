package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JsonTest {
  private static final TestObject TEST_OBJECT = new TestObject("foo", 42);
  private static final String TEST_JSON = "{\"string\":\"foo\",\"number\":42}";

  @Test
  void write(@TempDir Path directory) {
    Path file = directory.resolve("test.json");

    Json.write(file, TEST_OBJECT);

    assertThat(file).content(UTF_8).isEqualTo(TEST_JSON);
  }

  @Test
  void read(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("test.json");

    Files.writeString(file, TEST_JSON, UTF_8);

    assertThat(Json.read(file, TestObject.class)).isEqualTo(TEST_OBJECT);
  }

  private record TestObject(String string, int number) {}
}
