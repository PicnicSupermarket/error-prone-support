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
 * A {@link BugChecker} which flags methods with return type {@link
 * com.google.common.collect.ImmutableSortedSet} within a class or interface annotated with
 * {@code @Value.Immutable} or {@code @Value.Modifiable} and lacks either a default implementation
 * or {@code @Value.NaturalOrder} annotation.
 *
 * <p>Such methods without {@code @Value.NaturalOrder} or default implementation would result in
 * deserialization problems in case of absent sets.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MissingImmutableSortedSetDefault",
    summary =
        "Methods returning an `ImmutableSortedSet` within a `@Value.Immutable` or `@Value.Modifiable` class "
            + "or interface should provide a default value or specify a comparator",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MissingImmutableSortedSetDefaultCheck extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodTree> RETURNS_IMMUTABLE_SORTED_SET =
      methodReturns(isSameType(ImmutableSortedSet.class));
  private static final Matcher<Tree> ENCLOSING_IS_IMMUTABLE_OR_MODIFIABLE =
      enclosingClass(
          anyOf(
              hasAnnotation("org.immutables.value.Value.Immutable"),
              hasAnnotation("org.immutables.value.Value.Modifiable")));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!RETURNS_IMMUTABLE_SORTED_SET.matches(tree, state)
        || !ENCLOSING_IS_IMMUTABLE_OR_MODIFIABLE.matches(tree, state)
        || hasAnnotation("org.immutables.value.Value.NaturalOrder").matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (tree.getBody() != null && !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "Methods returning an `ImmutableSortedSet` within a `@Value.Immutable` or `@Value.Modifiable` class or "
                + "interface should be annotated with `@Value.NaturalOrder` or specify a default implementation")
        .addFix(
            SuggestedFix.builder()
                .addImport("org.immutables.value.Value")
                .prefixWith(tree, "@Value.NaturalOrder ")
                .build())
        .build();
  }
}
