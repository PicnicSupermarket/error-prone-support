package tech.picnic.errorprone.refasterrules;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.function.Predicate.not;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class NullRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class, Optional.class, Predicate.class, not(null));
  }

  ImmutableSet<Boolean> testIsNull() {
    return ImmutableSet.of("foo" == null, "bar" == null);
  }

  ImmutableSet<Boolean> testIsNotNull() {
    return ImmutableSet.of("foo" != null, "bar" != null);
  }

  ImmutableSet<String> testRequireNonNullElse() {
    return ImmutableSet.of(requireNonNullElse("foo", "bar"), requireNonNullElse("baz", "qux"));
  }

  String testRequireNonNullElseGet() {
    return requireNonNullElseGet("foo", () -> "bar");
  }

  ImmutableSet<Long> testIsNullFunction() {
    return ImmutableSet.of(
        Stream.of("foo").filter(Objects::isNull).count(),
        Stream.of("bar").filter(Objects::isNull).count());
  }

  ImmutableSet<Long> testNonNullFunction() {
    return ImmutableSet.of(
        Stream.of("foo").filter(Objects::nonNull).count(),
        Stream.of("bar").filter(Objects::nonNull).count());
  }
}
