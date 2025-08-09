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

  BufferedReader testFilesNewBufferedReaderFromStringPath() throws IOException {
    return new BufferedReader(new InputStreamReader(new FileInputStream("foo.txt")));
  }

  BufferedReader testFilesNewBufferedReaderFromFilePath() throws IOException {
    return new BufferedReader(new InputStreamReader(new FileInputStream(new File("bar.txt"))));
  }

  BufferedReader testFilesNewBufferedReaderFromStringPathWithCharset() throws IOException {
    return new BufferedReader(
        new InputStreamReader(new FileInputStream("baz.txt"), StandardCharsets.ISO_8859_1));
  }

  BufferedReader testFilesNewBufferedReaderFromFilePathWithCharset() throws IOException {
    return new BufferedReader(
        new InputStreamReader(new FileInputStream(new File("qux.txt")), StandardCharsets.UTF_16));
  }
}
