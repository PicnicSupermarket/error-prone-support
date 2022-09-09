package tech.picnic.errorprone.refastertemplates;

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
    return MoreObjects.firstNonNull("foo", "bar");
  }

  long testIsNullFunction() {
    return Stream.of("foo").filter(s -> s == null).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(s -> s != null).count();
  }

  boolean testIsNull() {
    return Objects.isNull("foo");
  }

  boolean testIsNotNull() {
    return Objects.nonNull("foo");
  }
}
