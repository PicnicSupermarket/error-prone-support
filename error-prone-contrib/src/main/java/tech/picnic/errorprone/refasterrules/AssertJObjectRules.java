package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ObjectAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJObjectRules {
  private AssertJObjectRules() {}

  static final class AssertThatIsInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isInstanceOf(Refaster.<T>clazz());
    }
  }

  static final class AssertThatIsNotInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isNotInstanceOf(Refaster.<T>clazz());
    }
  }

  static final class AssertThatIsIsEqualTo<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object1, T object2) {
      return assertThat(object1.equals(object2)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object1, T object2) {
      return assertThat(object1).isEqualTo(object2);
    }
  }

  static final class AssertThatIsIsNotEqualTo<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object1, T object2) {
      return assertThat(object1.equals(object2)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object1, T object2) {
      return assertThat(object1).isNotEqualTo(object2);
    }
  }

  static final class AssertThatHasToString<T> {
    @BeforeTemplate
    AbstractStringAssert<?> before(T object, String str) {
      return assertThat(object.toString()).isEqualTo(str);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectAssert<T> after(T object, String str) {
      return assertThat(object).hasToString(str);
    }
  }

  // XXX: This rule could produce non-compilable code. Possible improvements:
  // Instead of `type(clazz)` suggest a more suitable `InstanceOfAssertFactory`. (Perhaps have
  // separate rules which e.g. replace `.asInstanceOf(type(clazz))` with
  // `.asInstanceOf(throwable(clazz))`.)
  // Next to `matches(Predicate)`, this rule applies to several other functional interface-accepting
  // assertion methods.
  // Arguably this rule should be split in two.
  @SuppressWarnings("unchecked")
  abstract static class AbstractAssertAsInstanceOfMatches<R, S, T extends S> {
    @Placeholder
    abstract boolean test(S value);

    @BeforeTemplate
    AbstractAssert<?, R> before(AbstractAssert<?, R> abstractAssert, Class<T> clazz) {
      return abstractAssert.isInstanceOf(clazz).matches(v -> test((S) v));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, S> after(AbstractAssert<?, R> abstractAssert, Class<S> clazz) {
      return abstractAssert.asInstanceOf(type(clazz)).matches(v -> test(v));
    }
  }
}
