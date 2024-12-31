package tech.picnic.errorprone.refasterrules;

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
    return ImmutableSet.of(null == "foo", Objects.isNull("bar"));
  }

  ImmutableSet<Boolean> testIsNotNull() {
    return ImmutableSet.of(null != "foo", Objects.nonNull("bar"));
  }

  ImmutableSet<String> testRequireNonNullElse() {
    return ImmutableSet.of(
        MoreObjects.firstNonNull("foo", "bar"), Optional.ofNullable("baz").orElse("qux"));
  }

  String testRequireNonNullElseGet() {
    return Optional.ofNullable("foo").orElseGet(() -> "bar");
  }

  ImmutableSet<Long> testIsNullFunction() {
    return ImmutableSet.of(
        Stream.of("foo").filter(s -> s == null).count(),
        Stream.of("bar").filter(not(Objects::nonNull)).count());
  }

  ImmutableSet<Long> testNonNullFunction() {
    return ImmutableSet.of(
        Stream.of("foo").filter(s -> s != null).count(),
        Stream.of("bar").filter(not(Objects::isNull)).count());
  }
}
