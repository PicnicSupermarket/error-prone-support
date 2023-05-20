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
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags nesting of {@link Publisher Publishers} inside {@link Mono
 * Monos}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid nesting `Publisher`s inside `Mono`s; the resultant code is hard to reason about",
    link = BUG_PATTERNS_BASE_URL + "MonoOfPublishers",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class MonoOfPublishers extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Supplier<Type> MONO_OF_PUBLISHERS =
      VisitorState.memoize(
          generic(MONO, subOf(generic(type("org.reactivestreams.Publisher"), unbound()))));

  /** Instantiates a new {@link MonoOfPublishers} instance. */
  public MonoOfPublishers() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    Type type = MONO_OF_PUBLISHERS.get(state);
    if (type == null || !state.getTypes().isSubtype(ASTHelpers.getType(tree), type)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree);
  }
}
