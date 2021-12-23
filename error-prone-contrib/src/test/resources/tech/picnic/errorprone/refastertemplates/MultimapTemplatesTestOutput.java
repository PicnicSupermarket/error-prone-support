package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Set;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.MultimapTemplates.MultimapGet;
import tech.picnic.errorprone.refastertemplates.MultimapTemplates.MultimapKeySet;
import tech.picnic.errorprone.refastertemplates.MultimapTemplates.MultimapSize;

@TemplateCollection(MultimapTemplates.class)
final class MultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Multimaps.class);
  }

  @Template(MultimapKeySet.class)
  Set<String> testMultimapKeySet() {
    return ImmutableSetMultimap.of("foo", "bar").keySet();
  }

  @Template(MultimapSize.class)
  int testMultimapSize() {
    return ImmutableSetMultimap.of().size();
  }

  @Template(MultimapGet.class)
  ImmutableSet<Collection<Integer>> testMultimapGet() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(1, 2).get(1),
        ((Multimap<Integer, Integer>) ImmutableSetMultimap.of(1, 2)).get(1));
  }
}
