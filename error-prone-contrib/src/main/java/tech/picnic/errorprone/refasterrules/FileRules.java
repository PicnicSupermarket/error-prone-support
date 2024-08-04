package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
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

  /**
   * Prefer {@link Files#createTempFile(String, String, FileAttribute[])} over alternatives that
   * create files with more liberal permissions.
   */
  static final class FilesCreateTempFileToFile {
    @BeforeTemplate
    @SuppressWarnings("java:S5443" /* This violation will be rewritten. */)
    File before(String prefix, String suffix) throws IOException {
      return Refaster.anyOf(
          File.createTempFile(prefix, suffix), File.createTempFile(prefix, suffix, null));
    }

    @AfterTemplate
    File after(String prefix, String suffix) throws IOException {
      return Files.createTempFile(prefix, suffix).toFile();
    }
  }
}
