package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags Refaster template parameters with names that do not match the
 * method parameter they are passed to.
 *
 * <p>Parameter names are derived from the first method invocation in which the parameter appears as
 * an argument, considering template methods in priority order ({@code @AfterTemplate} first, then
 * {@code @BeforeTemplate}, with ties broken by descending parameter count).
 */
// XXX: Fully review this class.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Refaster template parameters should be named after the method parameter they are passed to",
    link = BUG_PATTERNS_BASE_URL + "RefasterParameterNaming",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class RefasterParameterNaming extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> REFASTER_METHOD =
      staticMethod().onClass(Refaster.class.getCanonicalName());
  private static final Matcher<ExpressionTree> REFASTER_AS_VARARGS =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("asVarargs");
  private static final Matcher<Tree> REPEATED_ANNOTATION =
      hasAnnotation(Repeated.class.getCanonicalName());
  private static final Pattern SYNTHETIC_PARAMETER_NAME = Pattern.compile("arg\\d+");

  /** Instantiates a new {@link RefasterParameterNaming} instance. */
  public RefasterParameterNaming() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<MethodTree> methods = MoreASTHelpers.getRefasterTemplateMethods(tree, state);
    if (methods.isEmpty()) {
      return Description.NO_MATCH;
    }

    ImmutableMap<String, String> renames = deriveParameterRenames(methods, state);
    return renames.isEmpty()
        ? Description.NO_MATCH
        : describeMatch(tree, renameParameters(methods, renames, state));
  }

  private static ImmutableMap<String, String> deriveParameterRenames(
      ImmutableList<MethodTree> methods, VisitorState state) {
    Set<String> processedParams = new LinkedHashSet<>();
    Map<String, String> renames = new LinkedHashMap<>();

    ImmutableSet<String> repeatedParams =
        methods.stream()
            .flatMap(m -> m.getParameters().stream())
            .filter(p -> REPEATED_ANNOTATION.matches(p, state))
            .map(p -> p.getName().toString())
            .collect(toImmutableSet());

    // XXX: We now scan the method definitions (though in the common case only the first
    // `@AfterTemplate`) once for each parameter. The alternative is to iterate over the method
    // definitions once, in order, and to inspect `IdentifierTree`s rather than
    // `MethodInvocationTree`s. This would make it easier to plug in additional naming strategies
    // later.
    for (MethodTree method : methods) {
      for (VariableTree param : method.getParameters()) {
        String paramName = param.getName().toString();
        if (!processedParams.add(paramName)) {
          continue;
        }

        deriveName(paramName, methods, repeatedParams, state)
            .ifPresent(derivedName -> renames.put(paramName, derivedName));
      }
    }

    /* Remove renames where the derived name matches the current name. */
    renames.entrySet().removeIf(e -> e.getKey().equals(e.getValue()));

    // XXX: Instead of dropping colliding renames unconditionally, per parameter we could collect
    // additional eligible renames, and then retain the first non-colliding option. But perhaps this
    // is too much.
    removeCollidingRenames(renames, processedParams);

    return ImmutableMap.copyOf(renames);
  }

  private static void removeCollidingRenames(Map<String, String> renames, Set<String> allParams) {
    Set<String> retainedNames = new LinkedHashSet<>(allParams);
    renames.forEach((oldName, newName) -> retainedNames.remove(oldName));
    renames
        .entrySet()
        .removeIf(
            e -> {
              if (!retainedNames.add(e.getValue())) {
                retainedNames.add(e.getKey());
                return true;
              }
              return false;
            });
  }

  private static Optional<String> deriveName(
      String paramName,
      ImmutableList<MethodTree> methods,
      ImmutableSet<String> repeatedParams,
      VisitorState state) {
    for (MethodTree method : methods) {
      String result =
          new TreeScanner<@Nullable String, @Nullable Void>() {
            @Override
            public @Nullable String visitMethodInvocation(
                MethodInvocationTree node, @Nullable Void unused) {
              if (REFASTER_METHOD.matches(node, state)) {
                return super.visitMethodInvocation(node, null);
              }

              MethodSymbol sym = ASTHelpers.getSymbol(node);
              // XXX: If the parameter is non-`@Repeated`, we could do a poor-man's version of
              // making the parameter name singular by dropping a trailing `s`, if any.
              int nonVarargsCount = sym.isVarArgs() ? sym.params().size() - 1 : sym.params().size();

              List<? extends ExpressionTree> args = node.getArguments();
              for (int i = 0; i < nonVarargsCount; i++) {
                Optional<String> name = unwrapParameterName(args.get(i), state);
                if (name.isPresent() && name.orElseThrow().equals(paramName)) {
                  String formalName = sym.params().get(i).name.toString();
                  if (!SYNTHETIC_PARAMETER_NAME.matcher(formalName).matches()) {
                    return formalName;
                  }
                }
              }

              if (sym.isVarArgs()) {
                int varargsIdx = sym.params().size() - 1;
                for (int i = varargsIdx; i < args.size(); i++) {
                  Optional<String> name = unwrapParameterName(args.get(i), state);
                  if (name.isPresent()
                      && name.orElseThrow().equals(paramName)
                      && repeatedParams.contains(paramName)) {
                    // XXX: This logic is repeated above; extract.
                    String formalName = sym.params().get(varargsIdx).name.toString();
                    if (!SYNTHETIC_PARAMETER_NAME.matcher(formalName).matches()) {
                      return formalName;
                    }
                  }
                }
              }

              return super.visitMethodInvocation(node, null);
            }

            @Override
            public @Nullable String reduce(@Nullable String next, @Nullable String current) {
              return current != null ? current : next;
            }
          }.scan(method.getBody(), null);

      if (result != null) {
        return Optional.of(result);
      }
    }

    return Optional.empty();
  }

  // XXX: Review method and parameter name.
  // XXX: In fact, change to `isIdentifier(ExpressionTree tree, String expectedName, VisitorState
  // state)`.
  private static Optional<String> unwrapParameterName(ExpressionTree arg, VisitorState state) {
    return arg instanceof MethodInvocationTree invocation
            && REFASTER_AS_VARARGS.matches(invocation, state)
        ? unwrapParameterName(Iterables.getOnlyElement(invocation.getArguments()), state)
        : arg instanceof IdentifierTree id
            ? Optional.of(id.getName().toString())
            : Optional.empty();
  }

  private static SuggestedFix renameParameters(
      ImmutableList<MethodTree> methods, ImmutableMap<String, String> renames, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (MethodTree method : methods) {
      for (VariableTree param : method.getParameters()) {
        String newName = renames.get(param.getName().toString());
        if (newName != null) {
          fix.merge(SuggestedFixes.renameVariable(param, newName, state));
        }
      }
    }
    return fix.build();
  }
}
