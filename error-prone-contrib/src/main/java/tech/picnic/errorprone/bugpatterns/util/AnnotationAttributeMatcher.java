package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Type;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

// XXX: Redefine using auto-value
// XXX: Document design decision that the project stays as close as possible to Error Prone.
//      ^ ... and *therefore* uses Google Auto Value rather than Immutables.org.
// XXX: Make this class implement the `MultiMatcher` interface.
/**
 * A matcher of (annotation, attribute) pairs.
 *
 * <p>This class allows one to define a whitelist or blacklist of annotations or their attributes.
 * Annotations are identified by their fully qualified name.
 */
public final class AnnotationAttributeMatcher implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean complement;
  private final ImmutableSet<String> wholeTypes;
  private final ImmutableSetMultimap<String, String> includedAttributes;
  private final ImmutableSetMultimap<String, String> excludedAttributes;

  private AnnotationAttributeMatcher(
      boolean complement,
      ImmutableSet<String> wholeTypes,
      ImmutableSetMultimap<String, String> includedAttributes,
      ImmutableSetMultimap<String, String> excludedAttributes) {
    this.complement = complement;
    this.wholeTypes = wholeTypes;
    this.includedAttributes = includedAttributes;
    this.excludedAttributes = excludedAttributes;
  }

  /**
   * Creates an {@link AnnotationAttributeMatcher}.
   *
   * <p>Each string provided to this method must be of the form {@code
   * "some.fully.qualified.AnnotationType"} or {@code
   * "some.fully.qualified.AnnotationType#attribute"}.
   *
   * @param inclusions If specified, only the listed annotations or annotation attributes are
   *     matched.
   * @param exclusions The listed annotations or annotation attributes are not matched.
   * @return A non-{@code null} {@link AnnotationAttributeMatcher}.
   */
  public static AnnotationAttributeMatcher create(
      Optional<? extends List<String>> inclusions, Iterable<String> exclusions) {
    Set<String> includedWholeTypes = new HashSet<>();
    Set<String> excludedWholeTypes = new HashSet<>();
    SetMultimap<String, String> includedAttributes = HashMultimap.create();
    SetMultimap<String, String> excludedAttributes = HashMultimap.create();

    inclusions.ifPresent(incl -> update(incl, includedWholeTypes, includedAttributes));
    update(exclusions, excludedWholeTypes, excludedAttributes);
    includedWholeTypes.removeAll(excludedWholeTypes);
    includedAttributes.keySet().removeAll(includedWholeTypes);
    includedAttributes.keySet().removeAll(excludedWholeTypes);
    excludedAttributes.forEach(includedAttributes::remove);
    excludedAttributes.keySet().removeAll(excludedWholeTypes);

    return new AnnotationAttributeMatcher(
        inclusions.isEmpty(),
        ImmutableSet.copyOf(inclusions.isPresent() ? includedWholeTypes : excludedWholeTypes),
        ImmutableSetMultimap.copyOf(includedAttributes),
        ImmutableSetMultimap.copyOf(excludedAttributes));
  }

  private static void update(
      Iterable<String> enumeration,
      Set<String> wholeTypes,
      SetMultimap<String, String> attributeRestrictions) {
    for (String entry : enumeration) {
      int hash = entry.indexOf('#');
      if (hash < 0) {
        wholeTypes.add(entry);
      } else {
        String annotationType = entry.substring(0, hash);
        String attribute = entry.substring(hash + 1);
        attributeRestrictions.put(annotationType, attribute);
      }
    }
  }

  /**
   * Returns the subset of arguments of the given {@link AnnotationTree} matched by this instance.
   *
   * @param tree The annotation AST node to be inspected.
   * @return Any matching annotation arguments.
   */
  public Stream<ExpressionTree> extractMatchingArguments(AnnotationTree tree) {
    Type type = ASTHelpers.getType(tree.getAnnotationType());
    if (type == null) {
      return Stream.empty();
    }

    String annotationType = type.toString();
    return tree.getArguments().stream()
        .map(ExpressionTree.class::cast)
        .filter(a -> matches(annotationType, extractAttributeName(a)));
  }

  private static String extractAttributeName(ExpressionTree expr) {
    return (expr.getKind() == Kind.ASSIGNMENT)
        ? ASTHelpers.getSymbol(((AssignmentTree) expr).getVariable()).getSimpleName().toString()
        : "value";
  }

  // XXX: The caller of this method can be implemented more efficiently in case of a "wholeTypes"
  // match.
  // XXX: Make this method private; re-implement the tests in terms of `#extractMatchingArguments`.
  @VisibleForTesting
  boolean matches(String annotationType, String attribute) {
    if (complement) {
      return !wholeTypes.contains(annotationType)
          && !excludedAttributes.containsEntry(annotationType, attribute);
    }

    return (wholeTypes.contains(annotationType)
            && !excludedAttributes.containsEntry(annotationType, attribute))
        || includedAttributes.containsEntry(annotationType, attribute);
  }
}
