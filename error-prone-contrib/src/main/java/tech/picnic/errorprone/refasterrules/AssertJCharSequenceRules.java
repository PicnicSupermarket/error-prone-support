package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJCharSequenceRules {
  private AssertJCharSequenceRules() {}

  static final class AssertThatCharSequenceIsEmpty {
    @BeforeTemplate
    void before(CharSequence charSequence) {
      Refaster.anyOf(
          assertThat(charSequence.length()).isEqualTo(0L),
          assertThat(charSequence.length()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
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
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(CharSequence charSequence, int length) {
      return assertThat(charSequence).hasSize(length);
    }
  }
}
