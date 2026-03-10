package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to expressions dealing with files. */
@OnlineDocumentation
final class FileRules {
  private FileRules() {}

  /** Prefer {@link Path#of(URI)} over less idiomatic alternatives. */
  static final class PathOf {
    @BeforeTemplate
    Path before(URI uri) {
      return Paths.get(uri);
    }

    @AfterTemplate
    Path after(URI uri) {
      return Path.of(uri);
    }
  }

  /** Prefer {@link Path#of(String, String...)} over less idiomatic alternatives. */
  static final class PathOfVarargs {
    @BeforeTemplate
    Path before(String first, @Repeated String more) {
      return Paths.get(first, Refaster.asVarargs(more));
    }

    @AfterTemplate
    Path after(String first, @Repeated String more) {
      return Path.of(first, Refaster.asVarargs(more));
    }
  }

  /** Prefer the {@link Path} as-is over more contrived alternatives. */
  // XXX: Review whether a rule such as this one is better handled by the `IdentityConversion` rule.
  static final class PathIdentity {
    @BeforeTemplate
    Path before(Path path) {
      return path.toFile().toPath();
    }

    @AfterTemplate
    Path after(Path path) {
      return path;
    }
  }

  /**
   * Prefer {@link Path#resolveSibling(Path)} over more fragile or more verbose alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes behavior when {@code path} has no parent: the
   * original code throws a {@link NullPointerException}, while the replacement handles this case
   * gracefully.
   */
  static final class PathResolveSiblingPath {
    @BeforeTemplate
    @SuppressWarnings(
        "NullAway" /* Matched expressions are in practice embedded in a larger context. */)
    Path before(Path path, Path other) {
      return path.getParent().resolve(other);
    }

    @AfterTemplate
    Path after(Path path, Path other) {
      return path.resolveSibling(other);
    }
  }

  /**
   * Prefer {@link Path#resolveSibling(String)} over more fragile or more verbose alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes behavior when {@code path} has no parent: the
   * original code throws a {@link NullPointerException}, while the replacement handles this case
   * gracefully.
   */
  static final class PathResolveSiblingString {
    @BeforeTemplate
    @SuppressWarnings(
        "NullAway" /* Matched expressions are in practice embedded in a larger context. */)
    Path before(Path path, String other) {
      return path.getParent().resolve(other);
    }

