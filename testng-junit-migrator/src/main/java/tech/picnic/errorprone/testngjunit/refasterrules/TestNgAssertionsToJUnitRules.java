package tech.picnic.errorprone.testngjunit.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;

/** Refaster rules to replace TestNG assertions with JUnit equivalents. */
final class TestNgAssertionsToJUnitRules {
  private TestNgAssertionsToJUnitRules() {}

  static final class AssertEquals {
    @BeforeTemplate
    void before(Object expected, Object actual) {
      assertThat(actual).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual) {
      assertEquals(expected, actual);
    }
  }

  static final class AssertEqualsMessage {
    @BeforeTemplate
    void before(Object expected, Object actual, String message) {
      assertThat(actual).withFailMessage(message).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual, String message) {
      assertEquals(expected, actual, message);
    }
  }

  static final class AssertNotEquals {
    @BeforeTemplate
    void before(Object expected, Object actual) {
      assertThat(actual).isNotEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual) {
      assertNotEquals(expected, actual);
    }
  }

  static final class AssertNotEqualsMessage {
    @BeforeTemplate
    void before(Object expected, Object actual, String message) {
      assertThat(actual).withFailMessage(message).isNotEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual, String message) {
      assertNotEquals(expected, actual, message);
    }
  }

  static final class AssertFalseCondition {
    @BeforeTemplate
    void before(boolean condition) {
      assertThat(condition).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      assertThat(condition).isFalse();
    }
  }

  static final class AssertFalseConditionMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      assertThat(condition).withFailMessage(message).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      assertThat(condition).withFailMessage(message).isFalse();
    }
  }

  static final class AssertTrueCondition {
    @BeforeTemplate
    void before(boolean condition) {
      assertThat(condition).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      assertThat(condition).isTrue();
    }
  }

  static final class AssertTrueConditionMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      assertThat(condition).withFailMessage(message).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      assertThat(condition).withFailMessage(message).isTrue();
    }
  }
}
