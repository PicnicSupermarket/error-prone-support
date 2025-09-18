package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

import java.util.Collection;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

/** Refaster rules related to AssertJ assertions over {@link Collection}s. */
@OnlineDocumentation
final class AssertJCollectionRules {
  private AssertJCollectionRules() {}

  /** Prefer {@link AbstractCollectionAssert#hasSize(int)} over more verbose alternatives. */
  static final class AssertThatCollectionHasSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> collection, int size) {
        return assertThat(collection.size()).isEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, ?, ?, ?> after(Collection<E> collection, int size) {
        return assertThat(collection).hasSize(size);
    }
  }

}
