package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;
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
    assertThat("foo").isEmpty();
  }

  AbstractStringAssert<?> testAbstractStringAssertIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatStartsWith() {
    return assertThat("foo").startsWith("bar");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotStartWith() {
    return assertThat("foo").doesNotStartWith("bar");
  }

  AbstractAssert<?, ?> testAssertThatEndsWith() {
    return assertThat("foo").endsWith("bar");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotEndWith() {
    return assertThat("foo").doesNotEndWith("bar");
  }

  AbstractAssert<?, ?> testAssertThatContains() {
    return assertThat("foo").contains("bar");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotContain() {
    return assertThat("foo").doesNotContain("bar");
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo").matches("bar");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo").doesNotMatch("bar");
  }

  AbstractStringAssert<?> testAssertThatContent() throws IOException {
    return assertThat(Paths.get("")).content(Charset.defaultCharset());
  }

  AbstractStringAssert<?> testAssertThatContentUtf8() throws IOException {
    return assertThat(Paths.get("")).content(UTF_8);
  }
}
