package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
import static com.google.errorprone.predicates.TypePredicates.allOf;
import static com.google.errorprone.predicates.TypePredicates.not;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypePredicates.hasTypeParameter;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypePredicates.isSubTypeOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.raw;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;

/**
 * A {@link BugChecker} that flags {@code Publisher<? extends Publisher>} instances, unless the
 * nested {@link org.reactivestreams.Publisher} is a {@link reactor.core.publisher.GroupedFlux}.
 */
// XXX: See the `NestedOptionals` check for some ideas on how to generalize this kind of checker.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        """
        Avoid `Publisher`s that emit other `Publishers`s; the resultant code is hard to reason \
        about""",
    link = BUG_PATTERNS_BASE_URL + "NestedPublishers",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class NestedPublishers extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> PUBLISHER = type("org.reactivestreams.Publisher");
  private static final Matcher<ExpressionTree> IS_NON_GROUPED_PUBLISHER_OF_PUBLISHERS =
      typePredicateMatcher(
          allOf(
              isSubTypeOf(generic(PUBLISHER, subOf(raw(PUBLISHER)))),
              not(
                  hasTypeParameter(
                      0, isSubTypeOf(raw(type("reactor.core.publisher.GroupedFlux")))))));

  /** Instantiates a new {@link NestedPublishers} instance. */
  public NestedPublishers() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return IS_NON_GROUPED_PUBLISHER_OF_PUBLISHERS.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }
}
