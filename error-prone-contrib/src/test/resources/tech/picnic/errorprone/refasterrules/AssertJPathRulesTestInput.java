package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPathRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Files.class);
  }

  AbstractAssert<?, ?> testAssertThatExists() {
    return assertThat(Files.exists(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotExist() {
    return assertThat(Files.exists(Path.of("foo"))).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsRegularFile() {
    return assertThat(Files.isRegularFile(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsDirectory() {
    return assertThat(Files.isDirectory(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsSymbolicLink() {
    return assertThat(Files.isSymbolicLink(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsAbsolute() {
    return assertThat(Path.of("foo").isAbsolute()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsRelative() {
    return assertThat(Path.of("foo").isAbsolute()).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsReadable() {
    return assertThat(Files.isReadable(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsWritable() {
    return assertThat(Files.isWritable(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsExecutable() {
    return assertThat(Files.isExecutable(Path.of("foo"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatHasFileName() {
    return assertThat(Path.of("foo").getFileName()).hasToString("bar");
  }

  AbstractAssert<?, ?> testAssertThatHasParentRaw() {
    return assertThat(Path.of("foo").getParent()).isEqualTo(Path.of("bar"));
  }

  void testAssertThatHasNoParent() {
    assertThat(Path.of("foo").getParent()).isNull();
  }

  AbstractAssert<?, ?> testAssertThatStartsWithRaw() {
    return assertThat(Path.of("foo").startsWith(Path.of("bar"))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatEndsWithRaw() {
    return assertThat(Path.of("foo").endsWith(Path.of("bar"))).isTrue();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasExtension() {
    return ImmutableSet.of(
        assertThat(Path.of("foo").toString()).endsWith('.' + "bar"),
        assertThat(Path.of("baz").getFileName().toString()).endsWith('.' + "qux"),
        assertThat(Path.of("quux").toString()).endsWith("." + toString()),
        assertThat(Path.of("corge").getFileName().toString())
            .endsWith("." + getClass().toString()));
  }
}
