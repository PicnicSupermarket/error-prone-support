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
    return Paths.get(URI.create("foo"));
  }

  ImmutableSet<Path> testPathOfString() {
    return ImmutableSet.of(Paths.get("foo"), Paths.get("bar", "baz", "qux"));
  }

  Path testPathInstance() {
    return Path.of("foo").toFile().toPath();
  }

  Path testPathResolveSiblingPath() {
    return Path.of("foo").getParent().resolve(Path.of("bar"));
  }

  Path testPathResolveSiblingString() {
    return Path.of("foo").getParent().resolve("bar");
  }

  String testFilesReadStringWithCharset() throws IOException {
    return new String(Files.readAllBytes(Paths.get("foo")), StandardCharsets.ISO_8859_1);
  }

  String testFilesReadString() throws IOException {
    return Files.readString(Paths.get("foo"), StandardCharsets.UTF_8);
  }

  ImmutableSet<File> testFilesCreateTempFileToFile() throws IOException {
    return ImmutableSet.of(
        File.createTempFile("foo", "bar"), File.createTempFile("baz", "qux", null));
  }

  File testFilesCreateTempFileInCustomDirectoryToFile() throws IOException {
    return File.createTempFile("foo", "bar", new File("baz"));
  }

  ImmutableSet<Boolean> testPathToFileMkDirsFilesExists() {
    return ImmutableSet.of(
        Files.exists(Path.of("foo")) || Path.of("foo").toFile().mkdirs(),
        !Files.exists(Path.of("bar")) && !Path.of("bar").toFile().mkdirs());
  }

  ImmutableSet<Boolean> testFileMkDirsFileExists() {
    return ImmutableSet.of(
        new File("foo").exists() || new File("foo").mkdirs(),
        !new File("bar").exists() && !new File("bar").mkdirs());
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReaderPathOf() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(Path.of("foo"), StandardCharsets.UTF_8),
        new BufferedReader(new InputStreamReader(new FileInputStream("bar"))));
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReaderToPath() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(new File("foo").toPath(), StandardCharsets.UTF_8),
        new BufferedReader(new InputStreamReader(new FileInputStream(new File("bar")))));
  }

  BufferedReader testFilesNewBufferedReaderPathOfWithCharset() throws IOException {
    return new BufferedReader(
        new InputStreamReader(new FileInputStream("foo"), StandardCharsets.UTF_8));
  }

  BufferedReader testFilesNewBufferedReaderToPathWithCharset() throws IOException {
    return new BufferedReader(
        new InputStreamReader(new FileInputStream(new File("foo")), StandardCharsets.UTF_8));
  }
}
