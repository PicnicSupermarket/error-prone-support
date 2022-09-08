package tech.picnic.errorprone.refastertemplates;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
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

  boolean testIsNullReference() {
    var ref1 = "foo";
    var ref2 = Set.of(1, 2, 3);
    Object ref3 = null;
    return Objects.isNull(ref1) || Objects.isNull(ref2) || Objects.isNull(ref3);
  }

  boolean testIsNonNullReference() {
    var ref1 = "foo";
    var ref2 = Set.of(1, 2, 3);
    Object ref3 = null;
    return Objects.nonNull(ref1) || Objects.nonNull(ref2) || Objects.nonNull(ref3);
  }
}
