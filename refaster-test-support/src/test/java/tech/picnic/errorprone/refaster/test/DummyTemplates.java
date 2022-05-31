package tech.picnic.errorprone.refaster.test;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.base.Strings;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Refaster template collection containing arbitrary examples to validate that violations are
 * reported correctly.
 */
// XXX: Rename class and update Javadoc.
final class DummyTemplates {
  private DummyTemplates() {}

  static final class StringIsEmpty2 {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("");
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }

  static final class StaticImportStringLength {
    @BeforeTemplate
    boolean before(@Nullable String string) {
      return string == null || string.isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    boolean after(String string) {
      return Strings.isNullOrEmpty(string);
    }
  }

  abstract class SetAddElement<E> {
    @Placeholder
    abstract void doAfterAdd(E element);

    @BeforeTemplate
    void before(Set<E> set, E elem) {
      if (!set.contains(elem)) {
        set.add(elem);
        doAfterAdd(elem);
      }
    }

    @AfterTemplate
    void after(Set<E> set, E elem) {
      if (set.add(elem)) {
        doAfterAdd(elem);
      }
    }
  }
}
