package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractFileAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJFileRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatDoesNotExist() {
    return assertThat(new File("foo")).doesNotExist();
  }

  AbstractAssert<?, ?> testAssertThatIsFile() {
    return assertThat(new File("foo")).isFile();
  }

  AbstractAssert<?, ?> testAssertThatIsDirectory() {
    return assertThat(new File("foo")).isDirectory();
  }

  AbstractAssert<?, ?> testAssertThatIsAbsolute() {
    return assertThat(new File("foo")).isAbsolute();
  }

  AbstractAssert<?, ?> testAssertThatIsRelative() {
    return assertThat(new File("foo")).isRelative();
  }

  AbstractAssert<?, ?> testAssertThatIsReadable() {
    return assertThat(new File("foo")).isReadable();
  }

  AbstractAssert<?, ?> testAssertThatIsWritable() {
    return assertThat(new File("foo")).isWritable();
  }

  AbstractAssert<?, ?> testAssertThatIsExecutable() {
    return assertThat(new File("foo")).isExecutable();
  }

  AbstractAssert<?, ?> testAssertThatHasFileName() {
    return assertThat(new File("foo")).hasFileName("bar");
  }

  AbstractFileAssert<?> testAssertThatHasParentFile() {
    return assertThat(new File("foo")).hasParent(new File("bar"));
  }

  AbstractFileAssert<?> testAssertThatHasParentString() {
    return assertThat(new File("foo")).hasParent("bar");
  }

  void testAssertThatHasNoParent() {
    assertThat(new File("foo")).hasNoParent();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasExtension() {
    return ImmutableSet.of(
        assertThat(new File("foo")).hasExtension("bar"),
        assertThat(new File("baz")).hasExtension("qux"),
        assertThat(new File("quux")).hasExtension(toString()),
        assertThat(new File("corge")).hasExtension(getClass().toString()));
  }
}
