package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with files. */
@OnlineDocumentation
final class FileRules {
  private FileRules() {}

  /** Prefer {@link Files#readString(Path, Charset)} over more contrived alternatives. */
  static final class FilesReadStringWithCharset {
    @BeforeTemplate
    String before(Path path, Charset charset) throws IOException {
      return new String(Files.readAllBytes(path), charset);
    }

    @AfterTemplate
    String after(Path path, Charset charset) throws IOException {
      return Files.readString(path, charset);
    }
  }

  /** Prefer {@link Files#readString(Path)} over more verbose alternatives. */
  static final class FilesReadString {
    @BeforeTemplate
    String before(Path path) throws IOException {
      return Files.readString(path, UTF_8);
    }

    @AfterTemplate
    String after(Path path) throws IOException {
      return Files.readString(path);
    }
  }
}