    @AfterTemplate
    Path after(Path path, String other) {
      return path.resolveSibling(other);
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
   * Prefer {@link Files#createTempFile(String, String, FileAttribute[])} over less secure
   * alternatives.
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
      "FilesCreateTempFileFileToPathToFile" /* This is a more specific template. */,
      "java:S5443" /* This violation will be rewritten. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
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
   * Prefer {@link Files#createTempFile(Path, String, String, FileAttribute[])} over less secure
   * alternatives.
   *
   * <p>Note that {@link File#createTempFile} treats the given prefix as a path, and ignores all but
   * its file name. That is, the actual prefix used is derived from all characters following the
   * final file separator (if any). This is not the case with {@link Files#createTempFile}, which
   * will instead throw an {@link IllegalArgumentException} if the prefix contains any file
   * separators.
   */
  static final class FilesCreateTempFileFileToPathToFile {
    @BeforeTemplate
    File before(File directory, String prefix, String suffix) throws IOException {
      return File.createTempFile(prefix, suffix, directory);
    }

    @AfterTemplate
    File after(File directory, String prefix, String suffix) throws IOException {
      return Files.createTempFile(directory.toPath(), prefix, suffix).toFile();
    }
  }

  /**
   * Prefer this evaluation order of {@link File#mkdirs()} and {@link Files#exists(Path,
   * LinkOption...)} over more fragile alternatives.
   */
  static final class PathToFileMkdirsOrFilesExists {
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

  /**
   * Prefer this evaluation order of {@link File#mkdirs()} and {@link File#exists()} over more
   * fragile alternatives.
   */
  static final class FileMkdirsOrFileExists {
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

  /** Prefer {@link Files#newInputStream(Path, OpenOption...)} over less idiomatic alternatives. */
  // XXX: The replacement code throws a `NoSuchFileException` instead of a `FileNotFoundException`.
  @PossibleSourceIncompatibility
  static final class FilesNewInputStreamPathOf {
    @BeforeTemplate
    @SuppressWarnings(
        "java:S2095" /* Matched expressions are in practice embedded in a larger context. */)
    FileInputStream before(String path) throws FileNotFoundException {
      return new FileInputStream(path);
    }

    @AfterTemplate
    InputStream after(String path) throws IOException {
      return Files.newInputStream(Path.of(path));
    }
  }

  /** Prefer {@link Files#newInputStream(Path, OpenOption...)} over less idiomatic alternatives. */
  // XXX: The replacement code throws a `NoSuchFileException` instead of a `FileNotFoundException`.
  @PossibleSourceIncompatibility
  static final class FilesNewInputStreamFileToPath {
    @BeforeTemplate
    @SuppressWarnings(
        "java:S2095" /* Matched expressions are in practice embedded in a larger context. */)
    FileInputStream before(File file) throws FileNotFoundException {
      return new FileInputStream(file);
    }

    @AfterTemplate
    InputStream after(File file) throws IOException {
      return Files.newInputStream(file.toPath());
    }
  }

  /** Prefer {@link Files#newOutputStream(Path, OpenOption...)} over less idiomatic alternatives. */
  // XXX: The replacement code throws a `NoSuchFileException` instead of a `FileNotFoundException`.
  @PossibleSourceIncompatibility
  static final class FilesNewOutputStreamPathOf {
    @BeforeTemplate
    @SuppressWarnings(
        "java:S2095" /* Matched expressions are in practice embedded in a larger context. */)
    FileOutputStream before(String path) throws FileNotFoundException {
      return new FileOutputStream(path);
    }

    @AfterTemplate
    OutputStream after(String path) throws IOException {
      return Files.newOutputStream(Path.of(path));
    }
  }

  /** Prefer {@link Files#newOutputStream(Path, OpenOption...)} over less idiomatic alternatives. */
  // XXX: The replacement code throws a `NoSuchFileException` instead of a `FileNotFoundException`.
  @PossibleSourceIncompatibility
  static final class FilesNewOutputStreamFileToPath {
    @BeforeTemplate
    @SuppressWarnings(
        "java:S2095" /* Matched expressions are in practice embedded in a larger context. */)
    FileOutputStream before(File file) throws FileNotFoundException {
      return new FileOutputStream(file);
    }

    @AfterTemplate
    OutputStream after(File file) throws IOException {
      return Files.newOutputStream(file.toPath());
    }
  }

  /**
   * Prefer {@link Files#newBufferedReader(Path)} over more verbose or contrived alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes behavior when no charset is specified: the
   * original code uses the default charset, while the replacement always uses UTF-8.
   */
  static final class FilesNewBufferedReader {
    @BeforeTemplate
    @SuppressWarnings({
      "DefaultCharset" /* This violation will be rewritten. */,
      "java:S1943" /* This violation will be rewritten. */,
      "java:S2095" /* Matched expressions are in practice embedded in a larger context. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    BufferedReader before(Path path) throws IOException {
      return Refaster.anyOf(
          Files.newBufferedReader(path, UTF_8),
          new BufferedReader(new InputStreamReader(Files.newInputStream(path))));
    }

    @AfterTemplate
    BufferedReader after(Path path) throws IOException {
      return Files.newBufferedReader(path);
    }
  }

  /** Prefer {@link Files#newBufferedReader(Path, Charset)} over more contrived alternatives. */
  static final class FilesNewBufferedReaderWithCharset {
    @BeforeTemplate
    BufferedReader before(Path path, Charset charset) throws IOException {
      return new BufferedReader(new InputStreamReader(Files.newInputStream(path), charset));
    }

    @AfterTemplate
    BufferedReader after(Path path, Charset charset) throws IOException {
      return Files.newBufferedReader(path, charset);
    }
  }
}
