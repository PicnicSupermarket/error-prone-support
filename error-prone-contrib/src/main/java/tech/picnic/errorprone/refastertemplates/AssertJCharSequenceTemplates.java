package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractAssert;

final class AssertJCharSequenceTemplates {
  private AssertJCharSequenceTemplates() {}

  static final class AbstractCharSequenceAssertContains {
    @BeforeTemplate
    void before(String string, CharSequence charSequence) {
      assertThat(string.contains(charSequence)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String string, CharSequence charSequence) {
      assertThat(string).contains(charSequence);
    }
  }

  static final class AssertThatCharSequenceIsEmpty {
    @BeforeTemplate
    void before(CharSequence charSequence) {
      Refaster.anyOf(
          assertThat(charSequence.length()).isEqualTo(0L),
          assertThat(charSequence.length()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(CharSequence charSequence) {
      assertThat(charSequence).isEmpty();
    }
  }

  static final class AssertThatCharSequenceIsNotEmpty {
    @BeforeTemplate
    AbstractAssert<?, ?> before(CharSequence charSequence) {
      return Refaster.anyOf(
          assertThat(charSequence.length()).isNotEqualTo(0),
          assertThat(charSequence.length()).isPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(CharSequence charSequence) {
      return assertThat(charSequence).isNotEmpty();
    }
  }

  static final class AssertThatCharSequenceHasSize {
    @BeforeTemplate
    AbstractAssert<?, ?> before(CharSequence charSequence, int length) {
      return assertThat(charSequence.length()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(CharSequence charSequence, int length) {
      return assertThat(charSequence).hasSize(length);
    }
  }
}
