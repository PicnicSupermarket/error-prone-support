package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.methodReturns;
import static com.google.errorprone.matchers.Matchers.not;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;
import java.util.SortedSet;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} which flags {@link SortedSet} property declarations inside
 * {@code @Value.Immutable}- and {@code @Value.Modifiable}-annotated types that lack a
 * {@code @Value.NaturalOrder} or {@code @Value.ReverseOrder} annotation.
 *
 * <p>Without such an annotation:
 *
 * <ul>
 *   <li>deserialization of the enclosing type requires that the associated JSON property is
 *       present, contrary to the way in which Immutables handles other collection properties; and
 *   <li>different instances may use different comparator implementations (e.g. deserialization
 *       would default to natural order sorting), potentially leading to subtle bugs.
 * </ul>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`SortedSet` properties of a `@Value.Immutable` or `@Value.Modifiable` type must be "
            + "annotated `@Value.NaturalOrder` or `@Value.ReverseOrder`",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class ImmutablesSortedSetComparator extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodTree> METHOD_LACKS_ANNOTATION =
      allOf(
          methodReturns(isSubtypeOf(SortedSet.class)),
          anyOf(
              allOf(
                  hasModifier(Modifier.ABSTRACT),
                  enclosingClass(
                      anyOf(
                          hasAnnotation("org.immutables.value.Value.Immutable"),
                          hasAnnotation("org.immutables.value.Value.Modifiable")))),
              hasAnnotation("org.immutables.value.Value.Default")),
          not(
              anyOf(
                  hasAnnotation("org.immutables.value.Value.NaturalOrder"),
                  hasAnnotation("org.immutables.value.Value.ReverseOrder"))));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!METHOD_LACKS_ANNOTATION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree,
        SuggestedFix.builder()
            .addImport("org.immutables.value.Value")
            .prefixWith(tree, "@Value.NaturalOrder ")
            .build());
  }
}
