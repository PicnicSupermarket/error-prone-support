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
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags Refaster template parameters with names that do not match their
 * intended semantics.
 *
 * <p>Parameter names are derived through two strategies, applied in order:
 *
 * <ol>
 *   <li>From the first method invocation in which the parameter appears as an argument, considering
 *       template methods in priority order ({@code @AfterTemplate} first, then
 *       {@code @BeforeTemplate}, with ties broken by descending parameter count).
 *   <li>As a fallback, from the parameter's type: array types yield {@code array}; primitive types
 *       yield their first letter (e.g. {@code int} yields {@code i}); other types yield the last
 *       CamelCase word of the simple type name, lowercased, with well-known shorthands applied
 *       (e.g. {@code ImmutableList} yields {@code list}, {@code String} yields {@code str}, {@code
 *       Comparator} yields {@code cmp}). If the resulting name is a Java keyword, the two preceding
 *       CamelCase words are combined instead (e.g. {@code OptionalInt} yields {@code optionalInt}).
 * </ol>
 *
 * <p>When multiple parameters would receive the same derived name, numeric suffixes are appended to
 * disambiguate (e.g. {@code list1}, {@code list2}).
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
  private static final ImmutableList<String> STRIPPED_TYPE_PREFIXES =
      ImmutableList.of("Abstract", "Immutable");
  private static final ImmutableMap<String, String> TYPE_NAME_SHORTHANDS =
      ImmutableMap.of(
          "comparator", "cmp",
          "invocationOnMock", "invocation",
          "string", "str",
          "stringBuilder", "sb");

  /** Instantiates a new {@link RefasterParameterNaming} instance. */
  public RefasterParameterNaming() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<MethodTree> methods = MoreASTHelpers.getRefasterTemplateMethods(tree, state);
    // XXX: The early return here is a fast path: `deriveParameterRenames` also returns an empty
    // map when `methods` is empty, so the result is the same either way.
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
    Map<String, VariableTree> paramTrees = new LinkedHashMap<>();
    Map<String, String> renames = new LinkedHashMap<>();

    ImmutableSet<String> repeatedParams =
        methods.stream()
            .flatMap(m -> m.getParameters().stream())
            .filter(p -> REPEATED_ANNOTATION.matches(p, state))
            .map(p -> p.getName().toString())
            .collect(toImmutableSet());

    for (MethodTree method : methods) {
      for (VariableTree param : method.getParameters()) {
        String paramName = param.getName().toString();
        if (processedParams.add(paramName)) {
          paramTrees.put(paramName, param);
        }
      }
    }

    Set<String> unresolved = new LinkedHashSet<>(processedParams);
    for (MethodTree method : methods) {
      if (unresolved.isEmpty()) {
        break;
      }
      ImmutableMap<String, String> found =
          scanMethodForNames(
              method, ImmutableSet.copyOf(unresolved), repeatedParams, state);
      for (Map.Entry<String, String> e : found.entrySet()) {
        renames.put(e.getKey(), e.getValue());
        unresolved.remove(e.getKey());
      }
    }

    for (String paramName : unresolved) {
      deriveNameFromType(paramTrees.get(paramName))
          .ifPresent(name -> renames.put(paramName, name));
    }

    /* Apply name prefix implied by @Matches annotations (check all template methods). */
    Map<String, String> matchesPrefixes = new LinkedHashMap<>();
    for (MethodTree method : methods) {
      for (VariableTree param : method.getParameters()) {
        String paramName = param.getName().toString();
        if (!matchesPrefixes.containsKey(paramName)) {
          getMatchesPrefix(param).ifPresent(prefix -> matchesPrefixes.put(paramName, prefix));
        }
      }
    }
    for (Map.Entry<String, String> entry : matchesPrefixes.entrySet()) {
      String paramName = entry.getKey();
      String prefix = entry.getValue();
      String base = renames.getOrDefault(paramName, paramName);
      renames.put(paramName, prefix + Character.toUpperCase(base.charAt(0)) + base.substring(1));
    }

    /* Remove renames where the derived name matches the current name. */
    renames.entrySet().removeIf(e -> e.getKey().equals(e.getValue()));

    resolveConflictsWithSuffix(renames, processedParams);

    /* Remove any no-op renames that suffix resolution may have introduced. */
    renames.entrySet().removeIf(e -> e.getKey().equals(e.getValue()));

    return ImmutableMap.copyOf(renames);
  }

  private static void resolveConflictsWithSuffix(
      Map<String, String> renames, Set<String> allParams) {
    /* Names of params that keep their current name (not being renamed). */
    Set<String> fixedNames = new LinkedHashSet<>(allParams);
    fixedNames.removeAll(renames.keySet());

    /* Group renames by target name, preserving parameter order. */
    Map<String, List<String>> byTarget = new LinkedHashMap<>();
    renames.forEach((from, to) -> byTarget.computeIfAbsent(to, k -> new ArrayList<>()).add(from));

    Set<String> occupiedNames = new LinkedHashSet<>(fixedNames);
    for (Map.Entry<String, List<String>> entry : byTarget.entrySet()) {
      String target = entry.getKey();
      List<String> sources = entry.getValue();
      if (sources.size() == 1 && !occupiedNames.contains(target)) {
        occupiedNames.add(target);
      } else {
        @Var int counter = 1;
        for (String source : sources) {
          @Var String candidate;
          do {
            candidate = target + counter++;
          } while (occupiedNames.contains(candidate));
          occupiedNames.add(candidate);
          renames.put(source, candidate);
        }
      }
    }
  }

  private static Optional<String> deriveNameFromType(VariableTree param) {
    Type type = ASTHelpers.getType(param);
    // XXX: `ASTHelpers.getType` returns null only for error or synthetic nodes, which never appear
    // in valid Refaster templates, so this guard is not observable in tests.
    if (type == null) {
      return Optional.empty();
    }

    // XXX: `ArrayType.tsym.getSimpleName()` returns "Array", which the logic below would also map
    // to "array". The explicit check is retained for clarity of intent.
    if (type instanceof Type.ArrayType) {
      return Optional.of("array");
    }

    if (type instanceof Type.TypeVar) {
      return Optional.empty();
    }

    String simpleName = type.tsym.getSimpleName().toString();
    // XXX: `simpleName` is empty only for anonymous or synthetic types, which do not appear as
    // Refaster template parameter types, so this guard is not observable in tests.
    if (simpleName.isEmpty()) {
      return Optional.empty();
    }

    if (type.isPrimitive()) {
      return Optional.of(String.valueOf(simpleName.charAt(0)));
    }

    // Try the prefix-stripped name first; fall back to the full name.
    for (String candidate : ImmutableList.of(stripTypePrefix(simpleName), simpleName)) {
      String base = Character.toLowerCase(candidate.charAt(0)) + candidate.substring(1);
      String name = TYPE_NAME_SHORTHANDS.getOrDefault(base, base);
      if (!SourceVersion.isKeyword(name)) {
        return Optional.of(name);
      }
    }

    return Optional.empty();
  }

  private static String stripTypePrefix(String simpleName) {
    for (String prefix : STRIPPED_TYPE_PREFIXES) {
      if (simpleName.startsWith(prefix) && simpleName.length() > prefix.length()) {
        return simpleName.substring(prefix.length());
      }
    }
    return simpleName;
  }

  /**
   * Returns the name prefix implied by a {@code @Matches} annotation on the given parameter, if
   * any. Specifically, {@code @Matches(IsIdentityOperation.class)} yields {@code "identity"} and
   * {@code @Matches(IsEmpty.class)} yields {@code "empty"}.
   */
  private static Optional<String> getMatchesPrefix(VariableTree param) {
    for (AnnotationTree ann : param.getModifiers().getAnnotations()) {
      String annType = ann.getAnnotationType().toString();
      if (!annType.equals("Matches") && !annType.endsWith(".Matches")) {
        continue;
      }
      for (ExpressionTree arg : ann.getArguments()) {
        String argSrc =
            arg instanceof AssignmentTree assign
                ? assign.getExpression().toString()
                : arg.toString();
        if (argSrc.contains("IsIdentityOperation")) {
          return Optional.of("identity");
        }
        if (argSrc.contains("IsEmpty")) {
          return Optional.of("empty");
        }
      }
    }
    return Optional.empty();
  }

  private static ImmutableMap<String, String> scanMethodForNames(
      MethodTree method,
      ImmutableSet<String> paramNames,
      ImmutableSet<String> repeatedParams,
      VisitorState state) {
    Map<String, String> result = new LinkedHashMap<>();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitMethodInvocation(
          MethodInvocationTree node, @Nullable Void unused) {
        // XXX: When the Refaster check is removed, Refaster methods are treated as regular
        // methods. Their formal parameter names are either synthetic (filtered by
        // `SYNTHETIC_PARAMETER_NAME`) or have names that wouldn't change the outcome for
        // parameters in practice, so the result is the same either way.
        if (REFASTER_METHOD.matches(node, state)) {
          return super.visitMethodInvocation(node, null);
        }

        MethodSymbol sym = ASTHelpers.getSymbol(node);
        List<? extends ExpressionTree> args = node.getArguments();
        int nonVarargsCount = sym.isVarArgs() ? sym.params().size() - 1 : sym.params().size();

        for (int i = 0; i < nonVarargsCount; i++) {
          Optional<String> name = unwrapParameterName(args.get(i), state);
          if (name.isPresent() && paramNames.contains(name.orElseThrow())) {
            String formalName = sym.params().get(i).name.toString();
            if (!SYNTHETIC_PARAMETER_NAME.matcher(formalName).matches()) {
              result.putIfAbsent(name.orElseThrow(), formalName);
            }
          }
        }

        if (sym.isVarArgs()) {
          int varargsIdx = sym.params().size() - 1;
          String formalName = sym.params().get(varargsIdx).name.toString();
          if (!SYNTHETIC_PARAMETER_NAME.matcher(formalName).matches()) {
            for (int i = varargsIdx; i < args.size(); i++) {
              Optional<String> name = unwrapParameterName(args.get(i), state);
              if (name.isPresent()
                  && paramNames.contains(name.orElseThrow())
                  && repeatedParams.contains(name.orElseThrow())) {
                result.putIfAbsent(name.orElseThrow(), formalName);
              }
            }
          }
        }

        return super.visitMethodInvocation(node, null);
      }

      @Override
      public @Nullable Void visitNewArray(NewArrayTree node, @Nullable Void unused) {
        for (ExpressionTree dim : node.getDimensions()) {
          if (dim instanceof IdentifierTree id) {
            String paramName = id.getName().toString();
            if (paramNames.contains(paramName) && !repeatedParams.contains(paramName)) {
              result.putIfAbsent(paramName, "size");
            }
          }
        }
        return super.visitNewArray(node, null);
      }

      @Override
      public @Nullable Void visitIf(IfTree node, @Nullable Void unused) {
        ExpressionTree cond =
            stripLogicalComplement(ASTHelpers.stripParentheses(node.getCondition()));
        if (cond instanceof IdentifierTree id) {
          String paramName = id.getName().toString();
          if (paramNames.contains(paramName) && !repeatedParams.contains(paramName)) {
            result.putIfAbsent(paramName, "condition");
          }
        }
        return super.visitIf(node, null);
      }
    }.scan(method.getBody(), null);
    return ImmutableMap.copyOf(result);
  }

  private static ExpressionTree stripLogicalComplement(ExpressionTree expr) {
    return expr instanceof UnaryTree unary
            && unary.getKind() == com.sun.source.tree.Tree.Kind.LOGICAL_COMPLEMENT
        ? unary.getExpression()
        : expr;
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
