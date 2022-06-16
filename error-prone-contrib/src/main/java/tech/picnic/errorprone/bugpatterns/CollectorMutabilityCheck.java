package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A {@link BugChecker} which flags {@link Collector} usage that doesn't emphasize (im)mutability.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "CollectorMutability",
    summary = "`#collect(to{List,Map,Set}())` doesn't emphasize (im)mutability",
    linkType = NONE,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class CollectorMutabilityCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final Matcher<ExpressionTree> COLLECTOR =
      staticMethod().onClass("java.util.stream.Collectors").namedAnyOf("toList", "toSet", "toMap");

  private static final Matcher<ExpressionTree> COLLECTOR_USAGE =
      instanceMethod()
          .onDescendantOfAny("reactor.core.publisher.Flux", "java.util.stream.Stream")
          .namedAnyOf("collect")
          .withParameters(Collector.class.getName());

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!COLLECTOR.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Symbol collector = ASTHelpers.getSymbol(tree);
    if (collector == null
        || state.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
      return Description.NO_MATCH;
    }

    MethodInvocationTree parent = (MethodInvocationTree) state.getPath().getParentPath().getLeaf();
    if (!COLLECTOR_USAGE.matches(parent, state)) {
      return Description.NO_MATCH;
    }

    // filter out toMap with more than 4 arguments, as these already emphasize the (im)mutability
    if (collector.name.contentEquals("toMap") && tree.getArguments().size() == 4) {
      return Description.NO_MATCH;
    }

    return createDescription(tree, state, collector);
  }

  /**
   * Build a {@link Description} for the provided {@link java.util.stream.Collectors} method.
   *
   * @param tree the method invocation tree
   * @param state the visitor state
   * @param collector the collector method symbol
   * @return a description appropriate for the specific collectors method
   */
  private Description createDescription(
      MethodInvocationTree tree, VisitorState state, Symbol collector) {
    switch (collector.name.toString()) {
      case "toList":
        return buildSimpleDescription(tree, state, "toImmutableList", "ArrayList");
      case "toSet":
        return buildSimpleDescription(tree, state, "toImmutableSet", "HashSet");
      case "toMap":
        return buildMapDescription(tree, state);
      default:
        return Description.NO_MATCH;
    }
  }

  /**
   * Build a {@link Description} for a simple collector method with suggested fixes to provide
   * emphasis on (im)mutability.
   *
   * @param tree the method invocation tree of the collector method
   * @param state the visitor state
   * @param immutable the name of the immutable collector method to use instead
   * @param mutable the name of the mutable collection class to use
   * @return a description with two suggested fixes on how to provide more emphasis on
   *     (im)mutability
   */
  private Description buildSimpleDescription(
      MethodInvocationTree tree, VisitorState state, String immutable, String mutable) {
    return buildDescription(tree)
        .addFix(SuggestedFixes.renameMethodInvocation(tree, immutable, state))
        .addFix(
            SuggestedFix.builder()
                .replace(tree, String.format("toCollection(%s::new)", mutable))
                .addImport(String.format("java.util.%s", mutable))
                .addStaticImport("java.util.stream.Collectors.toCollection")
                .build())
        .build();
  }

  /**
   * Build a {@link Description} for a {@link java.util.stream.Collectors#toMap(Function, Function)}
   * method with two suggested fixes to provide more emphasis on (im)mutability
   *
   * @param tree the method invocation tree
   * @param state the visitor state
   * @return a description with two suggested fixes to provide more emphasis on (im)mutability
   */
  private Description buildMapDescription(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree keyMapper = tree.getArguments().get(0);
    ExpressionTree valueMapper = tree.getArguments().get(1);

    return buildDescription(tree)
        .addFix(SuggestedFixes.renameMethodInvocation(tree, "toImmutableMap", state))
        .addFix(
            SuggestedFix.builder()
                .replace(
                    tree,
                    String.format(
                        "toMap(%s, %s, (a, b) -> a, HashMap::new)",
                        state.getSourceForNode(keyMapper), state.getSourceForNode(valueMapper)))
                .addImport("java.util.HashMap")
                .build())
        .build();
  }
}
