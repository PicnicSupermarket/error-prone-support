package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractFileAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJFileRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatDoesNotExist() {
    return assertThat(new File("foo").exists()).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsFile() {
    return assertThat(new File("foo").isFile()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsDirectory() {
    return assertThat(new File("foo").isDirectory()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsAbsolute() {
    return assertThat(new File("foo").isAbsolute()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsRelative() {
    return assertThat(new File("foo").isAbsolute()).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsReadable() {
    return assertThat(new File("foo").canRead()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsWritable() {
    return assertThat(new File("foo").canWrite()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsExecutable() {
    return assertThat(new File("foo").canExecute()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatHasFileName() {
    return assertThat(new File("foo").getName()).isEqualTo("bar");
  }

  AbstractFileAssert<?> testAssertThatHasParentFile() {
    return assertThat(new File("foo").getParentFile()).isEqualTo(new File("bar"));
  }

  AbstractFileAssert<?> testAssertThatHasParentString() {
    return assertThat(new File("foo").getParentFile()).hasFileName("bar");
  }

  void testAssertThatHasNoParent() {
    assertThat(new File("foo").getParent()).isNull();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasExtension() {
    return ImmutableSet.of(
        assertThat(new File("foo").getName()).endsWith('.' + "bar"),
        assertThat(new File("baz").toString()).endsWith('.' + "qux"),
        assertThat(new File("quux").getName()).endsWith("." + toString()),
        assertThat(new File("corge").toString()).endsWith("." + getClass().toString()));
  }
}
