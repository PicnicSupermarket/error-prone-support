package tech.picnic.errorprone.refastertemplates;

import static java.util.Objects.requireNonNullElse;

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
    return requireNonNullElse("foo", "bar");
  }

  long testIsNullFunction() {
    return Stream.of("foo").filter(Objects::isNull).count();
  }

  long testNonNullFunction() {
    return Stream.of("foo").filter(Objects::nonNull).count();
  }

  boolean testIsNullReference() {
    var ref1 = "foo";
    var ref2 = Set.of(1, 2, 3);
    Object ref3 = null;
    return ref1 == null || ref2 == null || ref3 == null;
  }

  boolean testIsNonNullReference() {
    var ref1 = "foo";
    var ref2 = Set.of(1, 2, 3);
    Object ref3 = null;
    return ref1 != null || ref2 != null || ref3 != null;
  }
}
