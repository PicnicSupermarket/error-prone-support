package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPathRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Files.class);
  }

  AbstractAssert<?, ?> testAbstractPathAssertExists() {
    return assertThat(Paths.get("/tmp")).exists();
  }

  AbstractAssert<?, ?> testAbstractPathAssertDoesNotExist() {
    return assertThat(Paths.get("/nonexistent")).doesNotExist();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsRegularFile() {
    return assertThat(Paths.get("/tmp/file.txt")).isRegularFile();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsDirectory() {
    return assertThat(Paths.get("/tmp")).isDirectory();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsSymbolicLink() {
    return assertThat(Paths.get("/tmp/link")).isSymbolicLink();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsAbsolute() {
    return assertThat(Paths.get("/tmp")).isAbsolute();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsRelative() {
    return assertThat(Paths.get("tmp")).isRelative();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsReadable() {
    return assertThat(Paths.get("/tmp")).isReadable();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsWritable() {
    return assertThat(Paths.get("/tmp")).isWritable();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsExecutable() {
    return assertThat(Paths.get("/bin/sh")).isExecutable();
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasFileName() {
    return assertThat(Paths.get("/tmp/file.txt")).hasFileName("file.txt");
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasParent() {
    return assertThat(Paths.get("/tmp/file.txt")).hasParent(Paths.get("/tmp"));
  }

  void testAbstractPathAssertHasNoParent() {
    assertThat(Paths.get("/")).hasNoParent();
  }

  AbstractAssert<?, ?> testAbstractPathAssertStartsWith() {
    return assertThat(Paths.get("/tmp/file.txt")).startsWith(Paths.get("/tmp"));
  }

  AbstractAssert<?, ?> testAbstractPathAssertEndsWith() {
    return assertThat(Paths.get("/tmp/file.txt")).endsWith(Paths.get("file.txt"));
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasExtension() {
    String ext = "txt";
    return assertThat(Paths.get("/tmp/file.txt")).hasExtension(ext);
  }
}
