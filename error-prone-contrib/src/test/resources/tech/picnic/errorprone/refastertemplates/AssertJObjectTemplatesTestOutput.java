package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJObjectTemplates.AssertThatHasToString;
import tech.picnic.errorprone.refastertemplates.AssertJObjectTemplates.AssertThatIsInstanceOf;
import tech.picnic.errorprone.refastertemplates.AssertJObjectTemplates.AssertThatIsIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJObjectTemplates.AssertThatIsIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJObjectTemplates.AssertThatIsNotInstanceOf;

@TemplateCollection(AssertJObjectTemplates.class)
final class AssertJObjectTemplatesTest implements RefasterTemplateTestCase {
  @Template(AssertThatIsInstanceOf.class)
  AbstractAssert<?, ?> testAssertThatIsInstanceOf() {
    return assertThat("foo").isInstanceOf(String.class);
  }

  @Template(AssertThatIsNotInstanceOf.class)
  AbstractAssert<?, ?> testAssertThatIsNotInstanceOf() {
    return assertThat("foo").isNotInstanceOf(String.class);
  }

  @Template(AssertThatIsIsEqualTo.class)
  AbstractAssert<?, ?> testAssertThatIsIsEqualTo() {
    return assertThat("foo").isEqualTo("bar");
  }

  @Template(AssertThatIsIsNotEqualTo.class)
  AbstractAssert<?, ?> testAssertThatIsIsNotEqualTo() {
    return assertThat("foo").isNotEqualTo("bar");
  }

  @Template(AssertThatHasToString.class)
  AbstractAssert<?, ?> testAssertThatHasToString() {
    return assertThat(new Object()).hasToString("foo");
  }
}
