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
    return Path.of(URI.create("foo"));
  }

  ImmutableSet<Path> testPathOfVarargs() {
    return ImmutableSet.of(Path.of("foo"), Path.of("bar", "baz", "qux"));
  }

  Path testPathIdentity() {
    return Path.of("foo");
  }

  Path testPathResolveSiblingPath() {
    return Path.of("foo").resolveSibling(Path.of("bar"));
  }

  Path testPathResolveSiblingString() {
    return Path.of("foo").resolveSibling("bar");
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

  File testFilesCreateTempFileFileToPathToFile() throws IOException {
    return Files.createTempFile(new File("baz").toPath(), "foo", "bar").toFile();
  }

  ImmutableSet<Boolean> testPathToFileMkdirsOrFilesExists() {
    return ImmutableSet.of(
        Path.of("foo").toFile().mkdirs() || Files.exists(Path.of("foo")),
        !Path.of("bar").toFile().mkdirs() && !Files.exists(Path.of("bar")));
  }

  ImmutableSet<Boolean> testFileMkdirsOrFileExists() {
    return ImmutableSet.of(
        new File("foo").mkdirs() || new File("foo").exists(),
        !new File("bar").mkdirs() && !new File("bar").exists());
  }

  InputStream testFilesNewInputStreamPathOf() throws IOException {
    return Files.newInputStream(Path.of("foo"));
  }

  InputStream testFilesNewInputStreamFileToPath() throws IOException {
    return Files.newInputStream(new File("foo").toPath());
  }

  OutputStream testFilesNewOutputStreamPathOf() throws IOException {
    return Files.newOutputStream(Path.of("foo"));
  }

  OutputStream testFilesNewOutputStreamFileToPath() throws IOException {
    return Files.newOutputStream(new File("foo").toPath());
  }

  ImmutableSet<BufferedReader> testFilesNewBufferedReader() throws IOException {
    return ImmutableSet.of(
        Files.newBufferedReader(Path.of("foo")), Files.newBufferedReader(Path.of("bar")));
  }

  BufferedReader testFilesNewBufferedReaderWithCharset() throws IOException {
    return Files.newBufferedReader(Path.of("foo"), StandardCharsets.UTF_8);
  }
}
