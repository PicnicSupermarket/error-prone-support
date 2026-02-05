package tech.picnic.errorprone.refaster.runner;

import static java.util.Collections.newSetFromMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

// XXX: Add some examples of which source files would match what templates in the tree.
// XXX: Consider this text in general.
/**
 * A {@link RefasterRuleSelector} algorithm that selects Refaster templates based on the content of
 * a {@link CompilationUnitTree}.
 *
 * <p>The algorithm consists of the following steps:
 *
 * <ol>
 *   <li>Create a {@link Node tree} structure based on the provided Refaster templates.
 *       <ol>
 *         <li>Extract all identifiers from the {@code @BeforeTemplate} methods.
 *         <li>Sort identifiers lexicographically and collect into a set.
 *         <li>Add a path to the tree based on the sorted identifiers.
 *       </ol>
 *   <li>Extract all identifiers from the {@link CompilationUnitTree} and sort them
 *       lexicographically.
 *   <li>Traverse the tree based on the identifiers from the {@link CompilationUnitTree}. Every node
 *       can contain Refaster templates. Once a node is we found a candidate Refaster template that
 *       might match some code and will therefore be added to the list of candidates.
 * </ol>
 *
 * <p>This is an example to explain the algorithm. Consider the templates with identifiers; {@code
 * T1 = [A, B, C]}, {@code T2 = [B]}, and {@code T3 = [B, D]}. This will result in the following
 * tree structure:
 *
 * <pre>{@code
 * <root>
 *    ├── A
 *    │   └── B
 *    │       └── C -- T1
 *    └── B         -- T2
 *        └── D     -- T3
 * }</pre>
 *
 * <p>The tree is traversed based on the identifiers in the {@link CompilationUnitTree}. When a node
 * containing a template is reached, we can be certain that the identifiers from the
 * {@code @BeforeTemplate} are at least present in the {@link CompilationUnitTree}.
 *
 * <p>Since the identifiers are sorted, we can skip parts of the {@link Node tree} while we are
 * traversing it. Instead of trying to match all Refaster templates against every expression in a
 * {@link CompilationUnitTree} we now only matching a subset of the templates that at least have a
 * chance of matching. As a result, the performance of Refaster increases significantly.
 */
final class RefasterRuleSelector {
  private final Node<CodeTransformer> codeTransformers;

  private RefasterRuleSelector(Node<CodeTransformer> codeTransformers) {
    this.codeTransformers = codeTransformers;
  }

  /**
   * Instantiates a new {@link RefasterRuleSelector} backed by the given {@link CodeTransformer}s.
   */
  @SuppressWarnings("NullAway" /* XXX: Inspect this. */)
  static RefasterRuleSelector create(ImmutableCollection<CodeTransformer> refasterRules) {
    // XXX: Instead of performing the indexing every time rules are loaded, consider collecting the
    // identifiers after rule compilation, and storing them on the `CodeTransformer`s using a custom
    // annotation. Review whether this approach comes with caveats around supporting multiple JDK
    // versions.
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> ruleIdentifiersByTransformer =
        indexRuleIdentifiers(refasterRules);
    return new RefasterRuleSelector(
        Node.create(ruleIdentifiersByTransformer.keySet(), ruleIdentifiersByTransformer::get));
  }

  /**
   * Retrieves a set of Refaster templates that can possibly match based on a {@link
   * CompilationUnitTree}.
   *
   * @param tree The {@link CompilationUnitTree} for which candidate Refaster templates are
   *     selected.
   * @return Set of Refaster templates that can possibly match in the provided {@link
   *     CompilationUnitTree}.
   */
  Set<CodeTransformer> selectCandidateRules(CompilationUnitTree tree) {
    Set<CodeTransformer> candidateRules = newSetFromMap(new IdentityHashMap<>());
    codeTransformers.collectReachableValues(
        SourceIdentifierExtractor.extractIdentifiers(tree), candidateRules::add);
    return candidateRules;
  }

  @VisibleForTesting
  static Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexRuleIdentifiers(
      ImmutableCollection<CodeTransformer> codeTransformers) {
    IdentityHashMap<CodeTransformer, ImmutableSet<ImmutableSet<String>>> identifiers =
        new IdentityHashMap<>();
    for (CodeTransformer transformer : codeTransformers) {
      collectRuleIdentifiers(transformer, identifiers);
    }
    return identifiers;
  }

  private static void collectRuleIdentifiers(
      CodeTransformer codeTransformer,
      Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> identifiers) {
    switch (codeTransformer) {
      case CompositeCodeTransformer compositeCodeTransformer -> {
        for (CodeTransformer transformer : compositeCodeTransformer.transformers()) {
          collectRuleIdentifiers(transformer, identifiers);
        }
      }
      case AnnotatedCompositeCodeTransformer annotatedTransformer -> {
        for (Map.Entry<CodeTransformer, ImmutableSet<ImmutableSet<String>>> e :
            indexRuleIdentifiers(annotatedTransformer.transformers()).entrySet()) {
          identifiers.put(
              new AnnotatedCompositeCodeTransformer(
                  annotatedTransformer.packageName(),
                  ImmutableList.of(e.getKey()),
                  annotatedTransformer.annotations()),
              e.getValue());
        }
      }
      case RefasterRule<?, ?> refasterRule ->
          identifiers.put(
              codeTransformer, RefasterRuleIdentifierExtractor.extractIdentifiers(refasterRule));
      default ->
          /* Unrecognized `CodeTransformer` types are indexed such that they always apply. */
          identifiers.put(codeTransformer, ImmutableSet.of(ImmutableSet.of()));
    }
  }
}
