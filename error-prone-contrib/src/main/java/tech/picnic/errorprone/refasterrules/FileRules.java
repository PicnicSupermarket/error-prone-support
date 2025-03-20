package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with files. */
@OnlineDocumentation
final class FileRules {
  private FileRules() {}

  /** Prefer the more idiomatic {@link Path#of(URI)} over {@link Paths#get(URI)}. */
  static final class PathOfUri {
    @BeforeTemplate
    Path before(URI uri) {
      return Paths.get(uri);
    }

    @AfterTemplate
    Path after(URI uri) {
      return Path.of(uri);
    }
  }

  /**
   * Prefer the more idiomatic {@link Path#of(String, String...)} over {@link Paths#get(String,
   * String...)}.
   */
  static final class PathOfString {
    @BeforeTemplate
    Path before(String first, @Repeated String more) {
      return Paths.get(first, more);
    }

    @AfterTemplate
    Path after(String first, @Repeated String more) {
      return Path.of(first, more);
    }
  }

  /** Avoid redundant conversions from {@link Path} to {@link File}. */
  // XXX: Review whether a rule such as this one is better handled by the `IdentityConversion` rule.
  static final class PathInstance {
    @BeforeTemplate
    Path before(Path path) {
      return path.toFile().toPath();
    }

    @AfterTemplate
    Path after(Path path) {
      return path;
    }
  }

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
   *
   * <p>Note that {@link File#createTempFile} treats the given prefix as a path, and ignores all but
   * its file name. That is, the actual prefix used is derived from all characters following the
   * final file separator (if any). This is not the case with {@link Files#createTempFile}, which
   * will instead throw an {@link IllegalArgumentException} if the prefix contains any file
   * separators.
   */
  static final class FilesCreateTempFileToFile {
    @BeforeTemplate
    @SuppressWarnings({
      "FilesCreateTempFileInCustomDirectoryToFile" /* This is a more specific template. */,
      "java:S5443" /* This violation will be rewritten. */,
      "key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    File before(String prefix, String suffix) throws IOException {
      return Refaster.anyOf(
          File.createTempFile(prefix, suffix), File.createTempFile(prefix, suffix, null));
    }

    @AfterTemplate
    @SuppressWarnings(
        "java:S5443" /* On POSIX systems the file will only have user read-write permissions. */)
    File after(String prefix, String suffix) throws IOException {
      return Files.createTempFile(prefix, suffix).toFile();
    }
  }

  /**
   * Prefer {@link Files#createTempFile(Path, String, String, FileAttribute[])} over alternatives
   * that create files with more liberal permissions.
   *
   * <p>Note that {@link File#createTempFile} treats the given prefix as a path, and ignores all but
   * its file name. That is, the actual prefix used is derived from all characters following the
   * final file separator (if any). This is not the case with {@link Files#createTempFile}, which
   * will instead throw an {@link IllegalArgumentException} if the prefix contains any file
   * separators.
   */
  static final class FilesCreateTempFileInCustomDirectoryToFile {
    @BeforeTemplate
    File before(File directory, String prefix, String suffix) throws IOException {
      return File.createTempFile(prefix, suffix, directory);
    }

    @AfterTemplate
    File after(File directory, String prefix, String suffix) throws IOException {
      return Files.createTempFile(directory.toPath(), prefix, suffix).toFile();
    }
  }

  /** Prefer {@link File#mkdirs} before {@link Files#exists} to avoid concurrency issues. */
  static final class PathToFileMkDirsFilesExists {
    @BeforeTemplate
    boolean before(Path path) {
      return Files.exists(path) || path.toFile().mkdirs();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Path path) {
      return path.toFile().mkdirs() || Files.exists(path);
    }
  }

  /** Prefer {@link File#mkdirs} before {@link File#exists} to avoid concurrency issues. */
  static final class FileMkDirsFileExists {
    @BeforeTemplate
    boolean before(File file) {
      return file.exists() || file.mkdirs();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(File file) {
      return file.mkdirs() || file.exists();
    }
  }
}
