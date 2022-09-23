package tech.picnic.errorprone.refaster.runner;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A node in an immutable tree.
 *
 * <p>The tree's edges are string-labeled, while its leaves store values of type {@code T}.
 */
@AutoValue
abstract class Node<T> {
  static <T> Node<T> create(Map<String, Node<T>> children, ImmutableList<T> values) {
    return new AutoValue_Node<>(ImmutableSortedMap.copyOf(children), values);
  }

  static <T> Node<T> create(
      List<T> values, Function<T, ImmutableSet<ImmutableSortedSet<String>>> pathExtractor) {
    BuildNode<T> tree = BuildNode.create();
    tree.register(values, pathExtractor);
    return tree.immutable();
  }

  abstract ImmutableMap<String, Node<T>> children();

  abstract ImmutableList<T> values();

  void collectCandidateTemplates(ImmutableList<String> candidateEdges, Consumer<T> sink) {
    values().forEach(sink);

    if (candidateEdges.isEmpty() || children().isEmpty()) {
      return;
    }

    if (children().size() < candidateEdges.size()) {
      for (Map.Entry<String, Node<T>> e : children().entrySet()) {
        if (candidateEdges.contains(e.getKey())) {
          e.getValue().collectCandidateTemplates(candidateEdges, sink);
        }
      }
    } else {
      ImmutableList<String> remainingCandidateEdges =
          candidateEdges.subList(1, candidateEdges.size());
      Node<T> child = children().get(candidateEdges.get(0));
      if (child != null) {
        child.collectCandidateTemplates(remainingCandidateEdges, sink);
      }
      collectCandidateTemplates(remainingCandidateEdges, sink);
    }
  }

  @AutoValue
  @SuppressWarnings("AutoValueImmutableFields" /* Type is used only during `Node` construction. */)
  abstract static class BuildNode<T> {
    static <T> BuildNode<T> create() {
      return new AutoValue_Node_BuildNode<>(new HashMap<>(), new ArrayList<>());
    }

    abstract Map<String, BuildNode<T>> children();

    abstract List<T> values();

    private void register(
        List<T> values, Function<T, ImmutableSet<ImmutableSortedSet<String>>> pathsExtractor) {
      for (T value : values) {
        for (ImmutableSet<String> path : pathsExtractor.apply(value)) {
          registerPath(value, path.asList());
        }
      }
    }

    private void registerPath(T value, ImmutableList<String> path) {
      path.stream()
          .findFirst()
          .ifPresentOrElse(
              edge ->
                  children()
                      .computeIfAbsent(edge, k -> BuildNode.create())
                      .registerPath(value, path.subList(1, path.size())),
              () -> values().add(value));
    }

    private Node<T> immutable() {
      return Node.create(
          Maps.transformValues(children(), BuildNode::immutable), ImmutableList.copyOf(values()));
    }
  }
}
