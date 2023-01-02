package tech.picnic.errorprone.refasterrules;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class NullRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class, Optional.class);
  }

  boolean testIsNull() {
    return Objects.isNull("foo");
  }

  boolean testIsNotNull() {
    return Objects.nonNull("foo");
  }

  ImmutableSet<String> testRequireNonNullElse() {
    return ImmutableSet.of(
        MoreObjects.firstNonNull("foo", "bar"), Optional.ofNullable("baz").orElse("qux"));
  }

  String testRequireNonNullElseGet() {
    return Optional.ofNullable("foo").orElseGet(() -> "bar");
  }

  long testIsNullFunction() {
    return Stream.of("foo").filter(s -> s == null).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(s -> s != null).count();
  }
}
