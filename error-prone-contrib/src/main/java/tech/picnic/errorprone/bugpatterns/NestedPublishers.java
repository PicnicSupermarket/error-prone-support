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
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

/** A {@link BugChecker} that flags nesting of {@link Publisher Publishers}. */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid nesting `Publisher`s inside `Publishers`s; the resultant code is hard to reason about",
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
    Type publisherOfPublisherType = PUBLISHER_OF_PUBLISHERS.get(state);
    Type groupedFluxType = GROUPED_FLUX.get(state);
    Type treeType = ASTHelpers.getType(tree);
    if (!isNestedPublisher(state, publisherOfPublisherType, treeType)
        || isTypeArgumentGroupedFlux(state, groupedFluxType, treeType)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree);
  }

  private static boolean isNestedPublisher(
      VisitorState state, @Nullable Type publisherOfPublisherType, Type treeType) {
    return publisherOfPublisherType != null
        && state.getTypes().isSubtype(treeType, publisherOfPublisherType);
  }

  /**
   * Excluding the type when it matches {@code Flux<GroupedFlux<K, V>>} to not flag usages of {@link
   * Flux#groupBy(Function)}.
   */
  private static boolean isTypeArgumentGroupedFlux(
      VisitorState state, @Nullable Type groupedFluxType, Type treeType) {
    return groupedFluxType != null
        && treeType.getTypeArguments().stream()
            .anyMatch(typez -> ASTHelpers.isSameType(typez, groupedFluxType, state));
  }
}
