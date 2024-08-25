package tech.picnic.errorprone.refasterrules.input;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class NullRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class, Optional.class);
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

  long testIsNullFunction() {
    return Stream.of("foo").filter(s -> s == null).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(s -> s != null).count();
  }
}
