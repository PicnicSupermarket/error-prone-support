package tech.picnic.errorprone.testngjunit.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.junit.jupiter.api.Assertions;
import org.testng.Assert;

/** Refaster rules to replace TestNG assertions with JUnit equivalents. */
final class TestNgAssertionsToJUnitRules {
  private TestNgAssertionsToJUnitRules() {}

  static final class AssertEquals {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      Assert.assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      Assertions.assertEquals(expected, actual);
    }
  }

  static final class AssertEqualsMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      Assert.assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      Assertions.assertEquals(expected, actual, message);
    }
  }

  static final class AssertNotEquals {
    @BeforeTemplate
    void before(Object actual, Object expected) {
      Assert.assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected) {
      Assertions.assertNotEquals(expected, actual);
    }
  }

  static final class AssertNotEqualsMessage {
    @BeforeTemplate
    void before(Object actual, Object expected, String message) {
      Assert.assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object actual, Object expected, String message) {
      Assertions.assertNotEquals(expected, actual, message);
    }
  }

  static final class AssertFalseCondition {
    @BeforeTemplate
    void before(boolean condition) {
      Assert.assertFalse(condition);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      Assertions.assertFalse(condition);
    }
  }

  static final class AssertFalseConditionMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      Assert.assertFalse(condition, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      Assertions.assertFalse(condition, message);
    }
  }

  static final class AssertTrueCondition {
    @BeforeTemplate
    void before(boolean condition) {
      Assert.assertTrue(condition);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition) {
      Assertions.assertTrue(condition);
    }
  }

  static final class AssertTrueConditionMessage {
    @BeforeTemplate
    void before(boolean condition, String message) {
      Assert.assertTrue(condition, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(boolean condition, String message) {
      Assertions.assertTrue(condition, message);
    }
  }
}
