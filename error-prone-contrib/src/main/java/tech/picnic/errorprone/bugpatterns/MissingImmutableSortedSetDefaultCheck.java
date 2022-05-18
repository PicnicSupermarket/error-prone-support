package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import org.immutables.value.Value;

/**
 * A {@link BugChecker} which flags methods with return type {@link
 * com.google.common.collect.ImmutableSortedSet} within an {@code @Value.Immutable} or
 * {@code @Value.Modifiable} class that lack either a default implementation or
 * {@code @Value.NaturalOrder}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MissingImmutableSortedSetDefault",
    summary =
        "Properties of type `ImmutableSortedSet` within an @Value.Immutable or @Value.Modifiable class "
            + "should provide a default value or specify the comparator.",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MissingImmutableSortedSetDefaultCheck extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String VALUE_NATURAL_ORDER_ANNOTATION =
      "org.immutables.value.Value.NaturalOrder";
  private static final Matcher<Tree> HAS_NATURAL_ORDER =
      hasAnnotation(VALUE_NATURAL_ORDER_ANNOTATION);

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // is not within immutable or modifiable class -> no match
    if (tree.getClass().isAnnotationPresent(org.immutables.value.Value.Immutable.class)
        || tree.getClass().isAnnotationPresent(Value.Modifiable.class)) {
      return Description.NO_MATCH;
    }

    // has implementation -> no match
    if (tree.getDefaultValue() != null) {
      return Description.NO_MATCH;
    }

    // is annotated with @Value.NaturalOrder -> no match
    if (HAS_NATURAL_ORDER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // The ImmutableSortedSet has no empty default -> add the `@Value.NaturalOrder` annotation.
    return describeMatch(
        tree,
        SuggestedFix.builder()
            .addStaticImport(VALUE_NATURAL_ORDER_ANNOTATION)
            .prefixWith(tree, "@Value.NaturalOrder ")
            .build());
  }
}
