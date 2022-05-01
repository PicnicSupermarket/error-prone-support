package tech.picnic.errorprone.bugpatterns;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@AutoValue
abstract class BuildNode<T> {
  static <T> BuildNode<T> create() {
    return new AutoValue_BuildNode<>(new TreeMap<>(), new HashSet<>());
  }

  static <T> BuildNode<T> create(SortedMap<String, BuildNode<T>> children, Set<T> candidateRules) {
    return new AutoValue_BuildNode<>(children, candidateRules);
  }

  @SuppressWarnings("AutoValueImmutableFields" /* The BuildNode is used to construct the tree. */)
  public abstract SortedMap<String, BuildNode<T>> children();

  @SuppressWarnings("AutoValueImmutableFields" /* The BuildNode is used to construct the tree. */)
  public abstract Set<T> candidateRules();

  public void register(ImmutableSet<ImmutableSortedSet<String>> identifierCombinations, T rule) {
    for (ImmutableSet<String> path : identifierCombinations) {
      registerPath(path.asList(), rule);
    }
  }

  void registerPath(ImmutableList<String> path, T rule) {
    path.stream()
        .findFirst()
        .ifPresentOrElse(
            edge ->
                children()
                    .computeIfAbsent(edge, k -> BuildNode.create())
                    .registerPath(path.subList(1, path.size()), rule),
            () -> candidateRules().add(rule));
  }

  Node<T> immutable() {
    return Node.create(
        Maps.transformValues(ImmutableSortedMap.copyOfSorted(children()), BuildNode::immutable),
        ImmutableSet.copyOf(candidateRules()));
  }
}
