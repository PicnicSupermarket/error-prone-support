package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.OptionalAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AbstractOptionalAssertContainsSame;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AbstractOptionalAssertHasValue;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AbstractOptionalAssertIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AbstractOptionalAssertIsPresent;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AssertThatOptional;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AssertThatOptionalHasValueMatching;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AssertThatOptionalIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJOptionalTemplates.AssertThatOptionalIsPresent;

@TemplateCollection(AssertJOptionalTemplates.class)
final class AssertJOptionalTemplatesTest implements RefasterTemplateTestCase {
  @Template(AssertThatOptional.class)
  AbstractAssert<?, ?> testAssertThatOptional() {
    return assertThat(Optional.of(new Object())).get();
  }

  @Template(AbstractOptionalAssertIsPresent.class)
  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isPresent(), assertThat(Optional.of(2)).isPresent());
  }

  @Template(AssertThatOptionalIsPresent.class)
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isPresent(), assertThat(Optional.of(2)).isPresent());
  }

  @Template(AbstractOptionalAssertIsEmpty.class)
  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isEmpty(), assertThat(Optional.of(2)).isEmpty());
  }

  @Template(AssertThatOptionalIsEmpty.class)
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isEmpty(), assertThat(Optional.of(2)).isEmpty());
  }

  @Template(AbstractOptionalAssertHasValue.class)
  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertHasValue() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).hasValue(1),
        assertThat(Optional.of(2)).hasValue(2),
        assertThat(Optional.of(3)).hasValue(3),
        assertThat(Optional.of(4)).hasValue(4));
  }

  @Template(AbstractOptionalAssertContainsSame.class)
  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertContainsSame() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).containsSame(1), assertThat(Optional.of(2)).containsSame(2));
  }

  @Template(AssertThatOptionalHasValueMatching.class)
  AbstractAssert<?, ?> testAssertThatOptionalHasValueMatching() {
    return assertThat(Optional.of("foo")).get().matches(String::isEmpty);
  }
}
