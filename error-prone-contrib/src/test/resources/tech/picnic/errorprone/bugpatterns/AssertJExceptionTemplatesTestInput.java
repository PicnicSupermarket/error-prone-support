package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractThrowableAssert;

final class AssertJExceptionTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testThrowableAssertAlternativeHasMessageArgs() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("foo %s %f", "bar", 1)));
  }

  ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>>
      testThrowableAssertAlternativeWithFailMessageArgs() {
    return ImmutableSet.of(
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s", "bar")),
        assertThatThrownBy(() -> {})
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage(String.format("foo %s %f", "bar", 1)));
  }
}
