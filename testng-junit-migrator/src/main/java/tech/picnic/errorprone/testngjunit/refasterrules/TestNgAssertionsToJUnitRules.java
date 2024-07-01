package tech.picnic.errorprone.testngjunit.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.junit.jupiter.api.Assertions;
import org.testng.Assert;

/** Refaster rules to replace TestNG assertions with JUnit equivalents. */
@SuppressWarnings("StaticImport")
final class TestNgAssertionsToJUnitRules {
  private TestNgAssertionsToJUnitRules() {}

  @SuppressWarnings("AssertEqual")
  static final class AssertEquals {
    @BeforeTemplate
    void before(Object expected, Object actual) {
      Assert.assertEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual) {
      Assertions.assertEquals(expected, actual);
    }
  }

  @SuppressWarnings("AssertEqualWithMessage")
  static final class AssertEqualsMessage {
    @BeforeTemplate
    void before(Object expected, Object actual, String message) {
      Assert.assertEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual, String message) {
      Assertions.assertEquals(expected, actual, message);
    }
  }

  @SuppressWarnings("AssertUnequal")
  static final class AssertNotEquals {
    @BeforeTemplate
    void before(Object expected, Object actual) {
      Assert.assertNotEquals(actual, expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual) {
      Assertions.assertNotEquals(expected, actual);
    }
  }

  @SuppressWarnings("AssertUnequalWithMessage")
  static final class AssertNotEqualsMessage {
    @BeforeTemplate
    void before(Object expected, Object actual, String message) {
      Assert.assertNotEquals(actual, expected, message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Object expected, Object actual, String message) {
      Assertions.assertNotEquals(expected, actual, message);
    }
  }

  @SuppressWarnings({"AssertFalse", "AssertThatIsFalse"})
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

  @SuppressWarnings({"AssertFalseWithMessage", "AssertThatWithFailMessageStringIsFalse"})
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

  @SuppressWarnings({"AssertThatIsTrue", "AssertTrue"})
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

  @SuppressWarnings({"AssertThatWithFailMessageStringIsTrue", "AssertTrueWithMessage"})
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
