package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code boolean}s. */
@OnlineDocumentation
final class AssertJBooleanRules {
  private AssertJBooleanRules() {}

  /** Prefer {@link AbstractBooleanAssert#isEqualTo(Object)} over more contrived alternatives. */
  static final class AbstractBooleanAssertIsEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> booleanAssert, boolean expected) {
      return booleanAssert.isNotEqualTo(!expected);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> booleanAssert, boolean expected) {
      return booleanAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isNotEqualTo(Object)} over more contrived alternatives. */
  static final class AbstractBooleanAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> booleanAssert, boolean other) {
      return booleanAssert.isEqualTo(!other);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> booleanAssert, boolean other) {
      return booleanAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isTrue()} over less explicit alternatives. */
  static final class AbstractBooleanAssertIsTrue {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> booleanAssert) {
      return Refaster.anyOf(booleanAssert.isEqualTo(true), booleanAssert.isNotEqualTo(false));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> booleanAssert) {
      return booleanAssert.isTrue();
    }
  }

  /** Prefer {@code assertThat(b).isTrue()} over more contrived alternatives. */
  static final class AssertThatIsTrue {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual) {
      return assertThat(!actual).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual) {
      return assertThat(actual).isTrue();
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isFalse()} over less explicit alternatives. */
  static final class AbstractBooleanAssertIsFalse {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> booleanAssert) {
      return Refaster.anyOf(booleanAssert.isEqualTo(false), booleanAssert.isNotEqualTo(true));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> booleanAssert) {
      return booleanAssert.isFalse();
    }
  }

  /** Prefer {@code assertThat(b).isFalse()} over more contrived alternatives. */
  static final class AssertThatIsFalse {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual) {
      return assertThat(!actual).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual) {
      return assertThat(actual).isFalse();
    }
  }
}
