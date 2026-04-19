package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ObjectAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over arbitrary objects. */
@OnlineDocumentation
final class AssertJObjectRules {
  private AssertJObjectRules() {}

  /** Prefer {@link ObjectAssert#isInstanceOf(Class)} over more contrived alternatives. */
  static final class AssertThatIsInstanceOfClass<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S actual) {
      return assertThat(Refaster.<T>isInstance(actual)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S actual) {
      return assertThat(actual).isInstanceOf(Refaster.<T>clazz());
    }
  }

  /** Prefer {@link ObjectAssert#isInstanceOf(Class)} over more contrived alternatives. */
  static final class AssertThatIsInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, Class<S> type) {
      return assertThat(type.isInstance(actual)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual, Class<S> type) {
      return assertThat(actual).isInstanceOf(type);
    }
  }

  /** Prefer {@link ObjectAssert#isNotInstanceOf(Class)} over more contrived alternatives. */
  static final class AssertThatIsNotInstanceOfClass<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S actual) {
      return assertThat(Refaster.<T>isInstance(actual)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S actual) {
      return assertThat(actual).isNotInstanceOf(Refaster.<T>clazz());
    }
  }

  /** Prefer {@link ObjectAssert#isEqualTo(Object)} over more contrived alternatives. */
  static final class AssertThatIsEqualTo<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S actual, T expected) {
      return assertThat(actual.equals(expected)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S actual, T expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  /** Prefer {@link ObjectAssert#isNotEqualTo(Object)} over more contrived alternatives. */
  static final class AssertThatIsNotEqualTo<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S actual, T other) {
      return assertThat(actual.equals(other)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S actual, T other) {
      return assertThat(actual).isNotEqualTo(other);
    }
  }

  /** Prefer {@link ObjectAssert#hasToString(String)} over more contrived alternatives. */
  static final class AssertThatHasToString<T> {
    @BeforeTemplate
    AbstractStringAssert<?> before(T actual, String expectedToString) {
      return assertThat(actual.toString()).isEqualTo(expectedToString);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual, String expectedToString) {
      return assertThat(actual).hasToString(expectedToString);
    }
  }

  /** Prefer {@link ObjectAssert#isSameAs(Object)} over more contrived alternatives. */
  static final class AssertThatIsSameAs<T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual, T expected) {
      return assertThat(actual).isSameAs(expected);
    }
  }

  /** Prefer {@link ObjectAssert#isNotSameAs(Object)} over more contrived alternatives. */
  static final class AssertThatIsNotSameAs<T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T other) {
      return Refaster.anyOf(
          assertThat(actual == other).isFalse(), assertThat(actual != other).isTrue());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual, T other) {
      return assertThat(actual).isNotSameAs(other);
    }
  }

  /** Prefer {@link ObjectAssert#isNull()} over more contrived alternatives. */
  // XXX: This rule is redundant when the `AssertThatIsSameAs` rule is used in combination with the
  // `AssertJNullnessAssertion` check. It's retained for use with OpenRewrite.
  static final class AssertThatIsNull<T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatIsSameAs" /* This is a more specific template. */)
    void before(T actual) {
      assertThat(actual == null).isTrue();
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatIsSameAs" /* This is a more specific template. */)
    void before2(T actual) {
      assertThat(actual != null).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T actual) {
      assertThat(actual).isNull();
    }
  }

  /** Prefer {@link ObjectAssert#isNotNull()} over more contrived alternatives. */
  // XXX: This rule is redundant when the `AssertThatIsNotSameAs` rule is used in combination with
  // the `AssertJNullnessAssertion` check. It's retained for use with OpenRewrite.
  static final class AssertThatIsNotNull<T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatIsNotSameAs" /* This is a more specific template. */)
    AbstractBooleanAssert<? extends AbstractBooleanAssert<?>> before(T actual) {
      return Refaster.anyOf(
          assertThat(actual == null).isFalse(), assertThat(actual != null).isTrue());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual) {
      return assertThat(actual).isNotNull();
    }
  }

  /** Prefer {@link ObjectAssert#hasSameHashCodeAs(Object)} over more contrived alternatives. */
  static final class AssertThatHasSameHashCodeAs<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.hashCode()).isEqualTo(other.hashCode());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T actual, T other) {
      return assertThat(actual).hasSameHashCodeAs(other);
    }
  }
}
