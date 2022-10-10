package tech.picnic.errorprone.refasterrules;

import static java.util.Objects.requireNonNullElse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class NullRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class);
  }

  boolean testIsNull() {
    return "foo" == null;
  }

  boolean testIsNotNull() {
    return "foo" != null;
  }

  String testRequireNonNullElse() {
    return requireNonNullElse("foo", "bar");
  }

  long testIsNullFunction() {
    return Stream.of("foo").filter(Objects::isNull).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(Objects::nonNull).count();
  }
}
