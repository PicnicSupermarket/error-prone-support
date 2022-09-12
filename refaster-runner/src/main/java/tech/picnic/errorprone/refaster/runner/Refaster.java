package tech.picnic.errorprone.refaster.runner;

import static com.google.auto.common.MoreStreams.toImmutableSet;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.refaster.BlockTemplate;
import com.google.errorprone.refaster.ExpressionTemplate;
import com.google.errorprone.refaster.RefasterRule;
import com.google.errorprone.refaster.UAnyOf;
import com.google.errorprone.refaster.UExpression;
import com.google.errorprone.refaster.UStatement;
import com.google.errorprone.refaster.UStaticIdent;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A {@link BugChecker} that flags code that can be simplified using Refaster templates located on
 * the classpath.
 *
 * <p>This checker locates all {@code *.refaster} classpath resources and assumes they contain a
 * {@link CodeTransformer}. The set of loaded Refaster templates can be restricted by passing {@code
 * -XepOpt:Refaster:NamePattern=<someRegex>}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Write idiomatic code when possible",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class Refaster extends BugChecker implements CompilationUnitTreeMatcher {
  /** Flag to pass a pattern that restricts which Refaster templates are loaded. */
  public static final String INCLUDED_TEMPLATES_PATTERN_FLAG = "Refaster:NamePattern";

  private static final long serialVersionUID = 1L;

  // XXX: Drop this field.
  //  private final CodeTransformer codeTransformer;
  private final ImmutableListMultimap<ImmutableSet<ImmutableSet<String>>, RefasterRule<?, ?>>
      refasterRules;

  /** Instantiates the default {@link Refaster}. */
  public Refaster() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link Refaster}.
   *
   * @param flags Any provided command line flags.
   */
  public Refaster(ErrorProneFlags flags) {
    // XXX: Drop this assignment.
    //    codeTransformer = createCompositeCodeTransformer(flags);
    refasterRules = loadIndexedRules(flags);
  }

  @CanIgnoreReturnValue
  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    ImmutableSet<String> sourceIdentifiers = extractSourceIdentifiers(tree);

    /* First, collect all matches. */
    List<Description> matches = new ArrayList<>();
    for (Map.Entry<ImmutableSet<ImmutableSet<String>>, Collection<RefasterRule<?, ?>>> e :
        refasterRules.asMap().entrySet()) {
      if (e.getKey().stream().anyMatch(sourceIdentifiers::containsAll)) {
        for (RefasterRule<?, ?> rule : e.getValue()) {
          try {
            rule.apply(state.getPath(), new SubContext(state.context), matches::add);
          } catch (LinkageError le) {
            // XXX: This `try/catch` block handles the issue described and resolved in
            // https://github.com/google/error-prone/pull/2456. Drop this block once that change is
            // released.
            // XXX: Find a way to identify that we're running Picnic's Error Prone fork and disable
            // this fallback if so, as it might hide other bugs.
          }
        }
      }
    }

    /* Then apply them. */
    applyMatches(matches, ((JCCompilationUnit) tree).endPositions, state);

    // XXX: drop code below.
    //    /* First, collect all matches. */
    //    List<Description> matches = new ArrayList<>();
    //    try {
    //      codeTransformer.apply(state.getPath(), new SubContext(state.context), matches::add);
    //    } catch (LinkageError e) {
    //      // XXX: This `try/catch` block handles the issue described and resolved in
    //      // https://github.com/google/error-prone/pull/2456. Drop this block once that change is
    //      // released.
    //      // XXX: Find a way to identify that we're running Picnic's Error Prone fork and disable
    // this
    //      // fallback if so, as it might hide other bugs.
    //      return Description.NO_MATCH;
    //    }
    //    /* Then apply them. */
    //    applyMatches(matches, ((JCCompilationUnit) tree).endPositions, state);

    /* Any matches were already reported by the code above, directly to the `VisitorState`. */
    return Description.NO_MATCH;
  }

  private static void collectRefasterRules(
      CodeTransformer transformer, Consumer<RefasterRule<?, ?>> sink) {
    if (transformer instanceof RefasterRule) {
      sink.accept((RefasterRule<?, ?>) transformer);
    } else if (transformer instanceof CompositeCodeTransformer) {
      for (CodeTransformer t : ((CompositeCodeTransformer) transformer).transformers()) {
        collectRefasterRules(t, sink);
      }
    }

    // XXX: Log `else` case?
  }

  private static ImmutableSet<ImmutableSet<String>> extractTemplateIdentifiers(
      RefasterRule<?, ?> refasterRule) {
    ImmutableSet.Builder<ImmutableSet<String>> results = ImmutableSet.builder();

    for (Object template : RefasterIntrospection.getBeforeTemplates(refasterRule)) {
      if (template instanceof ExpressionTemplate) {
        UExpression expr = RefasterIntrospection.getExpression((ExpressionTemplate) template);
        results.addAll(extractTemplateIdentifiers(ImmutableList.of(expr)));
      } else if (template instanceof BlockTemplate) {
        ImmutableList<UStatement> statements =
            RefasterIntrospection.getTemplateStatements((BlockTemplate) template);
        results.addAll(extractTemplateIdentifiers(statements));
      }
      // XXX: error if other kind of template.
    }

    return results.build();
  }

  // XXX: Consider interning the strings (once a benchmark is in place).
  private static ImmutableSet<ImmutableSet<String>> extractTemplateIdentifiers(
      ImmutableList<? extends Tree> trees) {
    // XXX: Here and below: replace `LinkedHashSet`s with `HashSet` once done.
    List<Set<String>> identifierCombinations = new ArrayList<>();
    identifierCombinations.add(new LinkedHashSet<>());

    // XXX: Make the scanner static, then make also its helper methods static.
    new TreeScanner<Void, List<Set<String>>>() {
      @Nullable
      @Override
      public Void visitIdentifier(IdentifierTree node, List<Set<String>> identifierCombinations) {
        // XXX: Also include the package name if not `java.lang`; it must be present.
        if (RefasterIntrospection.isUClassIdent(node)) {
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(node)));
            ids.add(getIdentifier(node));
          }
        } else if (node instanceof UStaticIdent) {
          IdentifierTree subNode = RefasterIntrospection.getClassIdent((UStaticIdent) node);
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(subNode)));
            ids.add(getIdentifier(subNode));
            ids.add(node.getName().toString());
          }
        }

        return null;
      }

      private String getIdentifier(IdentifierTree tree) {
        return getSimpleName(tree.getName().toString());
      }

      private String getSimpleName(String fcqn) {
        int index = fcqn.lastIndexOf('.');
        return index < 0 ? fcqn : fcqn.substring(index + 1);
      }

      @Nullable
      @Override
      public Void visitOther(Tree node, List<Set<String>> identifierCombinations) {
        if (node instanceof UAnyOf) {
          List<Set<String>> base = copy(identifierCombinations);
          identifierCombinations.clear();

          for (UExpression expr : RefasterIntrospection.getExpressions((UAnyOf) node)) {
            List<Set<String>> branch = copy(base);
            scan(expr, branch);
            identifierCombinations.addAll(branch);
          }
        }

        return null;
      }

      @Nullable
      @Override
      public Void visitMemberReference(
          MemberReferenceTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberReference(node, identifierCombinations);
        String id = node.getName().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberSelect(
          MemberSelectTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberSelect(node, identifierCombinations);
        String id = node.getIdentifier().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      private List<Set<String>> copy(List<Set<String>> identifierCombinations) {
        return identifierCombinations.stream()
            .map(LinkedHashSet::new)
            .collect(toCollection(ArrayList::new));
      }
    }.scan(trees, identifierCombinations);

    return identifierCombinations.stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
  }

  // XXX: Consider interning!
  private static ImmutableSet<String> extractSourceIdentifiers(Tree tree) {
    // XXX: Replace `LinkedHashSet`s with `HashSet` once done.
    Set<String> identifiers = new LinkedHashSet<>();

    // XXX: Make the scanner static.
    new TreeScanner<Void, Set<String>>() {
      @Nullable
      @Override
      public Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
        // XXX: Can we be more precise?
        identifiers.add(node.getName().toString());
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
        super.visitMemberReference(node, identifiers);
        identifiers.add(node.getName().toString());
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
        super.visitMemberSelect(node, identifiers);
        identifiers.add(node.getIdentifier().toString());
        return null;
      }
    }.scan(tree, identifiers);

    return ImmutableSet.copyOf(identifiers);
  }

  /**
   * Reports a subset of the given matches, such that no two reported matches suggest a replacement
   * of the same part of the source code.
   *
   * <p>In the common case all matches will be reported. In case of overlap the match that replaces
   * the largest piece of source code is preferred. In case two matches wish to replace exactly the
   * same piece of code, preference is given to the match that suggests the shortest replacement.
   */
  // XXX: This selection logic solves an issue described in
  // https://github.com/google/error-prone/issues/559. Consider contributing it back upstream.
  private static void applyMatches(
      Iterable<Description> allMatches, EndPosTable endPositions, VisitorState state) {
    ImmutableList<Description> byReplacementSize =
        ImmutableList.sortedCopyOf(
            Comparator.<Description>comparingInt(d -> getReplacedCodeSize(d, endPositions))
                .reversed()
                .thenComparingInt(d -> getInsertedCodeSize(d, endPositions)),
            allMatches);

    RangeSet<Integer> replacedSections = TreeRangeSet.create();
    for (Description description : byReplacementSize) {
      ImmutableRangeSet<Integer> ranges = getReplacementRanges(description, endPositions);
      if (ranges.asRanges().stream().noneMatch(replacedSections::intersects)) {
        /* This suggested fix does not overlap with any ("larger") replacement seen until now. Apply it. */
        state.reportMatch(description);
        replacedSections.addAll(ranges);
      }
    }
  }

  private static int getReplacedCodeSize(Description description, EndPosTable endPositions) {
    return getReplacements(description, endPositions).mapToInt(Replacement::length).sum();
  }

  // XXX: It might be nicer to prefer the shortest replacement _post formatting_.
  private static int getInsertedCodeSize(Description description, EndPosTable endPositions) {
    return getReplacements(description, endPositions).mapToInt(r -> r.replaceWith().length()).sum();
  }

  private static ImmutableRangeSet<Integer> getReplacementRanges(
      Description description, EndPosTable endPositions) {
    return getReplacements(description, endPositions)
        .map(Replacement::range)
        .filter(not(Range::isEmpty))
        .collect(toImmutableRangeSet());
  }

  private static Stream<Replacement> getReplacements(
      Description description, EndPosTable endPositions) {
    return description.fixes.stream().flatMap(fix -> fix.getReplacements(endPositions).stream());
  }

  // XXX: Move to separate class, in order to be compatible with vanilla EP.
  private static ImmutableListMultimap<ImmutableSet<ImmutableSet<String>>, RefasterRule<?, ?>>
      loadIndexedRules(ErrorProneFlags flags) {
    List<RefasterRule<?, ?>> refasterRules = new ArrayList<>();
    collectRefasterRules(createCompositeCodeTransformer(flags), refasterRules::add);

    return Multimaps.index(refasterRules, Refaster::extractTemplateIdentifiers);
  }

  // XXX: instead create an `ImmutableList<RefasterRule>`
  private static CodeTransformer createCompositeCodeTransformer(ErrorProneFlags flags) {
    ImmutableListMultimap<String, CodeTransformer> allTransformers =
        CodeTransformers.getAllCodeTransformers();
    return CompositeCodeTransformer.compose(
        flags
            .get(INCLUDED_TEMPLATES_PATTERN_FLAG)
            .map(Pattern::compile)
            .<ImmutableCollection<CodeTransformer>>map(
                nameFilter -> filterCodeTransformers(allTransformers, nameFilter))
            .orElseGet(allTransformers::values));
  }

  private static ImmutableList<CodeTransformer> filterCodeTransformers(
      ImmutableListMultimap<String, CodeTransformer> transformers, Pattern nameFilter) {
    return transformers.entries().stream()
        .filter(e -> nameFilter.matcher(e.getKey()).matches())
        .map(Map.Entry::getValue)
        .collect(toImmutableList());
  }

  private static final class RefasterIntrospection {
    private static final String UCLASS_IDENT_FQCN = "com.google.errorprone.refaster.UClassIdent";
    private static final Class<?> UCLASS_IDENT = getUClassIdentClass();
    private static final Method METHOD_REFASTER_RULE_BEFORE_TEMPLATES =
        getMethod(RefasterRule.class, "beforeTemplates");
    private static final Method METHOD_EXPRESSION_TEMPLATE_EXPRESSION =
        getMethod(ExpressionTemplate.class, "expression");
    private static final Method METHOD_BLOCK_TEMPLATE_TEMPLATE_STATEMENTS =
        getMethod(BlockTemplate.class, "templateStatements");
    private static final Method METHOD_USTATIC_IDENT_CLASS_IDENT =
        getMethod(UStaticIdent.class, "classIdent");
    private static final Method METHOD_UCLASS_IDENT_GET_TOP_LEVEL_CLASS =
        getMethod(UCLASS_IDENT, "getTopLevelClass");
    private static final Method METHOD_UANY_OF_EXPRESSIONS = getMethod(UAnyOf.class, "expressions");

    static boolean isUClassIdent(IdentifierTree tree) {
      return UCLASS_IDENT.equals(tree.getClass());
    }

    static ImmutableList<?> getBeforeTemplates(RefasterRule<?, ?> refasterRule) {
      return invokeMethod(METHOD_REFASTER_RULE_BEFORE_TEMPLATES, refasterRule);
    }

    static UExpression getExpression(ExpressionTemplate template) {
      return invokeMethod(METHOD_EXPRESSION_TEMPLATE_EXPRESSION, template);
    }

    static ImmutableList<UStatement> getTemplateStatements(BlockTemplate template) {
      return invokeMethod(METHOD_BLOCK_TEMPLATE_TEMPLATE_STATEMENTS, template);
    }

    // Actually UClassIdent.
    static IdentifierTree getClassIdent(UStaticIdent tree) {
      return invokeMethod(METHOD_USTATIC_IDENT_CLASS_IDENT, tree);
    }

    // XXX: Make nicer. Or rename the other params.
    static String getTopLevelClass(IdentifierTree uClassIdent) {
      return invokeMethod(METHOD_UCLASS_IDENT_GET_TOP_LEVEL_CLASS, uClassIdent);
    }

    static ImmutableList<UExpression> getExpressions(UAnyOf tree) {
      return invokeMethod(METHOD_UANY_OF_EXPRESSIONS, tree);
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    private static <T> T invokeMethod(Method method, Object instance) {
      try {
        return (T) method.invoke(instance);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(String.format("Failed to invoke method `%s`", method), e);
      }
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
      try {
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            String.format("No method `%s` on class `%s`", methodName, clazz.getName()), e);
      }
    }

    private static Class<?> getUClassIdentClass() {
      try {
        return RefasterIntrospection.class.getClassLoader().loadClass(UCLASS_IDENT_FQCN);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(
            String.format("Failed to load class `%s`", UCLASS_IDENT_FQCN), e);
      }
    }
  }
}
