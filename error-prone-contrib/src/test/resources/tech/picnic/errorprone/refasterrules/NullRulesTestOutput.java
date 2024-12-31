package tech.picnic.errorprone.refasterrules;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.function.Predicate.not;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class NullRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class, Optional.class, not(null));
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

  ImmutableSet<Predicate<String>> testIsNullFunction() {
    return ImmutableSet.of(Objects::isNull, Objects::isNull);
  }

  ImmutableSet<Predicate<String>> testNonNullFunction() {
    return ImmutableSet.of(Objects::nonNull, Objects::nonNull);
  }
}
