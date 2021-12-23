package tech.picnic.errorprone.refastertemplates;

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
        Arguments.of("foo"), Arguments.of(1, "foo", 2, "bar"), Arguments.of(new Object()));
  }
}
