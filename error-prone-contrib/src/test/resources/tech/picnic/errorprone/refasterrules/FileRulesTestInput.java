package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class FileRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(FileInputStream.class, FileOutputStream.class, InputStreamReader.class);
  }

  Path testPathOf() {
    return Paths.get(URI.create("foo"));
  }

  ImmutableSet<Path> testPathOfVarargs() {
    return ImmutableSet.of(Paths.get("foo"), Paths.get("bar", "baz", "qux"));
  }

  Path testPathIdentity() {
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

  File testFilesCreateTempFileFileToPathToFile() throws IOException {
    return File.createTempFile("foo", "bar", new File("baz"));
  }

  ImmutableSet<Boolean> testPathToFileMkdirsOrFilesExists() {
    return ImmutableSet.of(
        Files.exists(Path.of("foo")) || Path.of("foo").toFile().mkdirs(),
        !Files.exists(Path.of("bar")) && !Path.of("bar").toFile().mkdirs());
  }

  ImmutableSet<Boolean> testFileMkdirsOrFileExists() {
    return ImmutableSet.of(
        new File("foo").exists() || new File("foo").mkdirs(),
        !new File("bar").exists() && !new File("bar").mkdirs());
  }

  InputStream testFilesNewInputStreamPathOf() throws IOException {
    return new FileInputStream("foo");
  }

  InputStream testFilesNewInputStreamToPath() throws IOException {
    return new FileInputStream(new File("foo"));
  }

  OutputStream testFilesNewOutputStreamPathOf() throws IOException {
    return new FileOutputStream("foo");
  }

  OutputStream testFilesNewOutputStreamToPath() throws IOException {
    return new FileOutputStream(new File("foo"));
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReader() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(Path.of("foo"), StandardCharsets.UTF_8),
        new BufferedReader(new InputStreamReader(Files.newInputStream(Path.of("bar")))));
  }

  BufferedReader testFilesNewBufferedReaderWithCharset() throws IOException {
    return new BufferedReader(
        new InputStreamReader(Files.newInputStream(Path.of("foo")), StandardCharsets.UTF_8));
  }
}
