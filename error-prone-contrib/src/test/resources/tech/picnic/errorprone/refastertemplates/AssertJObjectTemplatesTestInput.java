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
    return assertThat("foo" instanceof String).isTrue();
  }

  @Template(AssertThatIsNotInstanceOf.class)
  AbstractAssert<?, ?> testAssertThatIsNotInstanceOf() {
    return assertThat("foo" instanceof String).isFalse();
  }

  @Template(AssertThatIsIsEqualTo.class)
  AbstractAssert<?, ?> testAssertThatIsIsEqualTo() {
    return assertThat("foo".equals("bar")).isTrue();
  }

  @Template(AssertThatIsIsNotEqualTo.class)
  AbstractAssert<?, ?> testAssertThatIsIsNotEqualTo() {
    return assertThat("foo".equals("bar")).isFalse();
  }

  @Template(AssertThatHasToString.class)
  AbstractAssert<?, ?> testAssertThatHasToString() {
    return assertThat(new Object().toString()).isEqualTo("foo");
  }
}
