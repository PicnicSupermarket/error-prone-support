package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractPathAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJPathRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Files.class);
  }

  AbstractAssert<?, ?> testAssertThatExists() {
    return assertThat(Path.of("foo")).exists();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotExist() {
    return assertThat(Path.of("foo")).doesNotExist();
  }

  AbstractAssert<?, ?> testAssertThatIsRegularFile() {
    return assertThat(Path.of("foo")).isRegularFile();
  }

  AbstractAssert<?, ?> testAssertThatIsDirectory() {
    return assertThat(Path.of("foo")).isDirectory();
  }

  AbstractAssert<?, ?> testAssertThatIsSymbolicLink() {
    return assertThat(Path.of("foo")).isSymbolicLink();
  }

  AbstractAssert<?, ?> testAssertThatIsAbsolute() {
    return assertThat(Path.of("foo")).isAbsolute();
  }

  AbstractAssert<?, ?> testAssertThatIsRelative() {
    return assertThat(Path.of("foo")).isRelative();
  }

  AbstractAssert<?, ?> testAssertThatIsReadable() {
    return assertThat(Path.of("foo")).isReadable();
  }

  AbstractAssert<?, ?> testAssertThatIsWritable() {
    return assertThat(Path.of("foo")).isWritable();
  }

  AbstractAssert<?, ?> testAssertThatIsExecutable() {
    return assertThat(Path.of("foo")).isExecutable();
  }

  AbstractPathAssert<?> testAssertThatHasFileName() {
    return assertThat(Path.of("foo")).hasFileName("bar");
  }

  AbstractPathAssert<?> testAssertThatHasParentRaw() {
    return assertThat(Path.of("foo")).hasParentRaw(Path.of("bar"));
  }

  void testAssertThatHasNoParent() {
    assertThat(Path.of("foo")).hasNoParent();
  }

  AbstractAssert<?, ?> testAssertThatStartsWithRaw() {
    return assertThat(Path.of("foo")).startsWithRaw(Path.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatEndsWithRaw() {
    return assertThat(Path.of("foo")).endsWithRaw(Path.of("bar"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasExtension() {
    return ImmutableSet.of(
        assertThat(Path.of("foo")).hasExtension("bar"),
        assertThat(Path.of("baz")).hasExtension("qux"),
        assertThat(Path.of("quux")).hasExtension(toString()),
        assertThat(Path.of("corge")).hasExtension(getClass().toString()));
  }
}
