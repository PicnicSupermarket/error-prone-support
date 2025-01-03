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

  void testAbstractStringAssertStringIsEmpty() {
    assertThat("foo").isEmpty();
  }

  AbstractStringAssert<?> testAbstractStringAssertStringIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatStringContains() {
    return assertThat("foo").contains("bar");
  }

  AbstractAssert<?, ?> testAssertThatStringDoesNotContain() {
    return assertThat("foo").doesNotContain("bar");
  }

  AbstractAssert<?, ?> testAssertThatMatches() {
    return assertThat("foo").matches(".*");
  }

  AbstractAssert<?, ?> testAssertThatDoesNotMatch() {
    return assertThat("foo").doesNotMatch(".*");
  }

  AbstractStringAssert<?> testAssertThatPathContent() throws IOException {
    return assertThat(Paths.get("")).content(Charset.defaultCharset());
  }

  AbstractStringAssert<?> testAssertThatPathContentUtf8() throws IOException {
    return assertThat(Paths.get("")).content(UTF_8);
  }
}
