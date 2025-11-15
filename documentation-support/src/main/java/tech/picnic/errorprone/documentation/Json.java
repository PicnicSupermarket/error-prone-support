package tech.picnic.errorprone.documentation;

import java.nio.file.Path;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

/**
 * Utility class that offers mutually consistent JSON serialization and deserialization operations,
 * without further specifying the exact schema used.
 */
final class Json {
  private static final ObjectMapper OBJECT_MAPPER =
      JsonMapper.builder().addModule(new GuavaModule()).build();

  private Json() {}

  static <T> T read(Path path, Class<T> clazz) {
    return OBJECT_MAPPER.readValue(path.toFile(), clazz);
  }

  static <T> void write(Path path, T object) {
    OBJECT_MAPPER.writeValue(path.toFile(), object);
  }
}
