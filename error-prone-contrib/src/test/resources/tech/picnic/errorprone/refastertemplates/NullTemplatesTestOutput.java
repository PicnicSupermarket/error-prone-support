package tech.picnic.errorprone.refastertemplates;

import static java.util.Objects.requireNonNullElse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class NullTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class);
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

  boolean testObjectEqualsNull() {
    Object obj = null;
    return obj == null;
  }

  boolean testObjectNotEqualsNull() {
    Object obj = null;
    return obj != null;
  }
}
