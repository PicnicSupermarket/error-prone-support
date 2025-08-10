package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class FileRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(FileInputStream.class, InputStreamReader.class);
  }

  Path testPathOfUri() {
    return Path.of(URI.create("foo"));
  }

  ImmutableSet<Path> testPathOfString() {
    return ImmutableSet.of(Path.of("foo"), Path.of("bar", "baz", "qux"));
  }

  Path testPathInstance() {
    return Path.of("foo");
  }

  String testFilesReadStringWithCharset() throws IOException {
    return Files.readString(Paths.get("foo"), StandardCharsets.ISO_8859_1);
  }

  String testFilesReadString() throws IOException {
    return Files.readString(Paths.get("foo"));
  }

  ImmutableSet<File> testFilesCreateTempFileToFile() throws IOException {
    return ImmutableSet.of(
        Files.createTempFile("foo", "bar").toFile(), Files.createTempFile("baz", "qux").toFile());
  }

  File testFilesCreateTempFileInCustomDirectoryToFile() throws IOException {
    return Files.createTempFile(new File("baz").toPath(), "foo", "bar").toFile();
  }

  ImmutableSet<Boolean> testPathToFileMkDirsFilesExists() {
    return ImmutableSet.of(
        Path.of("foo").toFile().mkdirs() || Files.exists(Path.of("foo")),
        !Path.of("bar").toFile().mkdirs() && !Files.exists(Path.of("bar")));
  }

  ImmutableSet<Boolean> testFileMkDirsFileExists() {
    return ImmutableSet.of(
        new File("foo").mkdirs() || new File("foo").exists(),
        !new File("bar").mkdirs() && !new File("bar").exists());
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReaderPathOf() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(Path.of("foo")), Files.newBufferedReader(Path.of("bar")));
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReaderToPath() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(new File("foo").toPath()),
        Files.newBufferedReader(new File("bar").toPath()));
  }

  BufferedReader testFilesNewBufferedReaderPathOfWithCharset() throws IOException {
    return Files.newBufferedReader(Path.of("foo"), StandardCharsets.UTF_8);
  }

  BufferedReader testFilesNewBufferedReaderToPathWithCharset() throws IOException {
    return Files.newBufferedReader(new File("foo").toPath(), StandardCharsets.UTF_8);
  }
}
