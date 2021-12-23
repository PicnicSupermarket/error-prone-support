package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJCharSequenceTemplates.AssertThatCharSequenceHasSize;
import tech.picnic.errorprone.refastertemplates.AssertJCharSequenceTemplates.AssertThatCharSequenceIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJCharSequenceTemplates.AssertThatCharSequenceIsNotEmpty;

@TemplateCollection(AssertJCharSequenceTemplates.class)
final class AssertJCharSequenceTemplatesTest implements RefasterTemplateTestCase {
  @Template(AssertThatCharSequenceIsEmpty.class)
  void testAssertThatCharSequenceIsEmpty1() {
    assertThat("foo".length()).isEqualTo(0L);
  }

  @Template(AssertThatCharSequenceIsEmpty.class)
  void testAssertThatCharSequenceIsEmpty2() {
    assertThat("foo".length()).isNotPositive();
  }

  @Template(AssertThatCharSequenceIsNotEmpty.class)
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo".length()).isNotEqualTo(0), assertThat("bar".length()).isPositive());
  }

  @Template(AssertThatCharSequenceHasSize.class)
  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo".length()).isEqualTo(3);
  }
}
