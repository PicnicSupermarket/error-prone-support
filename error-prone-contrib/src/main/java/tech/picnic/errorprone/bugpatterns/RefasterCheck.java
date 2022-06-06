package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
    name = "Refaster",
    summary = "Write idiomatic code when possible",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class RefasterCheck extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String REFASTER_TEMPLATE_SUFFIX = ".refaster";
  private static final String INCLUDED_TEMPLATES_PATTERN_FLAG = "Refaster:NamePattern";

  @VisibleForTesting
  static final Supplier<ImmutableListMultimap<String, CodeTransformer>> ALL_CODE_TRANSFORMERS =
      Suppliers.memoize(RefasterCheck::loadAllCodeTransformers);

  private final List<RefasterRule<?, ?>> refasterRules;
  //  private final Node<RefasterRule<?, ?>> refasterRules;

  /** Instantiates the default {@link RefasterCheck}. */
  public RefasterCheck() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link RefasterCheck}.
   *
   * @param flags Any provided command line flags.
   */
  public RefasterCheck(ErrorProneFlags flags) {
    refasterRules = getRefasterRules(flags);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    RefasterRuleSelectorFactory refasterRuleSelectorFactory =
        loadRefasterRuleSelectorFactory(state);
    RefasterRuleSelector refasterRuleSelector =
        refasterRuleSelectorFactory.createRefasterRuleSelector(refasterRules);
    Set<RefasterRule<?, ?>> candidateRules = refasterRuleSelector.selectCandidateRules(tree);


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
    // candidateRules.stream().map(Object::toString).ruleSelectorFactories(joining(","));
    // System.out.printf("\nTemplates for %s: \n%s\n", tree.getSourceFile().getName(), removeThis);

    /* First, ruleSelectorFactories all matches. */
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

  //  // XXX: Here and below: drop redundant `Refaster` from method names?
  //  private Set<RefasterRule<?, ?>> getCandidateRules(CompilationUnitTree tree) {
  //    Set<RefasterRule<?, ?>> candidateRules = newSetFrom`Map(new IdentityHashMap<>());
  //    refasterRules.collectCandidateTemplates(
  //        extractSourceIdentifiers(tree).asList(), candidateRules::add);
  //
  //    return candidateRules;
  //  }

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
    ImmutableListMultimap<String, CodeTransformer> allTransformers = ALL_CODE_TRANSFORMERS.get();
    return CompositeCodeTransformer.compose(
        flags
            .get(INCLUDED_TEMPLATES_PATTERN_FLAG)
            .map(Pattern::compile)
            .map(nameFilter -> filterCodeTransformers(allTransformers, nameFilter))
            .orElseGet(allTransformers::values));
  }

  private static ImmutableCollection<CodeTransformer> filterCodeTransformers(
      ImmutableListMultimap<String, CodeTransformer> transformers, Pattern nameFilter) {
    return transformers.entries().stream()
        .filter(e -> nameFilter.matcher(e.getKey()).matches())
        .map(Map.Entry::getValue)
        .collect(toImmutableList());
  }

  private static ImmutableListMultimap<String, CodeTransformer> loadAllCodeTransformers() {
    ImmutableListMultimap.Builder<String, CodeTransformer> transformers =
        ImmutableListMultimap.builder();

    for (ResourceInfo resource : getClassPathResources()) {
      getRefasterTemplateName(resource)
          .ifPresent(
              templateName ->
                  loadCodeTransformer(resource)
                      .ifPresent(transformer -> transformers.put(templateName, transformer)));
    }

    return transformers.build();
  }

  private static ImmutableSet<ResourceInfo> getClassPathResources() {
    try {
      return ClassPath.from(ClassLoader.getSystemClassLoader()).getResources();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan classpath for resources", e);
    }
  }

  private static Optional<String> getRefasterTemplateName(ResourceInfo resource) {
    String resourceName = resource.getResourceName();
    if (!resourceName.endsWith(REFASTER_TEMPLATE_SUFFIX)) {
      return Optional.empty();
    }

    int lastPathSeparator = resourceName.lastIndexOf('/');
    int beginIndex = lastPathSeparator < 0 ? 0 : lastPathSeparator + 1;
    int endIndex = resourceName.length() - REFASTER_TEMPLATE_SUFFIX.length();
    return Optional.of(resourceName.substring(beginIndex, endIndex));
  }

  private static Optional<CodeTransformer> loadCodeTransformer(ResourceInfo resource) {
    try (InputStream in = resource.url().openStream();
        ObjectInputStream ois = new ObjectInputStream(in)) {
      @SuppressWarnings("BanSerializableRead" /* Part of the Refaster API. */)
      CodeTransformer codeTransformer = (CodeTransformer) ois.readObject();
      return Optional.of(codeTransformer);
    } catch (NoSuchElementException e) {
      /* For some reason we can't load the resource. Skip it. */
      // XXX: Should we log this?
      return Optional.empty();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Can't load `CodeTransformer` from " + resource, e);
    }
  }
}
