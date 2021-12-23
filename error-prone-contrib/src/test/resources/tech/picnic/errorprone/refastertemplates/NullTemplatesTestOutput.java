package tech.picnic.errorprone.refastertemplates;

import static java.util.Objects.requireNonNullElse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.NullTemplates.IsNullFunction;
import tech.picnic.errorprone.refastertemplates.NullTemplates.NonNullFunction;
import tech.picnic.errorprone.refastertemplates.NullTemplates.RequireNonNullElse;

@TemplateCollection(NullTemplates.class)
final class NullTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(MoreObjects.class);
  }

  @Template(RequireNonNullElse.class)
  String testRequireNonNullElse() {
    return requireNonNullElse("foo", "bar");
  }

  @Template(IsNullFunction.class)
  long testIsNullFunction() {
    return Stream.of("foo").filter(Objects::isNull).count();
  }

  @Template(NonNullFunction.class)
  long testNonNullFunction() {
    return Stream.of("foo").filter(Objects::nonNull).count();
  }
}
