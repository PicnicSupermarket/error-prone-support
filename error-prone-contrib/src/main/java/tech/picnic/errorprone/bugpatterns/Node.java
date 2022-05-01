package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@AutoValue
abstract class Node<T> {
  static <T> Node<T> create(Map<String, Node<T>> children, ImmutableSet<T> candidateRules) {
    return new AutoValue_Node<>(ImmutableMap.copyOf(children), candidateRules);
  }

  public abstract ImmutableMap<String, Node<T>> children();

  public abstract ImmutableSet<T> candidateRules();

  static <C> Node<C> createRefasterTemplateTree(
      List<C> refasterRules, Function<C, ImmutableSet<ImmutableSortedSet<String>>> edgeExtractor) {
    // XXX: Improve this method...
    List<ImmutableSet<ImmutableSortedSet<String>>> beforeTemplateIdentifiers =
        refasterRules.stream().map(edgeExtractor).collect(toImmutableList());

    BuildNode<C> tree = BuildNode.create();
    for (int i = 0; i < refasterRules.size(); i++) {
      tree.register(beforeTemplateIdentifiers.get(i), refasterRules.get(i));
    }
    return tree.immutable();
  }

  void collectCandidateTemplates(ImmutableList<String> sourceIdentifiers, Consumer<T> sink) {
    candidateRules().forEach(sink);

    if (sourceIdentifiers.isEmpty() || children().isEmpty()) {
      return;
    }

    if (children().size() < sourceIdentifiers.size()) {
      for (Map.Entry<String, Node<T>> e : children().entrySet()) {
        if (sourceIdentifiers.contains(e.getKey())) {
          e.getValue().collectCandidateTemplates(sourceIdentifiers, sink);
        }
      }
    } else {
      ImmutableList<String> remainingSourceCandidateEdges =
          sourceIdentifiers.subList(1, sourceIdentifiers.size());
      Node<T> child = children().get(sourceIdentifiers.get(0));
      if (child != null) {
        child.collectCandidateTemplates(remainingSourceCandidateEdges, sink);
      }
      collectCandidateTemplates(remainingSourceCandidateEdges, sink);
    }
  }
}
