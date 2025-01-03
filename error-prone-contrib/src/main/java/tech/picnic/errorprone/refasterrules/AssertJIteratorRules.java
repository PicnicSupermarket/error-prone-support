package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Iterator;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.IteratorAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJIteratorRules {
  private AssertJIteratorRules() {}

  static final class AssertThatHasNext<T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Iterator<T> iterator) {
      return assertThat(iterator.hasNext()).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IteratorAssert<T> after(Iterator<T> iterator) {
      return assertThat(iterator).hasNext();
    }
  }

  static final class AssertThatIsExhausted<T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Iterator<T> iterator) {
      return assertThat(iterator.hasNext()).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IteratorAssert<T> after(Iterator<T> iterator) {
      return assertThat(iterator).isExhausted();
    }
  }
}
