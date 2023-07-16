package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.unbound;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;

/** A {@link BugChecker} that flags nesting of {@link Publisher Publishers}. */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid nesting `Publisher`s inside `Publishers`s; "
            + "the resultant code is hard to reason about",
    link = BUG_PATTERNS_BASE_URL + "NestedPublishers",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class NestedPublishers extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> PUBLISHER = type("org.reactivestreams.Publisher");
  private static final Supplier<Type> PUBLISHER_OF_PUBLISHERS =
      VisitorState.memoize(generic(PUBLISHER, subOf(generic(PUBLISHER, unbound()))));
  private static final Supplier<Type> GROUPED_FLUX =
      VisitorState.memoize(
          generic(
              Suppliers.typeFromString("reactor.core.publisher.GroupedFlux"),
              unbound(),
              unbound()));

  /** Instantiates a new {@link NestedPublishers} instance. */
  public NestedPublishers() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    Type type = ASTHelpers.getType(tree);

    if (!isSubType(type, PUBLISHER_OF_PUBLISHERS.get(state), state)
        || isSubType(
            Iterables.getOnlyElement(type.getTypeArguments()), GROUPED_FLUX.get(state), state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree);
  }

  private static boolean isSubType(Type subType, @Nullable Type type, VisitorState state) {
    return type != null && state.getTypes().isSubtype(subType, type);
  }
}
