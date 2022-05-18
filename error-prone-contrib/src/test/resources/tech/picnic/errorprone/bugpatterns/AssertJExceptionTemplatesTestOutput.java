package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssertAlternative;

import static org.assertj.core.api.Assertions.*;

final class AssertJExceptionTemplatesTest implements RefasterTemplateTestCase {
    ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>> testThrowableAssertAlternativeHasMessageArgs() {
        return ImmutableSet.of(
                assertThatThrownBy(() -> {})
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("foo %s", "bar"),
                assertThatThrownBy(() -> {})
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("foo %s %f", "bar", 1)
        );
    }

    ImmutableSet<AbstractThrowableAssert<?, ? extends Throwable>> testThrowableAssertAlternativeWithFailMessageArgs() {
        return ImmutableSet.of(
                assertThatThrownBy(() -> {})
                        .isInstanceOf(IllegalArgumentException.class)
                        .withFailMessage("foo %s", "bar"),
                assertThatThrownBy(() -> {})
                        .isInstanceOf(IllegalArgumentException.class)
                        .withFailMessage("foo %s %f", "bar", 1));
    }
}
