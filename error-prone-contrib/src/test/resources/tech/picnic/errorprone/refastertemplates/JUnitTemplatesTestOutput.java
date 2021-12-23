package tech.picnic.errorprone.refastertemplates;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.JUnitTemplates.ArgumentsEnumeration;

@TemplateCollection(JUnitTemplates.class)
final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  @Template(ArgumentsEnumeration.class)
  ImmutableSet<Arguments> testArgumentsEnumeration() {
    return ImmutableSet.of(
        arguments("foo"), arguments(1, "foo", 2, "bar"), arguments(new Object()));
  }
}
