package tech.picnic.errorprone.refastertemplates;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
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
    return MoreObjects.firstNonNull("foo", "bar");
  }

  @Template(IsNullFunction.class)
  long testIsNullFunction() {
    return Stream.of("foo").filter(s -> s == null).count();
  }

  @Template(NonNullFunction.class)
  long testNonNullFunction() {
    return Stream.of("foo").filter(s -> s != null).count();
  }
}
