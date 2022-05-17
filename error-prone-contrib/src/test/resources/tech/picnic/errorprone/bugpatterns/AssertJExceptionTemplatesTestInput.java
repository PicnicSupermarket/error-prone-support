package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

final class AssertJExceptionTemplatesTestInput implements RefasterTemplateTestCase {
    void testThrowableAssertAlternativeWithMessageArgs() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> {})
                .withMessage(String.format("foo"));
    }

    void testThrowableAssertAlternativeWithMessageArgsOptionalArgs() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> {})
                .withMessage(String.format("foo %s %f", "bar", 1));
    }
}
