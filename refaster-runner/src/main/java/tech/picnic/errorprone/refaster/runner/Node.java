package tech.picnic.errorprone.refaster.runner;

import static java.util.Comparator.comparingInt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A node in an immutable tree.
 *
 * <p>The tree's edges are string-labeled, while its leaves store values of type {@code T}.
 */
record Node<T>(ImmutableMap<String, Node<T>> children, ImmutableList<T> values) {
  // XXX: Review: should this method accept a `SetMultimap<V, ? extends Set<String>>`, or should
  // there be such an overload?
  static <T> Node<T> create(
      Set<T> values, Function<? super T, ? extends Set<? extends Set<String>>> pathExtractor) {
    Builder<T> tree = Builder.create();
    tree.register(values, pathExtractor);
    return tree.build();
  }

  // XXX: Consider having `RefasterRuleSelector` already collect the candidate edges into a
  // `SortedSet`, as that would likely speed up `ImmutableSortedSet#copyOf`.
  // XXX: If this ^ proves worthwhile, then the test code and benchmark should be updated
  // accordingly.
  void collectReachableValues(Set<String> candidateEdges, Consumer<T> sink) {
    collectReachableValues(ImmutableSortedSet.copyOf(candidateEdges).asList(), sink);
  }

  private void collectReachableValues(ImmutableList<String> candidateEdges, Consumer<T> sink) {
    values().forEach(sink);

    if (candidateEdges.isEmpty() || children().isEmpty()) {
      return;
    }

    /*
     * For performance reasons we iterate over the smallest set of edges. In case there are fewer
     * children than candidate edges we iterate over the former, at the cost of not pruning the set
     * of candidate edges if a transition is made.
     */
    int candidateEdgeCount = candidateEdges.size();
    if (children().size() < candidateEdgeCount) {
      for (Map.Entry<String, Node<T>> e : children().entrySet()) {
        if (candidateEdges.contains(e.getKey())) {
          e.getValue().collectReachableValues(candidateEdges, sink);
        }
      }
    } else {
      for (int i = 0; i < candidateEdgeCount; i++) {
        Node<T> child = children().get(candidateEdges.get(i));
        if (child != null) {
          child.collectReachableValues(candidateEdges.subList(i + 1, candidateEdgeCount), sink);
        }
      }
    }
  }

  static final class Builder<T> {
    private final Map<String, Builder<T>> children;
    private final List<T> values;

    private Builder(Map<String, Builder<T>> children, List<T> values) {
      this.children = children;
      this.values = values;
    }

    private static <T> Builder<T> create() {
      return new Builder<>(new HashMap<>(), new ArrayList<>());
    }

    Map<String, Builder<T>> children() {
      return children;
    }

    List<T> values() {
      return values;
    }

    /**
     * Registers all paths to each of the given values.
     *
     * <p>Shorter paths are registered first, so that longer paths can be skipped if a strict prefix
     * leads to the same value.
     */
    private void register(
        Set<T> valuesToRegister,
        Function<? super T, ? extends Set<? extends Set<String>>> pathsExtractor) {
      for (T value : valuesToRegister) {
        List<? extends Set<String>> paths = new ArrayList<>(pathsExtractor.apply(value));
        /*
         * We sort paths by length ascending, so that in case of two paths where one is an initial
         * prefix of the other, only the former is encoded (thus saving some space).
         */
        paths.sort(comparingInt(Set::size));
        // XXX: Use regular `for` loop?
        paths.forEach(path -> registerPath(value, ImmutableList.sortedCopyOf(path)));
      }
    }

    private void registerPath(T value, ImmutableList<String> path) {
      // XXX: Should `values()` be a `Set`?
      if (values().contains(value)) {
        /* Another (shorter) path already leads to this value. */
        return;
      }

      if (path.isEmpty()) {
        values().add(value);
      } else {
        children()
            .computeIfAbsent(path.getFirst(), k -> create())
            .registerPath(value, path.subList(1, path.size()));
      }
    }

    private Node<T> build() {
      return new Node<>(
          ImmutableMap.copyOf(Maps.transformValues(children(), Builder::build)),
          ImmutableList.copyOf(values()));
    }
  }
}
