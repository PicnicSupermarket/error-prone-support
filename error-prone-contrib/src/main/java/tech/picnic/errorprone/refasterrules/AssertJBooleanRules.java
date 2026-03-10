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
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isNotEqualTo(!other);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isEqualTo(other);
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isNotEqualTo(Object)} over more contrived alternatives. */
  static final class AbstractBooleanAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isEqualTo(!other);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isTrue()} over less explicit alternatives. */
  static final class AbstractBooleanAssertIsTrue {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(boolAssert.isEqualTo(true), boolAssert.isNotEqualTo(false));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isTrue();
    }
  }

  /** Prefer {@code assertThat(b).isTrue()} over more contrived alternatives. */
  static final class AssertThatBooleanIsTrue {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean b) {
      return assertThat(!b).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean b) {
      return assertThat(b).isTrue();
    }
  }

  /** Prefer {@link AbstractBooleanAssert#isFalse()} over less explicit alternatives. */
  static final class AbstractBooleanAssertIsFalse {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(boolAssert.isEqualTo(false), boolAssert.isNotEqualTo(true));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isFalse();
    }
  }

  /** Prefer {@code assertThat(b).isFalse()} over more contrived alternatives. */
  static final class AssertThatBooleanIsFalse {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean b) {
      return assertThat(!b).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean b) {
      return assertThat(b).isFalse();
    }
  }
}
