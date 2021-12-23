package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertHasSameSizeAs;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJEnumerableTemplates.EnumerableAssertIsNotEmpty;

@TemplateCollection(AssertJEnumerableTemplates.class)
final class AssertJEnumerableTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty1() {
    assertThat(ImmutableSet.of()).hasSize(0);
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty2() {
    assertThat(ImmutableSet.of()).hasSizeLessThanOrEqualTo(0);
  }

  @Template(EnumerableAssertIsEmpty.class)
  void testEnumerableAssertIsEmpty3() {
    assertThat(ImmutableSet.of()).hasSizeLessThan(1);
  }

  @Template(EnumerableAssertIsNotEmpty.class)
  ImmutableSet<EnumerableAssert<?, Character>> testEnumerableAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo").hasSizeGreaterThan(0), assertThat("bar").hasSizeGreaterThanOrEqualTo(1));
  }

  @Template(EnumerableAssertHasSameSizeAs.class)
  ImmutableSet<EnumerableAssert<?, Integer>> testEnumerableAssertHasSameSizeAs() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).hasSize(Iterables.size(ImmutableSet.of(2))),
        assertThat(ImmutableSet.of(3)).hasSize(ImmutableSet.of(4).size()),
        assertThat(ImmutableSet.of(5)).hasSize(new Integer[0].length));
  }
}
