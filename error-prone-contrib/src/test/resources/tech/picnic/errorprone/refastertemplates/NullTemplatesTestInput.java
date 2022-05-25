package tech.picnic.errorprone.refastertemplates;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;

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
}
