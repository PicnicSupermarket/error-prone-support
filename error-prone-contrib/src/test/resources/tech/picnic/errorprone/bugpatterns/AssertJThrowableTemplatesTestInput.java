package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;

final class AssertJThrowableTemplatesTestInput implements RefasterTemplateTestCase {

    void throwMethod() {
        throw new IllegalArgumentException(("nope"));
    }

    void testWithMessageStringFormat() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> throwMethod())
                .withMessage(String.format("nope %s"));
    }

    void testWithMessageStringFormatVarArgs() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> throwMethod())
                .withMessage(String.format("nope %s %f", "formatObject", 3.14));
    }
}
