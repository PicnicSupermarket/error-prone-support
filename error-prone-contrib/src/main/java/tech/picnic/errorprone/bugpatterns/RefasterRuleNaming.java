package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

// XXX: Support `BlockTemplate` naming.
// XXX: How to handle e.g. `ImmutableList.of(e1, e2)`.
/** A {@link BugChecker} that flags incorrectly named Refaster rules. */
@AutoService(BugChecker.class)
@BugPattern(
    linkType = CUSTOM,
    link = BUG_PATTERNS_BASE_URL + "RefasterRuleNaming",
    summary = "Apply naming algorithm",
    severity = ERROR)
public final class RefasterRuleNaming extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> AFTER_TEMPLATE_METHOD = hasAnnotation(AfterTemplate.class);

  private static final ImmutableMap<String, String> DEFAULT_PARAM_MAPPING =
      ImmutableMap.of(
          "int",
          "IntOnly",
          "int-int",
          "Ints",
          "string",
          "StringOnly",
          "string-int",
          "StringAndInt");

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!hasMatchingMember(tree, BEFORE_TEMPLATE_METHOD, state)) {
      /* This class does not contain a Refaster template. */
      return Description.NO_MATCH;
    }

    ImmutableList<MethodTree> collect =
        tree.getMembers().stream()
            .filter(member -> AFTER_TEMPLATE_METHOD.matches(member, state))
            .filter(MethodTree.class::isInstance)
            .map(MethodTree.class::cast)
            .collect(toImmutableList());

    if (collect.size() > 1) {
      return Description.NO_MATCH;
    }

    // XXX: Check if there is nicer way to get the only element from the list of members.
    MethodTree afterTemplate = Iterables.getOnlyElement(collect);
    String canonicalName = deduceCanonicalRefasterRuleName(afterTemplate, state).orElse("");
    return tree.getSimpleName().contentEquals(canonicalName)
        ? Description.NO_MATCH
        : buildDescription(tree)
            .setMessage("Refaster rule should be named: " + canonicalName)
            .build();
  }

  // XXX: Get the first After template.
  // XXX: Otherwise get the first beforetemplate and use that as import.
  // XXX: In that case, prefix with `Flag`.
  // XXX: Use the expression:
  //  1. Get the objects on which a method is invoked.
  //  2. Check if there are many overloads, if so specify the extra name.
  //  3. Look at what else is after that and repeat.
  @SuppressWarnings("SystemOut")
  private static Optional<String> deduceCanonicalRefasterRuleName(
      MethodTree tree, VisitorState state) {
    System.out.println("Tree: " + state.getSourceForNode(tree));
    StatementTree statement = tree.getBody().getStatements().get(0);
    if (!(statement instanceof ReturnTree)) {
      return Optional.empty();
    }

    ImmutableList<MethodInvocationTree> methodInvocations =
        getMethodInvocations((ReturnTree) statement).reverse();

    //    ExpressionTree expression = ((ReturnTree) statement).getExpression();

    StringBuilder test = new StringBuilder();
    for (MethodInvocationTree mit : methodInvocations) {
      Symbol symbol = ASTHelpers.getSymbol(mit.getMethodSelect());
      List<Symbol> methodsFromType =
          getMethodsFromType(symbol.owner.type, symbol.name.toString(), state);

      String start = symbol.owner.getSimpleName().toString();
      //      if (methodsFromType.size() == 1) {
      String simpleName = symbol.getSimpleName().toString();
      String firstLetter = simpleName.substring(0, 1).toUpperCase(Locale.ROOT);
      test.append(start).append(firstLetter).append(simpleName.substring(1));
      //      }
    }

    return Optional.of(test.toString());
  }

  @VisibleForTesting
  // XXX: Add tests.
  public String stringifyParams(ImmutableList<VarSymbol> params) {
    ImmutableList.Builder<String> builder = builder();
    for (VarSymbol param : params) {
      builder.add(param.type.tsym.name.toString());
    }
    return builder.build().stream().collect(Collectors.joining("-")).toLowerCase(Locale.ROOT);
  }

  // XXX: Is there a better way to do this?
  private static ImmutableList<MethodInvocationTree> getMethodInvocations(ReturnTree tree) {
    ImmutableList.Builder<MethodInvocationTree> nodes = ImmutableList.builder();

    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitMethodInvocation(
          MethodInvocationTree node, @Nullable Void unused) {
        nodes.add(node);
        return super.visitMethodInvocation(node, unused);
      }
    }.scan(tree, null);

    return nodes.build();
  }

  private static List<Symbol> getMethodsFromType(Type type, String name, VisitorState state) {
    List<Symbol> list = new ArrayList<>();
    type.tsym
        .members()
        .getSymbolsByName(state.getName(name))
        .iterator()
        .forEachRemaining(list::add);

    return list;
  }

  // XXX: Copied over from RuleModifiers.
  private static boolean hasMatchingMember(
      ClassTree tree, Matcher<Tree> matcher, VisitorState state) {
    return tree.getMembers().stream().anyMatch(member -> matcher.matches(member, state));
  }
}
