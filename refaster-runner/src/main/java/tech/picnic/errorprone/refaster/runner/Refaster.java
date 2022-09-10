package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Iterables;
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
import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import tech.picnic.errorprone.rule.selector.NewFactory;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory.RefasterRuleSelector;

/**
 * A {@link BugChecker} which flags code which can be simplified using Refaster templates located on
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

  private final List<RefasterRule<?, ?>> refasterRules;

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
    refasterRules = getRefasterRules(flags);
  }

  @CanIgnoreReturnValue
  @Override
  @SuppressWarnings("UnusedVariable")
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    // XXX: Inline this variable.
    //    Set<RefasterRule<?, ?>> candidateRules = getCandidateRefasterRules(tree);

    RefasterRuleSelector selector =
        NewFactory.getSelector(Thread.currentThread().getContextClassLoader(), refasterRules);
    //    boolean isForkOnClassPath;
    //    try {
    //      Class<?> clazz =
    //          Class.forName(
    //              "com.google.errorprone.ErrorProneOptions",
    //              /* initialize= */ false,
    //              Thread.currentThread().getContextClassLoader());
    //      Optional<Method> forkMethod =
    //          Arrays.stream(clazz.getDeclaredMethods())
    //              .filter(method -> Modifier.isPublic(method.getModifiers()))
    //              .filter(m -> m.getName().equals("isSuggestionsAsWarnings"))
    //              .findFirst();
    //      // THE CLASS IS HERE, THIS IS THE FORK!
    //      isForkOnClassPath = forkMethod.isPresent();
    //      System.out.println("CLASS FOUND? isForkOnClassPath" + isForkOnClassPath);
    //      //      isForkOnClassPath = true;
    //    } catch (ClassNotFoundException e) {
    //      System.out.println("NOT FOUND!!!");
    //      isForkOnClassPath = false;
    //    }
    //
    //    RefasterRuleSelectorFactory.RefasterRuleSelector selector =
    //        NewFactory.getSelector(refasterRules);
    //    SmartRefasterRuleSelector smartRefasterRuleSelector =
    //        new SmartRefasterRuleSelector(refasterRules);

    //    RefasterRuleSelectorFactory refasterRuleSelectorFactory =
    //        loadRefasterRuleSelectorFactory(state);
    //    RefasterRuleSelectorFactory.RefasterRuleSelector refasterRuleSelector =
    //        refasterRuleSelectorFactory.createRefasterRuleSelector(refasterRules);
    Set<RefasterRule<?, ?>> candidateRules = selector.selectCandidateRules(tree);

    //    DefaultRefasterRuleSelector refasterRuleSelector =
    //        (DefaultRefasterRuleSelector)
    // ruleSelectorFactories.get(0).createRefasterRuleSelector(refasterRules);
    //    SmartRefasterRuleSelector smartRefasterRuleSelector =
    //        (SmartRefasterRuleSelector)
    // ruleSelectorFactories.get(1).createRefasterRuleSelector(refasterRules);
    //
    //    Set<RefasterRule<?, ?>> refasterRules1 = ((RefasterRuleSelector)
    // refasterRuleSelector).selectCandidateRules(tree);
    //    Set<RefasterRule<?, ?>> refasterRules2 = ((RefasterRuleSelector)
    // smartRefasterRuleSelector).selectCandidateRules(tree);

    // XXX: Inline this variable.
    //    Set<RefasterRule<?, ?>> candidateRules = new HashSet<>(); // new getCandidateRules(tree);

    // XXX: Remove these debug lines
    // String removeThis =
    // candidateRules.stream().map(Object::toString).collect(joining(","));
    // System.out.printf("\nTemplates for %s: \n%s\n", tree.getSourceFile().getName(), removeThis);

    /* First, collect all matches. */
    SubContext context = new SubContext(state.context);
    List<Description> matches = new ArrayList<>();
    for (RefasterRule<?, ?> rule : candidateRules) {
      try {
        rule.apply(state.getPath(), context, matches::add);
      } catch (LinkageError e) {
        // XXX: This `try/catch` block handles the issue described and resolved in
        // https://github.com/google/error-prone/pull/2456. Drop this block once that change is
        // released.
        // XXX: Find a way to identify that we're running Picnic's Error Prone fork and disable this
        // fallback if so, as it might hide other bugs.
        return Description.NO_MATCH;
      }
    }
    /* Then apply them. */
    applyMatches(matches, ((JCCompilationUnit) tree).endPositions, state);

    /* Any matches were already reported by the code above, directly to the `VisitorState`. */
    return Description.NO_MATCH;
  }

  // XXX: Here and below: drop redundant `Refaster` from method names?
  //  private Set<RefasterRule<?, ?>> getCandidateRefasterRules(CompilationUnitTree tree) {
  //    Set<RefasterRule<?, ?>> candidateRules = newSetFromMap(new IdentityHashMap<>());
  //    refasterRules.collectCandidateTemplates(
  //        extractSourceIdentifiers(tree).asList(), candidateRules::add);
  //
  //    return candidateRules;
  //  }

  // XXX: Move this to the factory?
  // XXX: Or find other way to load the other one in the thingy? Determine first if it'll be OK.
  //  XXX: Something in between should determine if it is OK,
  private static RefasterRuleSelectorFactory loadRefasterRuleSelectorFactory(VisitorState state) {
    JavacProcessingEnvironment processingEnvironment =
        JavacProcessingEnvironment.instance(state.context);
    ClassLoader loader = processingEnvironment.getProcessorClassLoader();
    Iterable<RefasterRuleSelectorFactory> ruleSelectorFactory =
        ServiceLoader.load(RefasterRuleSelectorFactory.class, loader);

    return Arrays.stream(Iterables.toArray(ruleSelectorFactory, RefasterRuleSelectorFactory.class))
        .filter(RefasterRuleSelectorFactory::isClassPathCompatible)
        .min(Comparator.comparingInt(RefasterRuleSelectorFactory::priority))
        .orElseThrow();
  }

  private static List<RefasterRule<?, ?>> getRefasterRules(ErrorProneFlags flags) {
    CodeTransformer compositeCodeTransformer = createCompositeCodeTransformer(flags);

    List<RefasterRule<?, ?>> refasterRules = new ArrayList<>();
    collectRefasterRules(compositeCodeTransformer, refasterRules::add);
    return refasterRules;
  }

  private static void collectRefasterRules(
      CodeTransformer transformer, Consumer<RefasterRule<?, ?>> sink) {
    if (transformer instanceof RefasterRule) {
      sink.accept((RefasterRule<?, ?>) transformer);
    } else if (transformer instanceof CompositeCodeTransformer) {
      for (CodeTransformer t : ((CompositeCodeTransformer) transformer).transformers()) {
        collectRefasterRules(t, sink);
      }
    } else {
      throw new IllegalStateException(
          String.format("Can't handle `CodeTransformer` of type '%s'", transformer.getClass()));
    }
  }

  /**
   * Reports a subset of the given matches, such that no two reported matches suggest a replacement
   * of the same part of the source code.
   *
   * <p>In the common case all matches will be reported. In case of overlap the match which replaces
   * the largest piece of source code is preferred. In case two matches wish to replace exactly the
   * same piece of code, preference is given to the match which suggests the shortest replacement.
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

  // XXX: Instead create an `ImmutableList<RefasterRule>`
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

  // XXX: Decompose `RefasterRule`s such that each has exactly one `@BeforeTemplate`.
  //  private static ImmutableSet<ImmutableSortedSet<String>> extractTemplateIdentifiers(
  //      RefasterRule<?, ?> refasterRule) {
  //    ImmutableSet.Builder<ImmutableSortedSet<String>> results = ImmutableSet.builder();
  //
  //    for (Object template : refasterRule.beforeTemplates()) {
  //      if (template instanceof ExpressionTemplate) {
  //        UExpression expr = ((ExpressionTemplate) template).expression();
  //        results.addAll(extractTemplateIdentifiers(ImmutableList.of(expr)));
  //      } else if (template instanceof BlockTemplate) {
  //        ImmutableList<UStatement> statements = ((BlockTemplate) template).templateStatements();
  //        results.addAll(extractTemplateIdentifiers(statements));
  //      } else {
  //        throw new IllegalStateException(
  //            String.format("Unexpected template type '%s'", template.getClass()));
  //      }
  //    }
  //
  //    return results.build();
  //  }
  //
  //  // XXX: Consider interning the strings (once a benchmark is in place).
  //  private static ImmutableSet<ImmutableSortedSet<String>> extractTemplateIdentifiers(
  //      ImmutableList<? extends Tree> trees) {
  //    List<Set<String>> identifierCombinations = new ArrayList<>();
  //    identifierCombinations.add(new HashSet<>());
  //
  //    // XXX: Make the scanner static, then make also its helper methods static.
  //    new TreeScanner<Void, List<Set<String>>>() {
  //      @Override
  //      public Void visitIdentifier(IdentifierTree node, List<Set<String>> identifierCombinations)
  // {
  //        // XXX: Also include the package name if not `java.lang`; it must be present.
  //        if (node instanceof UClassIdent) {
  //          for (Set<String> ids : identifierCombinations) {
  //            ids.add(getSimpleName(((UClassIdent) node).getTopLevelClass()));
  //            ids.add(getIdentifier(node));
  //          }
  //        } else if (node instanceof UStaticIdent) {
  //          UClassIdent subNode = ((UStaticIdent) node).classIdent();
  //          for (Set<String> ids : identifierCombinations) {
  //            ids.add(getSimpleName(subNode.getTopLevelClass()));
  //            ids.add(getIdentifier(subNode));
  //            ids.add(node.getName().toString());
  //          }
  //        }
  //
  //        return null;
  //      }
  //
  //      private String getIdentifier(IdentifierTree tree) {
  //        return getSimpleName(tree.getName().toString());
  //      }
  //
  //      private String getSimpleName(String fcqn) {
  //        int index = fcqn.lastIndexOf('.');
  //        return index < 0 ? fcqn : fcqn.substring(index + 1);
  //      }
  //
  //      @Override
  //      public Void visitMemberReference(
  //          MemberReferenceTree node, List<Set<String>> identifierCombinations) {
  //        super.visitMemberReference(node, identifierCombinations);
  //        String id = node.getName().toString();
  //        identifierCombinations.forEach(ids -> ids.add(id));
  //        return null;
  //      }
  //
  //      @Override
  //      public Void visitMemberSelect(
  //          MemberSelectTree node, List<Set<String>> identifierCombinations) {
  //        super.visitMemberSelect(node, identifierCombinations);
  //        String id = node.getIdentifier().toString();
  //        identifierCombinations.forEach(ids -> ids.add(id));
  //        return null;
  //      }
  //
  //      @Override
  //      public Void visitAssignment(AssignmentTree node, List<Set<String>> identifierCombinations)
  // {
  //        registerOperator(node, identifierCombinations);
  //        return super.visitAssignment(node, identifierCombinations);
  //      }
  //
  //      @Override
  //      public Void visitCompoundAssignment(
  //          CompoundAssignmentTree node, List<Set<String>> identifierCombinations) {
  //        registerOperator(node, identifierCombinations);
  //        return super.visitCompoundAssignment(node, identifierCombinations);
  //      }
  //
  //      @Override
  //      public Void visitUnary(UnaryTree node, List<Set<String>> identifierCombinations) {
  //        registerOperator(node, identifierCombinations);
  //        return super.visitUnary(node, identifierCombinations);
  //      }
  //
  //      @Override
  //      public Void visitBinary(BinaryTree node, List<Set<String>> identifierCombinations) {
  //        registerOperator(node, identifierCombinations);
  //        return super.visitBinary(node, identifierCombinations);
  //      }
  //
  //      // XXX: Rename!
  //      private void registerOperator(ExpressionTree node, List<Set<String>>
  // identifierCombinations) {
  //        identifierCombinations.forEach(ids -> ids.add(Util.treeKindToString(node.getKind())));
  //      }
  //
  //      @Override
  //      public Void visitOther(Tree node, List<Set<String>> identifierCombinations) {
  //        if (node instanceof UAnyOf) {
  //          List<Set<String>> base = copy(identifierCombinations);
  //          identifierCombinations.clear();
  //
  //          for (UExpression expr : ((UAnyOf) node).expressions()) {
  //            List<Set<String>> branch = copy(base);
  //            scan(expr, branch);
  //            identifierCombinations.addAll(branch);
  //          }
  //        }
  //
  //        return null;
  //      }
  //
  //      private List<Set<String>> copy(List<Set<String>> identifierCombinations) {
  //        return identifierCombinations.stream()
  //            .map(HashSet::new)
  //            .collect(toCollection(ArrayList::new));
  //      }
  //    }.scan(trees, identifierCombinations);
  //
  //    return identifierCombinations.stream()
  //        .map(ImmutableSortedSet::copyOf)
  //        .collect(toImmutableSet());
  //  }
  //
  //  // XXX: Consider interning!
  //  private static ImmutableSortedSet<String> extractSourceIdentifiers(Tree tree) {
  //    Set<String> identifiers = new HashSet<>();
  //
  //    // XXX: Make the scanner static.
  //    new TreeScanner<Void, Set<String>>() {
  //      @Override
  //      public Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
  //        identifiers.add(node.getName().toString());
  //        return null;
  //      }
  //
  //      @Override
  //      public Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
  //        super.visitMemberReference(node, identifiers);
  //        identifiers.add(node.getName().toString());
  //        return null;
  //      }
  //
  //      @Override
  //      public Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
  //        super.visitMemberSelect(node, identifiers);
  //        identifiers.add(node.getIdentifier().toString());
  //        return null;
  //      }
  //
  //      @Override
  //      public Void visitAssignment(AssignmentTree node, Set<String> identifiers) {
  //        registerOperator(node, identifiers);
  //        return super.visitAssignment(node, identifiers);
  //      }
  //
  //      @Override
  //      public Void visitCompoundAssignment(CompoundAssignmentTree node, Set<String> identifiers)
  // {
  //        registerOperator(node, identifiers);
  //        return super.visitCompoundAssignment(node, identifiers);
  //      }
  //
  //      @Override
  //      public Void visitUnary(UnaryTree node, Set<String> identifiers) {
  //        registerOperator(node, identifiers);
  //        return super.visitUnary(node, identifiers);
  //      }
  //
  //      @Override
  //      public Void visitBinary(BinaryTree node, Set<String> identifiers) {
  //        registerOperator(node, identifiers);
  //        return super.visitBinary(node, identifiers);
  //      }
  //
  //      // XXX: Rename!
  //      private void registerOperator(ExpressionTree node, Set<String> identifiers) {
  //        identifiers.add(Util.treeKindToString(node.getKind()));
  //      }
  //    }.scan(tree, identifiers);
  //
  //    return ImmutableSortedSet.copyOf(identifiers);
  //  }
}
