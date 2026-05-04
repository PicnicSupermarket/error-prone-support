package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStringRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Files.class);
  }

  void testAbstractStringAssertIsEmpty() {
    assertThat("foo").isEqualTo("");
  }

  AbstractStringAssert<?> testAbstractStringAssertIsNotEmpty() {
    return assertThat("foo").isNotEqualTo("");
  }

  AbstractAssert<?, ?> testAssertThatStartsWith() {
    return assertThat("foo".startsWith("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotStartWith() {
    return assertThat("foo".startsWith("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatEndsWith() {
    return assertThat("foo".endsWith("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotEndWith() {
    return assertThat("foo".endsWith("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatContains() {
    return assertThat("foo".contains("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotContain() {
    return assertThat("foo".contains("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsEqualToIgnoringCase() {
    return assertThat("foo".equalsIgnoreCase("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotEqualToIgnoringCase() {
    return assertThat("foo".equalsIgnoreCase("bar")).isFalse();
  }

  void testAssertThatIsBlank() {
    assertThat("foo".isBlank()).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotBlank() {
    return assertThat("foo".isBlank()).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo".matches("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo".matches("bar")).isFalse();
  }

  AbstractStringAssert<?> testAssertThatContent() throws IOException {
    return assertThat(Files.readString(Paths.get(""), Charset.defaultCharset()));
  }

  AbstractStringAssert<?> testAssertThatContentUtf8() throws IOException {
    return assertThat(Files.readString(Paths.get("")));
  }
}
