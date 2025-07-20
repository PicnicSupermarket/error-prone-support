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
    return assertThat(Files.exists(Paths.get("/tmp"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertDoesNotExist() {
    return assertThat(Files.exists(Paths.get("/nonexistent"))).isFalse();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsRegularFile() {
    return assertThat(Files.isRegularFile(Paths.get("/tmp/file.txt"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsDirectory() {
    return assertThat(Files.isDirectory(Paths.get("/tmp"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsSymbolicLink() {
    return assertThat(Files.isSymbolicLink(Paths.get("/tmp/link"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsAbsolute() {
    return assertThat(Paths.get("/tmp").isAbsolute()).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsRelative() {
    return assertThat(Paths.get("tmp").isAbsolute()).isFalse();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsReadable() {
    return assertThat(Files.isReadable(Paths.get("/tmp"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsWritable() {
    return assertThat(Files.isWritable(Paths.get("/tmp"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertIsExecutable() {
    return assertThat(Files.isExecutable(Paths.get("/bin/sh"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasFileName() {
    return assertThat(Paths.get("/tmp/file.txt").getFileName().toString()).isEqualTo("file.txt");
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasParent() {
    return assertThat(Paths.get("/tmp/file.txt").getParent()).isEqualTo(Paths.get("/tmp"));
  }

  void testAbstractPathAssertHasNoParent() {
    assertThat(Paths.get("/").getParent()).isNull();
  }

  AbstractAssert<?, ?> testAbstractPathAssertStartsWith() {
    return assertThat(Paths.get("/tmp/file.txt").startsWith(Paths.get("/tmp"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertEndsWith() {
    return assertThat(Paths.get("/tmp/file.txt").endsWith(Paths.get("file.txt"))).isTrue();
  }

  AbstractAssert<?, ?> testAbstractPathAssertHasExtension() {
    String ext = "txt";
    return assertThat(Paths.get("/tmp/file.txt").toString().endsWith("." + ext)).isTrue();
  }
}
