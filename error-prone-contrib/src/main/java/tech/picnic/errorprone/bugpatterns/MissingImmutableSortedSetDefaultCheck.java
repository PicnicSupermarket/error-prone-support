package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.methodReturns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Modifiable;
import org.immutables.value.Value.NaturalOrder;

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
        "Methods returning an `ImmutableSortedSet` within an @Value.Immutable or @Value.Modifiable class "
            + "should provide a default value or specify the comparator.",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MissingImmutableSortedSetDefaultCheck extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> ENCLOSING_IS_MODIFIABLE_OR_IMMUTABLES =
      enclosingClass(anyOf(hasAnnotation(Immutable.class), hasAnnotation(Modifiable.class)));
  private static final Matcher<MethodTree> RETURNS_IMMUTABLE_SORTED_SET =
      methodReturns(isSameType(ImmutableSortedSet.class));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!RETURNS_IMMUTABLE_SORTED_SET.matches(tree, state)
        || !ENCLOSING_IS_MODIFIABLE_OR_IMMUTABLES.matches(tree, state)
        || hasAnnotation(NaturalOrder.class).matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (tree.getBody() != null && !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    // The ImmutableSortedSet has no empty default -> add the `@Value.NaturalOrder` annotation or
    // provide a default implementation.
    return buildDescription(tree)
        .addFix(SuggestedFix.builder().prefixWith(tree, "@Value.NaturalOrder ").build())
        .addFix(
            SuggestedFix.builder()
                .replace(
                    ASTHelpers.getStartPosition(tree), ASTHelpers.getStartPosition(tree) + 8, "")
                .replace(
                    state.getEndPosition(tree) - 1,
                    state.getEndPosition(tree),
                    "{ return ImmutableSortedSet.of(); }")
                .build())
        .build();
  }
}
