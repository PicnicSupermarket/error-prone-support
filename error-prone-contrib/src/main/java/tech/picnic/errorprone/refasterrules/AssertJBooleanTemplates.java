package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;

final class AssertJBooleanTemplates {
  private AssertJBooleanTemplates() {}

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

  static final class AbstractBooleanAssertIsTrue {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(true),
          boolAssert.isEqualTo(Boolean.TRUE),
          boolAssert.isNotEqualTo(false),
          boolAssert.isNotEqualTo(Boolean.FALSE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isTrue();
    }
  }

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

  static final class AbstractBooleanAssertIsFalse {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(false),
          boolAssert.isEqualTo(Boolean.FALSE),
          boolAssert.isNotEqualTo(true),
          boolAssert.isNotEqualTo(Boolean.TRUE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isFalse();
    }
  }

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
