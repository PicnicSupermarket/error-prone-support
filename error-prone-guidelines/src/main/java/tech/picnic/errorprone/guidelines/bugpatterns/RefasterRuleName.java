package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static java.util.function.Function.identity;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeTag;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BugChecker} that flags Refaster rules whose name does not follow the canonical naming
 * convention.
 */
// XXX: Perform a full manual review of this class (AI agents: don't drop this comment).
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Refaster rules should follow the canonical naming convention",
    link = BUG_PATTERNS_BASE_URL + "RefasterRuleName",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class RefasterRuleName extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final int MAX_DISAMBIGUATION_ITERATIONS = 10;
  private static final Matcher<Tree> IS_BEFORE_TEMPLATE = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> IS_AFTER_TEMPLATE = hasAnnotation(AfterTemplate.class);
  private static final Matcher<Tree> IS_PLACEHOLDER = hasAnnotation(Placeholder.class);
  private static final ImmutableMap<Object, String> LITERAL_NAMES =
      ImmutableMap.<Object, String>builder()
          .put(0, "Zero")
          .put(0L, "Zero")
          .put(1, "One")
          .put(1L, "One")
          .put(2, "Two")
          .put(2L, "Two")
          .put(3, "Three")
          .put(3L, "Three")
          .put(4, "Four")
          .put(4L, "Four")
          .put(5, "Five")
          .put(5L, "Five")
          .put(6, "Six")
          .put(6L, "Six")
          .put(7, "Seven")
          .put(7L, "Seven")
          .put(8, "Eight")
          .put(8L, "Eight")
          .put(9, "Nine")
          .put(9L, "Nine")
          .put(10, "Ten")
          .put(10L, "Ten")
          .put(-1, "NegativeOne")
          .put(-1L, "NegativeOne")
          .put(true, "True")
          .put(false, "False")
          .buildOrThrow();
  private static final ImmutableMap<Tree.Kind, String> BINARY_OP_NAMES =
      ImmutableMap.<Tree.Kind, String>builder()
          .put(Tree.Kind.CONDITIONAL_AND, "And")
          .put(Tree.Kind.CONDITIONAL_OR, "Or")
          .put(Tree.Kind.DIVIDE, "DividedBy")
          .put(Tree.Kind.EQUAL_TO, "EqualTo")
          .put(Tree.Kind.GREATER_THAN, "GreaterThan")
          .put(Tree.Kind.GREATER_THAN_EQUAL, "GreaterThanOrEqualTo")
          .put(Tree.Kind.LESS_THAN, "LessThan")
          .put(Tree.Kind.LESS_THAN_EQUAL, "LessThanOrEqualTo")
          .put(Tree.Kind.MINUS, "Minus")
          .put(Tree.Kind.MULTIPLY, "Times")
          .put(Tree.Kind.NOT_EQUAL_TO, "NotEqualTo")
          .put(Tree.Kind.PLUS, "Plus")
          .put(Tree.Kind.REMAINDER, "Modulo")
          .buildOrThrow();
  private static final ImmutableMap<Tree.Kind, String> UNARY_OP_NAMES =
      ImmutableMap.of(
          Tree.Kind.LOGICAL_COMPLEMENT, "Not",
          Tree.Kind.UNARY_MINUS, "Negate");

  /** Instantiates a new {@link RefasterRuleName} instance. */
  public RefasterRuleName() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<RuleInfo> rules = collectRules(tree, state);
    if (rules.isEmpty()) {
      return Description.NO_MATCH;
    }

    ImmutableListMultimap<String, RuleInfo> grouped =
        Multimaps.index(rules, RuleInfo::derivedBaseName);

    for (String baseName : grouped.keySet()) {
      ImmutableList<RuleInfo> group = grouped.get(baseName);
      if (group.size() == 1) {
        RuleInfo rule = group.getFirst();
        reportIfMisnamed(rule, rule.derivedBaseName(), state);
      } else {
        disambiguateAndReport(group, state);
      }
    }

    return Description.NO_MATCH;
  }

  private static ImmutableList<RuleInfo> collectRules(ClassTree outerClass, VisitorState state) {
    ImmutableList.Builder<RuleInfo> rules = ImmutableList.builder();

    for (Tree member : outerClass.getMembers()) {
      if (!(member instanceof ClassTree innerClass)) {
        continue;
      }
      if (!hasBeforeTemplate(innerClass, state)) {
        continue;
      }

      Optional<MethodTree> maybeAfterMethod = findFirstAfterTemplate(innerClass, state);
      if (maybeAfterMethod.isEmpty()) {
        continue;
      }

      MethodTree afterMethod = maybeAfterMethod.orElseThrow();
      ImmutableSet<Symbol> placeholderSymbols = collectPlaceholderSymbols(innerClass, state);
      String derivedName = deriveName(afterMethod, placeholderSymbols);
      if (derivedName.isEmpty()) {
        /* Cannot derive a name (e.g., after-template consists only of placeholder invocations). */
        continue;
      }

      rules.add(new RuleInfo(innerClass, afterMethod, derivedName));
    }

    return rules.build();
  }

  private static boolean hasBeforeTemplate(ClassTree tree, VisitorState state) {
    return tree.getMembers().stream().anyMatch(m -> IS_BEFORE_TEMPLATE.matches(m, state));
  }

  private static Optional<MethodTree> findFirstAfterTemplate(ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(m -> IS_AFTER_TEMPLATE.matches(m, state))
        .findFirst()
        .map(MethodTree.class::cast);
  }

  private static ImmutableSet<Symbol> collectPlaceholderSymbols(
      ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(m -> IS_PLACEHOLDER.matches(m, state))
        .map(ASTHelpers::getSymbol)
        .collect(toImmutableSet());
  }

  private static String deriveName(
      MethodTree afterMethod, ImmutableSet<Symbol> placeholderSymbols) {
    ImmutableSet<VarSymbol> parameterSymbols =
        afterMethod.getParameters().stream().map(ASTHelpers::getSymbol).collect(toImmutableSet());

    List<? extends StatementTree> statements = afterMethod.getBody().getStatements();

    /* Check for identity rule: single return of a non-placeholder parameter. */
    if (statements.size() == 1
        && statements.getFirst() instanceof ReturnTree returnTree
        && returnTree.getExpression() instanceof IdentifierTree identTree) {
      Symbol sym = ASTHelpers.getSymbol(identTree);
      if (sym instanceof VarSymbol varSym
          && parameterSymbols.contains(varSym)
          && !placeholderSymbols.contains(varSym)) {
        return getDisambiguatingTypeName(varSym.type) + "Identity";
      }
    }

    NameDerivationScanner scanner = new NameDerivationScanner(parameterSymbols, placeholderSymbols);
    ImmutableList.Builder<String> fragments = ImmutableList.builder();
    Consumer<String> sink = fragments::add;

    boolean singleReturn = statements.size() == 1 && statements.getFirst() instanceof ReturnTree;

    for (StatementTree stmt : statements) {
      switch (stmt) {
        case ReturnTree returnTree -> {
          if (!singleReturn) {
            sink.accept("Return");
          }
          if (returnTree.getExpression() != null) {
            scanner.scan(returnTree.getExpression(), sink);
          }
        }
        case ExpressionStatementTree exprStmt -> scanner.scan(exprStmt.getExpression(), sink);
        default -> {}
      }
    }

    return String.join("", fragments.build());
  }

  private void disambiguateAndReport(ImmutableList<RuleInfo> group, VisitorState state) {
    /* Strategy 1: Expression vs. Block. */
    if (disambiguateByExpressionBlock(group, state)) {
      return;
    }

    /* Strategy 2: Unified parameter-based disambiguation. */
    if (disambiguateByParameter(group, state)) {
      return;
    }

    /* Strategy 3: Return type disambiguation. */
    if (disambiguateByReturnType(group, state)) {
      return;
    }

    /* Strategy 4: Cannot disambiguate automatically. */
    for (RuleInfo rule : group) {
      if (!isSuppressed(rule.classTree(), state)) {
        state.reportMatch(
            buildDescription(rule.classTree())
                .setMessage(
                    "Multiple rules derive the base name '%s'; manual disambiguation is required"
                        .formatted(rule.derivedBaseName()))
                .build());
      }
    }
  }

  private boolean disambiguateByExpressionBlock(ImmutableList<RuleInfo> group, VisitorState state) {
    if (!isExpressionBlockPair(group)) {
      return false;
    }

    for (RuleInfo rule : group) {
      reportIfMisnamed(rule, rule.derivedBaseName() + expressionBlockSuffix(rule), state);
    }
    return true;
  }

  private boolean disambiguateByParameter(ImmutableList<RuleInfo> group, VisitorState state) {
    int baseCount = getMinParamCount(group);
    boolean allSameArity =
        group.stream().allMatch(r -> r.afterMethod().getParameters().size() == baseCount);
    Optional<Map<RuleInfo, String>> suffixes =
        allSameArity
            ? computeSameAritySuffixes(group)
            : computeMixedAritySuffixes(group, baseCount);

    if (suffixes.isEmpty()) {
      return false;
    }

    /*
     * Verify all final suffixes are unique; rules in the same group share a base name, so suffix
     * uniqueness implies full name uniqueness.
     */
    Map<RuleInfo, String> resolvedSuffixes = suffixes.orElseThrow();
    if (ImmutableSet.copyOf(resolvedSuffixes.values()).size() < resolvedSuffixes.size()) {
      return false;
    }

    resolvedSuffixes.forEach(
        (rule, suffix) -> reportIfMisnamed(rule, rule.derivedBaseName() + suffix, state));
    return true;
  }

  @SuppressWarnings("NullAway" /* `returnTypes` is keyed on `group` elements; lookups are safe. */)
  private boolean disambiguateByReturnType(ImmutableList<RuleInfo> group, VisitorState state) {
    ImmutableMap<RuleInfo, Type> returnTypes =
        Maps.toMap(group, rule -> ASTHelpers.getSymbol(rule.afterMethod()).getReturnType());

    /* Try raw return type disambiguation. */
    ImmutableListMultimap<String, RuleInfo> rawGroups =
        Multimaps.index(group, r -> getDisambiguatingTypeName(returnTypes.get(r)));
    if (rawGroups.keySet().size() > 1 && rawGroups.keySet().size() == rawGroups.size()) {
      for (RuleInfo rule : group) {
        reportIfMisnamed(
            rule, rule.derivedBaseName() + getDisambiguatingTypeName(returnTypes.get(rule)), state);
      }
      return true;
    }

    /* Same raw type: try type argument disambiguation. */
    if (returnTypes.values().stream().anyMatch(t -> t.getTypeArguments().isEmpty())) {
      return false;
    }

    int numArgs = returnTypes.get(group.getFirst()).getTypeArguments().size();
    for (int argIdx = 0; argIdx < numArgs; argIdx++) {
      int idx = argIdx;
      if (returnTypes.values().stream().anyMatch(t -> idx >= t.getTypeArguments().size())) {
        continue;
      }
      ImmutableListMultimap<String, RuleInfo> argGroups =
          Multimaps.index(
              group,
              r -> getDisambiguatingTypeName(returnTypes.get(r).getTypeArguments().get(idx)));
      if (argGroups.keySet().size() > 1 && argGroups.keySet().size() == argGroups.size()) {
        for (RuleInfo rule : group) {
          String suffix =
              getDisambiguatingTypeName(returnTypes.get(rule).getTypeArguments().get(argIdx));
          reportIfMisnamed(rule, rule.derivedBaseName() + suffix, state);
        }
        return true;
      }
    }

    return false;
  }

  private void reportIfMisnamed(RuleInfo rule, String expectedName, VisitorState state) {
    String currentName = rule.classTree().getSimpleName().toString();
    if (currentName.equals(expectedName)) {
      return;
    }

    if (isSuppressed(rule.classTree(), state)) {
      return;
    }

    state.reportMatch(
        describeMatch(
            rule.classTree(),
            SuggestedFixes.renameClassWithUses(rule.classTree(), expectedName, state)));
  }

  private static Optional<Map<RuleInfo, String>> computeSameAritySuffixes(
      ImmutableList<RuleInfo> group) {
    Map<RuleInfo, String> suffixes = new LinkedHashMap<>();

    /* Split by @Repeated. */
    group.stream()
        .filter(RefasterRuleName::hasAnyRepeatedParam)
        .forEach(rule -> suffixes.put(rule, "Varargs"));
    ImmutableList<RuleInfo> nonRepeated =
        group.stream().filter(rule -> !hasAnyRepeatedParam(rule)).collect(toImmutableList());

    if (nonRepeated.isEmpty()) {
      return Optional.empty();
    }
    if (nonRepeated.size() == 1) {
      suffixes.put(nonRepeated.getFirst(), "");
      return Optional.of(suffixes);
    }

    /* Type disambiguation for the non-@Repeated rules. */
    if (!assignTypeSuffixes(nonRepeated, suffixes)) {
      return Optional.empty();
    }
    return Optional.of(suffixes);
  }

  private static Optional<Map<RuleInfo, String>> computeMixedAritySuffixes(
      ImmutableList<RuleInfo> group, int baseCount) {
    ImmutableList<ImmutableList<String>> allBaseParamTypeNames =
        group.stream()
            .filter(rule -> rule.afterMethod().getParameters().size() == baseCount)
            .map(
                rule ->
                    rule.afterMethod().getParameters().stream()
                        .map(RefasterRuleName::paramTypeName)
                        .collect(toImmutableList()))
            .collect(toImmutableList());

    ImmutableListMultimap<RuleInfo, String> extraTypeNames =
        group.stream()
            .filter(rule -> rule.afterMethod().getParameters().size() > baseCount)
            .collect(
                flatteningToImmutableListMultimap(
                    identity(),
                    rule -> getExtraParamTypeNames(rule, allBaseParamTypeNames).stream()));

    /*
     * If any extra-param type list would produce a suffix with repeated type names (e.g.,
     * "WithEnumAndEnum"), use arity numbering for all non-Varargs rules instead.
     */
    boolean useArityNumbers =
        extraTypeNames.keySet().stream()
            .map(extraTypeNames::get)
            .anyMatch(RefasterRuleName::hasRepeatedTypeAmong);

    Map<RuleInfo, String> suffixes = new LinkedHashMap<>();
    for (RuleInfo rule : group) {
      int arity = rule.afterMethod().getParameters().size();
      if (arity == baseCount) {
        suffixes.put(rule, useArityNumbers ? String.valueOf(arity) : "");
      } else {
        ImmutableList<String> extraNames = extraTypeNames.get(rule);
        if (extraNames.stream().allMatch("Varargs"::equals)) {
          suffixes.put(rule, "Varargs");
        } else {
          suffixes.put(rule, useArityNumbers ? String.valueOf(arity) : buildWithSuffix(extraNames));
        }
      }
    }

    if (!resolveAllClashes(group, suffixes)) {
      return Optional.empty();
    }

    return Optional.of(suffixes);
  }

  private static boolean hasRepeatedTypeAmong(ImmutableList<String> typeNames) {
    ImmutableList<String> nonVarargs =
        typeNames.stream().filter(n -> !"Varargs".equals(n)).collect(toImmutableList());
    return ImmutableSet.copyOf(nonVarargs).size() < nonVarargs.size();
  }

  private static boolean resolveAllClashes(
      ImmutableList<RuleInfo> group, Map<RuleInfo, String> suffixes) {
    for (int iteration = 0; iteration < MAX_DISAMBIGUATION_ITERATIONS; iteration++) {
      ImmutableListMultimap<String, RuleInfo> subGroups =
          ImmutableListMultimap.copyOf(suffixes.entrySet()).inverse();

      Optional<ImmutableList<RuleInfo>> firstClash =
          subGroups.keySet().stream().map(subGroups::get).filter(l -> l.size() > 1).findFirst();
      if (firstClash.isEmpty()) {
        return true;
      }

      ImmutableList<RuleInfo> clash = firstClash.orElseThrow();
      if (!splitByRepeated(clash, suffixes)
          && !resolveClashByType(clash, suffixes, group)
          && !numberByArity(clash, suffixes)
          && !splitByExpressionBlock(clash, suffixes)) {
        return false;
      }
    }
    return false;
  }

  private static boolean splitByRepeated(
      ImmutableList<RuleInfo> clash, Map<RuleInfo, String> suffixes) {
    ImmutableList<RuleInfo> toMarkVarargs =
        clash.stream()
            .filter(r -> hasAnyRepeatedParam(r) && !"Varargs".equals(suffixes.get(r)))
            .collect(toImmutableList());
    if (toMarkVarargs.isEmpty() || toMarkVarargs.size() == clash.size()) {
      return false;
    }
    toMarkVarargs.forEach(r -> suffixes.put(r, "Varargs"));
    return true;
  }

  private static boolean resolveClashByType(
      ImmutableList<RuleInfo> clash,
      Map<RuleInfo, String> suffixes,
      ImmutableList<RuleInfo> fullGroup) {
    int minArity = getMinParamCount(clash);
    for (int pos = 0; pos < minArity; pos++) {
      ImmutableListMultimap<String, RuleInfo> typeGroups = groupByTypeAtPosition(clash, pos);
      if (typeGroups.keySet().size() <= 1) {
        continue;
      }

      /*
       * Types that appear in this clash exactly once AND only in this clash (not shared with
       * other arity groups at this position) get type suffixes. Types shared with other arity
       * groups keep their existing suffix (e.g., an arity number).
       */
      ImmutableListMultimap<String, RuleInfo> globalTypeGroups =
          groupByTypeAtPosition(fullGroup, pos);
      for (String typeName : typeGroups.keySet()) {
        ImmutableList<RuleInfo> rulesWithType = typeGroups.get(typeName);
        if (rulesWithType.size() == 1 && globalTypeGroups.get(typeName).size() == 1) {
          suffixes.put(rulesWithType.getFirst(), typeName);
        }
      }

      if (clash.stream().map(suffixes::get).distinct().count() == clash.size()) {
        return true;
      }

      /* Fall back: compose type suffixes with existing suffixes for all rules in the clash. */
      if (typeGroups.keySet().size() == typeGroups.size()) {
        for (String typeName : typeGroups.keySet()) {
          RuleInfo rule = typeGroups.get(typeName).getFirst();
          suffixes.put(rule, typeName + suffixes.get(rule));
        }
        return true;
      }
    }
    return false;
  }

  private static boolean numberByArity(
      ImmutableList<RuleInfo> clash, Map<RuleInfo, String> suffixes) {
    long distinctArities =
        clash.stream().map(r -> r.afterMethod().getParameters().size()).distinct().count();
    if (distinctArities < clash.size()) {
      return false;
    }
    clash.forEach(r -> suffixes.put(r, String.valueOf(r.afterMethod().getParameters().size())));
    return true;
  }

  private static boolean splitByExpressionBlock(
      ImmutableList<RuleInfo> clash, Map<RuleInfo, String> suffixes) {
    if (!isExpressionBlockPair(clash)) {
      return false;
    }

    for (RuleInfo rule : clash) {
      suffixes.put(rule, suffixes.get(rule) + expressionBlockSuffix(rule));
    }
    return true;
  }

  private static boolean assignTypeSuffixes(
      ImmutableList<RuleInfo> rules, Map<RuleInfo, String> suffixes) {
    int arity = rules.getFirst().afterMethod().getParameters().size();

    /* Try single-position disambiguation first. */
    for (int pos = 0; pos < arity; pos++) {
      ImmutableListMultimap<String, RuleInfo> typeGroups = groupByTypeAtPosition(rules, pos);
      if (typeGroups.keySet().size() <= 1) {
        continue;
      }
      if (typeGroups.keySet().size() == typeGroups.size()) {
        for (String typeName : typeGroups.keySet()) {
          suffixes.put(typeGroups.get(typeName).getFirst(), typeName);
        }
        return true;
      }
    }

    /* Fall back to multi-position disambiguation: try pairs of positions. */
    for (int i = 0; i < arity; i++) {
      for (int j = i + 1; j < arity; j++) {
        ImmutableListMultimap<String, RuleInfo> tupleGroups = groupByTypeAtPositions(rules, i, j);
        if (tupleGroups.keySet().size() == tupleGroups.size()) {
          for (String tuple : tupleGroups.keySet()) {
            suffixes.put(tupleGroups.get(tuple).getFirst(), tuple);
          }
          return true;
        }
      }
    }

    return false;
  }

  private static ImmutableListMultimap<String, RuleInfo> groupByTypeAtPositions(
      ImmutableList<RuleInfo> rules, int pos1, int pos2) {
    return rules.stream()
        .filter(rule -> rule.afterMethod().getParameters().size() > Math.max(pos1, pos2))
        .collect(
            toImmutableListMultimap(
                rule -> {
                  List<? extends VariableTree> params = rule.afterMethod().getParameters();
                  String type1 =
                      getDisambiguatingTypeName(ASTHelpers.getSymbol(params.get(pos1)).type);
                  String type2 =
                      getDisambiguatingTypeName(ASTHelpers.getSymbol(params.get(pos2)).type);
                  return type1 + type2;
                },
                identity()));
  }

  private static String buildWithSuffix(ImmutableList<String> extraTypeNames) {
    return "With" + String.join("And", extraTypeNames);
  }

  private static ImmutableList<String> getExtraParamTypeNames(
      RuleInfo rule, ImmutableList<ImmutableList<String>> allBaseParamTypeNames) {
    List<? extends VariableTree> params = rule.afterMethod().getParameters();
    ImmutableList<String> ruleTypeNames =
        params.stream().map(RefasterRuleName::paramTypeName).collect(toImmutableList());

    /* Select the base rule with the most positional type matches. */
    @Var ImmutableList<String> bestBase = ImmutableList.of();
    @Var int bestScore = -1;
    for (ImmutableList<String> base : allBaseParamTypeNames) {
      int score = countPositionalMatches(ruleTypeNames, base);
      if (score > bestScore) {
        bestScore = score;
        bestBase = base;
      }
    }

    /* Greedy subsequence matching against the best base to find extra params. */
    ImmutableList.Builder<String> extras = ImmutableList.builder();
    @Var int baseIdx = 0;
    for (String name : ruleTypeNames) {
      if (baseIdx < bestBase.size() && name.equals(bestBase.get(baseIdx))) {
        baseIdx++;
      } else {
        extras.add(name);
      }
    }

    /*
     * If the greedy match did not consume all base params (e.g. because the types diverge
     * completely), fall back to a tail slice: the extra params are the trailing positions beyond the
     * base arity.
     */
    return baseIdx >= bestBase.size()
        ? extras.build()
        : ruleTypeNames.subList(bestBase.size(), ruleTypeNames.size());
  }

  private static int countPositionalMatches(
      ImmutableList<String> extended, ImmutableList<String> base) {
    @Var int matches = 0;
    for (int i = 0, limit = Math.min(extended.size(), base.size()); i < limit; i++) {
      if (extended.get(i).equals(base.get(i))) {
        matches++;
      }
    }
    return matches;
  }

  private static String paramTypeName(VariableTree param) {
    VarSymbol sym = ASTHelpers.getSymbol(param);
    return hasRepeatedAnnotation(sym) ? "Varargs" : getDisambiguatingTypeName(sym.type);
  }

  private static boolean hasRepeatedAnnotation(VarSymbol sym) {
    return sym.getAnnotationMirrors().stream()
        .anyMatch(
            a -> a.type.tsym.getQualifiedName().contentEquals(Repeated.class.getCanonicalName()));
  }

  private static boolean hasAnyRepeatedParam(RuleInfo rule) {
    return rule.afterMethod().getParameters().stream()
        .map(ASTHelpers::getSymbol)
        .anyMatch(RefasterRuleName::hasRepeatedAnnotation);
  }

  private static String getDisambiguatingTypeName(Type type) {
    return switch (type) {
      case ArrayType arrayType -> getDisambiguatingTypeName(arrayType.getComponentType());
      case TypeVar typeVar when typeVar.getUpperBound() != null ->
          getDisambiguatingTypeName(typeVar.getUpperBound());
      default -> capitalize(type.tsym.getSimpleName().toString());
    };
  }

  private static int getMinParamCount(ImmutableList<RuleInfo> rules) {
    return rules.stream().mapToInt(r -> r.afterMethod().getParameters().size()).min().orElseThrow();
  }

  private static ImmutableListMultimap<String, RuleInfo> groupByTypeAtPosition(
      ImmutableList<RuleInfo> rules, int pos) {
    return rules.stream()
        .filter(rule -> rule.afterMethod().getParameters().size() > pos)
        .collect(
            toImmutableListMultimap(
                rule ->
                    getDisambiguatingTypeName(
                        ASTHelpers.getSymbol(rule.afterMethod().getParameters().get(pos)).type),
                identity()));
  }

  private static boolean isVoidMethod(MethodTree method) {
    return ASTHelpers.getSymbol(method).getReturnType().getTag() == TypeTag.VOID;
  }

  private static boolean isExpressionBlockPair(ImmutableList<RuleInfo> rules) {
    return rules.size() == 2
        && isVoidMethod(rules.getFirst().afterMethod()) != isVoidMethod(rules.get(1).afterMethod());
  }

  private static String expressionBlockSuffix(RuleInfo rule) {
    return isVoidMethod(rule.afterMethod()) ? "Block" : "Expression";
  }

  private static String capitalize(String s) {
    if (s.isEmpty()) {
      return s;
    }
    if (s.contains("_")) {
      return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s);
    }
    if (s.length() > 1 && s.equals(s.toUpperCase(Locale.ROOT))) {
      return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  private record RuleInfo(ClassTree classTree, MethodTree afterMethod, String derivedBaseName) {}

  private static final class NameDerivationScanner
      extends TreeScanner<@Nullable Void, Consumer<String>> {
    private final ImmutableSet<VarSymbol> parameterSymbols;
    private final ImmutableSet<Symbol> placeholderSymbols;
    private final Set<Symbol> lambdaParamSymbols = new HashSet<>();

    NameDerivationScanner(Set<VarSymbol> parameterSymbols, Set<Symbol> placeholderSymbols) {
      this.parameterSymbols = ImmutableSet.copyOf(parameterSymbols);
      this.placeholderSymbols = ImmutableSet.copyOf(placeholderSymbols);
    }

    private boolean isSkippableVariable(Symbol sym) {
      return parameterSymbols.contains(sym)
          || lambdaParamSymbols.contains(sym)
          || placeholderSymbols.contains(sym);
    }

    private static boolean isRefasterAsVarargs(MethodInvocationTree tree) {
      return getRefasterMethodName(tree).equals(Optional.of("asVarargs"));
    }

    private static Optional<String> getRefasterMethodName(MethodInvocationTree tree) {
      return tree.getMethodSelect() instanceof MemberSelectTree memberSelect
              && memberSelect.getExpression() instanceof IdentifierTree identTree
              && identTree.getName().contentEquals("Refaster")
          ? Optional.of(memberSelect.getIdentifier().toString())
          : Optional.empty();
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree tree, Consumer<String> sink) {
      /* Check if it's a placeholder invocation. */
      if (placeholderSymbols.contains(ASTHelpers.getSymbol(tree))) {
        // XXX: I do think we need to recurse here to capture the arguments? Validate with tests.
        return null;
      }

      /* Handle Refaster utility methods. */
      Optional<String> refasterMethod = getRefasterMethodName(tree);
      if (refasterMethod.isPresent()) {
        switch (refasterMethod.orElseThrow()) {
          case "anyOf", "asVarargs" -> {
            for (ExpressionTree arg : tree.getArguments()) {
              scan(arg, sink);
            }
          }
          case "clazz" -> sink.accept("Class");
          default -> {}
        }
        return null;
      }

      /* Visit the method select (which includes the qualifier). */
      scan(tree.getMethodSelect(), sink);

      /* Include explicit type arguments (e.g., the <T> in invocation.<T>getArgument(i)). */
      for (Tree typeArg : tree.getTypeArguments()) {
        Type type = ASTHelpers.getType(typeArg);
        if (type != null) {
          sink.accept(getDisambiguatingTypeName(type));
        }
      }

      /* Visit non-variable arguments. */
      for (ExpressionTree arg : tree.getArguments()) {
        if (!isVariableArg(arg)) {
          scan(arg, sink);
        }
      }

      return null;
    }

    private boolean isVariableArg(ExpressionTree arg) {
      return switch (arg) {
        case IdentifierTree identTree -> {
          Symbol sym = ASTHelpers.getSymbol(identTree);
          yield sym != null && isSkippableVariable(sym);
        }
        case MethodInvocationTree invocation -> isRefasterAsVarargs(invocation);
        default -> false;
      };
    }

    @Override
    public @Nullable Void visitMemberSelect(MemberSelectTree tree, Consumer<String> sink) {
      ExpressionTree expr = tree.getExpression();

      /* Recurse into the expression first. */
      Symbol exprSym = ASTHelpers.getSymbol(expr);
      if (exprSym instanceof VarSymbol varSym && isSkippableVariable(varSym)) {
        /* Expression is a parameter reference; emit the parameter's type name. */
        sink.accept(capitalize(varSym.type.tsym.getSimpleName().toString()));
      } else {
        scan(expr, sink);
      }

      /* Emit the member identifier. */
      sink.accept(capitalize(tree.getIdentifier().toString()));

      return null;
    }

    @Override
    public @Nullable Void visitIdentifier(IdentifierTree tree, Consumer<String> sink) {
      Symbol sym = ASTHelpers.getSymbol(tree);
      if (sym != null && isSkippableVariable(sym)) {
        return null;
      }

      sink.accept(capitalize(tree.getName().toString()));
      return null;
    }

    @Override
    public @Nullable Void visitNewClass(NewClassTree tree, Consumer<String> sink) {
      sink.accept("New");
      sink.accept(capitalize(getSimpleTypeName(tree.getIdentifier())));

      for (ExpressionTree arg : tree.getArguments()) {
        if (!isVariableArg(arg)) {
          scan(arg, sink);
        }
      }

      return null;
    }

    private static String getSimpleTypeName(Tree typeTree) {
      return switch (typeTree) {
        case ParameterizedTypeTree paramType -> getSimpleTypeName(paramType.getType());
        case IdentifierTree identTree -> identTree.getName().toString();
        case MemberSelectTree memberSelect -> memberSelect.getIdentifier().toString();
        default ->
            throw new IllegalArgumentException("Unexpected tree type: " + typeTree.getKind());
      };
    }

    @Override
    public @Nullable Void visitBinary(BinaryTree tree, Consumer<String> sink) {
      scan(tree.getLeftOperand(), sink);
      String opName = BINARY_OP_NAMES.get(tree.getKind());
      if (opName != null) {
        sink.accept(opName);
      }
      scan(tree.getRightOperand(), sink);
      return null;
    }

    @Override
    public @Nullable Void visitUnary(UnaryTree tree, Consumer<String> sink) {
      String opName = UNARY_OP_NAMES.get(tree.getKind());
      if (opName != null) {
        sink.accept(opName);
      }
      scan(tree.getExpression(), sink);
      return null;
    }

    @Override
    public @Nullable Void visitInstanceOf(InstanceOfTree tree, Consumer<String> sink) {
      scan(tree.getExpression(), sink);
      sink.accept("Instanceof");
      sink.accept(capitalize(getSimpleTypeName(tree.getType())));
      return null;
    }

    @Override
    public @Nullable Void visitLiteral(LiteralTree tree, Consumer<String> sink) {
      Object value = tree.getValue();
      if (value == null) {
        sink.accept("Null");
        return null;
      }

      String name = LITERAL_NAMES.get(value);
      if (name != null) {
        sink.accept(name);
      } else if (value instanceof Number) {
        sink.accept(value.toString());
      }
      return null;
    }

    @Override
    public @Nullable Void visitTypeCast(TypeCastTree tree, Consumer<String> sink) {
      return scan(tree.getExpression(), sink);
    }

    @Override
    public @Nullable Void visitParenthesized(ParenthesizedTree tree, Consumer<String> sink) {
      return scan(tree.getExpression(), sink);
    }

    @Override
    public @Nullable Void visitConditionalExpression(
        ConditionalExpressionTree tree, Consumer<String> sink) {
      scan(tree.getCondition(), sink);
      scan(tree.getTrueExpression(), sink);
      scan(tree.getFalseExpression(), sink);
      return null;
    }

    @Override
    public @Nullable Void visitLambdaExpression(LambdaExpressionTree tree, Consumer<String> sink) {
      ImmutableList<? extends Symbol> params =
          tree.getParameters().stream().map(ASTHelpers::getSymbol).collect(toImmutableList());
      lambdaParamSymbols.addAll(params);
      scan(tree.getBody(), sink);
      lambdaParamSymbols.removeAll(params);
      return null;
    }

    @Override
    public @Nullable Void visitMemberReference(MemberReferenceTree tree, Consumer<String> sink) {
      ExpressionTree qualifierExpr = tree.getQualifierExpression();
      Symbol qualifierSym = ASTHelpers.getSymbol(qualifierExpr);
      if (qualifierSym instanceof VarSymbol varSym && isSkippableVariable(varSym)) {
        sink.accept(capitalize(varSym.type.tsym.getSimpleName().toString()));
      } else {
        scan(qualifierExpr, sink);
      }

      sink.accept(capitalize(tree.getName().toString()));
      return null;
    }
  }
}
