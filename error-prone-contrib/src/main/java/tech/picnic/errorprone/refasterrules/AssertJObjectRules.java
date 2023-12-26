package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
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
}
