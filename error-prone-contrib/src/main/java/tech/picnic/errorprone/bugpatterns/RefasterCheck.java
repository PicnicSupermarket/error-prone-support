package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Streams;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link BugChecker} which flags code which can be simplified using a Refaster template located
 * on the classpath.
 *
 * <p>This checker locates all {@code *.refaster} classpath resources and assumes they contain a
 * {@link CodeTransformer}.
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
  private static final long serialVersionUID = 1L;

  private final CodeTransformer codeTransformer = loadCompositeCodeTransformer();

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
   * <p>Generally all matches will be reported. In case of overlap the match which replaces the
   * largest piece of source code is preferred.
   */
  // XXX: This selection logic solves an issue described in
  // https://github.com/google/error-prone/issues/559. Consider contributing it back upstream.
  private static void applyMatches(
      List<Description> allMatches, EndPosTable endPositions, VisitorState state) {
    ImmutableList<Description> byReplacementSize =
        ImmutableList.sortedCopyOf(
            Comparator.<Description>comparingInt(d -> getReplacedCodeSize(d, endPositions))
                .reversed(),
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

  private static ImmutableRangeSet<Integer> getReplacementRanges(
      Description description, EndPosTable endPositions) {
    return getReplacements(description, endPositions)
        .map(Replacement::range)
        .collect(toImmutableRangeSet());
  }

  private static Stream<Replacement> getReplacements(
      Description description, EndPosTable endPositions) {
    return description.fixes.stream()
        .limit(1)
        .flatMap(fix -> fix.getReplacements(endPositions).stream());
  }

  private static CodeTransformer loadCompositeCodeTransformer() {
    return CompositeCodeTransformer.compose(
        getClassPathResources().stream()
            .filter(ri -> ri.getResourceName().endsWith(".refaster"))
            .map(RefasterCheck::loadCodeTransformer)
            .flatMap(Streams::stream)
            .collect(toImmutableList()));
  }

  private static ImmutableSet<ResourceInfo> getClassPathResources() {
    try {
      return ClassPath.from(RefasterCheck.class.getClassLoader()).getResources();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to scan classpath for resources", e);
    }
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
