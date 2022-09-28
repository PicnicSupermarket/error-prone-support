package tech.picnic.errorprone.refasterrules.output;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

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
    return "foo" == null;
  }

  boolean testIsNotNull() {
    return "foo" != null;
  }

  ImmutableSet<String> testRequireNonNullElse() {
    return ImmutableSet.of(requireNonNullElse("foo", "bar"), requireNonNullElse("baz", "qux"));
  }

  String testRequireNonNullElseGet() {
    return requireNonNullElseGet("foo", () -> "bar");
  }

  long testIsNullFunction() {
    return Stream.of("foo").filter(Objects::isNull).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(Objects::nonNull).count();
  }
}
