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

  void testAbstractStringAssertStringIsEmpty() {
    assertThat("foo").isEqualTo("");
  }

  AbstractStringAssert<?> testAbstractStringAssertStringIsNotEmpty() {
    return assertThat("foo").isNotEqualTo("");
  }

  AbstractAssert<?, ?> testAssertThatStringStartsWith() {
    return assertThat("foo".startsWith("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatStringDoesNotStartWith() {
    return assertThat("foo".startsWith("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatStringEndsWith() {
    return assertThat("foo".endsWith("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatStringDoesNotEndWith() {
    return assertThat("foo".endsWith("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatStringContains() {
    return assertThat("foo".contains("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatStringDoesNotContain() {
    return assertThat("foo".contains("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo".matches(".*")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo".matches(".*")).isFalse();
  }

  AbstractStringAssert<?> testAssertThatPathContent() throws IOException {
    return assertThat(Files.readString(Paths.get(""), Charset.defaultCharset()));
  }

  AbstractStringAssert<?> testAssertThatPathContentUtf8() throws IOException {
    return assertThat(Files.readString(Paths.get("")));
  }
}
