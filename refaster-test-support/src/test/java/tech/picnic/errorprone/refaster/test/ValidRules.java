package tech.picnic.errorprone.refaster.test;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.base.Strings;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Set;
import javax.annotation.Nullable;

/** Refaster rule collection to validate that having no violations works as expected. */
final class ValidRules {
  private ValidRules() {}

  static final class StringIsEmpty2 {
    @BeforeTemplate
    boolean before(String string) {
      return string.toCharArray().length == 0;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }

  static final class StaticImportStringLength {
    @BeforeTemplate
    boolean before(@Nullable String string) {
      return string == null || string.toCharArray().length == 0;
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    boolean after(String string) {
      return Strings.isNullOrEmpty(string);
    }
  }

  abstract static class BlockRuleSetAddElement<E> {
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
