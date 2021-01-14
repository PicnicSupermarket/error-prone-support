package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class RefasterCheck extends BugChecker implements CompilationUnitTreeMatcher {
  private static final String REFASTER_TEMPLATE_SUFFIX = ".refaster";
  private static final String INCLUDED_TEMPLATES_PATTERN_FLAG = "Refaster:NamePattern";
  private static final long serialVersionUID = 1L;

  private final CodeTransformer codeTransformer;

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
    codeTransformer = createCompositeCodeTransformer(flags, loadAllCodeTransformers());
  }

  @VisibleForTesting
  RefasterCheck(
      ErrorProneFlags flags, ImmutableListMultimap<String, CodeTransformer> allTransformers) {
    codeTransformer = createCompositeCodeTransformer(flags, allTransformers);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    /* First, collect all matches. */
    List<Description> matches = new ArrayList<>();
    codeTransformer.apply(state.getPath(), new SubContext(state.context), matches::add);
    /* Then apply them. */
    applyMatches(matches, ((JCCompilationUnit) tree).endPositions, state);

    /* Any matches were already reported by the code above, directly to the `VisitorState`. */
    return Description.NO_MATCH;
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

  private static CodeTransformer createCompositeCodeTransformer(
      ErrorProneFlags flags, ImmutableListMultimap<String, CodeTransformer> allTransformers) {
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

  @VisibleForTesting
  static ImmutableListMultimap<String, CodeTransformer> loadAllCodeTransformers() {
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
      return ClassPath.from(
              Objects.requireNonNullElseGet(
                  RefasterCheck.class.getClassLoader(), () -> ClassLoader.getSystemClassLoader()))
          .getResources();
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
      return Optional.of((CodeTransformer) ois.readObject());
    } catch (NoSuchElementException e) {
      /* For some reason we can't load the resource. Skip it. */
      // XXX: Should we log this?
      return Optional.empty();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Can't load `CodeTransformer` from " + resource, e);
    }
  }
}
