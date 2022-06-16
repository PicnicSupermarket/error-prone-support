package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.kindIs;
import static com.google.errorprone.matchers.Matchers.methodInvocation;
import static com.google.errorprone.matchers.Matchers.parentNode;
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
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.stream.Collector;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

/**
 * A {@link BugChecker} which flags {@link Collector} usages that don't clearly express
 * (im)mutability.
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

  private static final Matcher<MethodInvocationTree> MATCHER =
      Matchers.allOf(
          parentNode(
              allOf(
                  kindIs(Tree.Kind.METHOD_INVOCATION),
                  // `Matchers#parentNode()` requires as `Matcher<? extends Tree>` as parameter
                  // `Matchers#instanceMethod` provides a `Matcher<ExpressionTree>`,
                  // which cannot be casted safely. However, we assert the parent node is a
                  // `METHOD_INVOCATION`, so we can safely cast it manually.
                  (tree, state) ->
                      instanceMethod()
                          .onDescendantOfAny(Flux.class.getName(), Stream.class.getName())
                          .named("collect")
                          .matches((MethodInvocationTree) tree, state))),
          anyOf(
              staticMethod().onClass("java.util.stream.Collectors").namedAnyOf("toList", "toSet"),
              allOf(
                  staticMethod().onClass("java.util.stream.Collectors").named("toMap"),
                  argumentCount(2))));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    if (!MATCHER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Symbol collector = ASTHelpers.getSymbol(tree);
    if (collector == null) {
      return Description.NO_MATCH;
    }

    return createDescription(tree, collector, state);
  }

  private Description createDescription(
      MethodInvocationTree tree, Symbol collector, VisitorState state) {
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
