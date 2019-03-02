package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.collect.Lists;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.ArrayList;
import java.util.Collection;

/** Prefer {@link ArrayList#ArrayList(Collection)} over the Guava alternative. */
final class NewArrayListFromCollection<T> {
  @BeforeTemplate
  ArrayList<T> before(Collection<T> collection) {
    return Lists.newArrayList(collection);
  }

  @AfterTemplate
  ArrayList<T> after(Collection<T> collection) {
    return new ArrayList<>(collection);
  }
}
