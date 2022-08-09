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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * A {@link BugChecker} which flags instance methods with return type {@link
 * com.google.common.collect.ImmutableSortedSet} defined inside a {@code @Value.Immutable}- or
 * {@code @Value.Modifiable}-annotated type that lack the {@code @Value.NaturalOrder} or
 * {@code @Value.ReverseOrder} annotation.
 *
 * <p>Deserialization of the enclosing type then requires that the associated JSON property is
 * present, would result in deserialization problems in case of absent sets.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MissingImmutableSortedSetDefault",
    summary =
        "`ImmutableSortedSet` properties of a `@Value.Immutable` or `@Value.Modifiable` type "
            + "should be annotated `@Value.NaturalOrder` or `@Value.ReverseOrder`.",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MissingImmutableSortedSetDefaultCheck extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodTree> RETURNS_IMMUTABLE_SORTED_SET =
      methodReturns(isSameType(ImmutableSortedSet.class));
  private static final Matcher<Tree> IS_IMMUTABLES_TYPE =
      enclosingClass(
          anyOf(
              hasAnnotation("org.immutables.value.Value.Immutable"),
              hasAnnotation("org.immutables.value.Value.Modifiable")));
  private static final Matcher<Tree> HAS_ORDER_ANNOTATION =
      anyOf(
          hasAnnotation("org.immutables.value.Value.NaturalOrder"),
          hasAnnotation("org.immutables.value.Value.ReverseOrder"));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!RETURNS_IMMUTABLE_SORTED_SET.matches(tree, state)
        || !IS_IMMUTABLES_TYPE.matches(tree, state)
        || HAS_ORDER_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (tree.getBody() != null && !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(
            SuggestedFix.builder()
                .addImport("org.immutables.value.Value")
                .prefixWith(tree, "@Value.NaturalOrder ")
                .build())
        .build();
  }
}
