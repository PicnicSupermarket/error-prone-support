package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitToAssertJRulesTest implements RefasterRuleCollectionTestCase {
    void testAssertTrue() {
        assertThat(true).isTrue();
    }

    void testAssertTrueWithMessage() {
        assertThat(true).withFailMessage("foo").isTrue();
    }

    void testAssertTrueWithMessageSupplier() {
        assertThat(true).withFailMessage(() -> "foo").isTrue();
    }
}
