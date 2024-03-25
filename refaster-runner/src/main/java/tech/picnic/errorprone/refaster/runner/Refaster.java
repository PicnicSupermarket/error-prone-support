package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableRangeSet.toImmutableRangeSet;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.ErrorProneOptions.Severity;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;

/**
 * A {@link BugChecker} that flags code that can be simplified using Refaster rules located on the
 * classpath.
 *
 * <p>This checker locates all {@code *.refaster} classpath resources and assumes that they contain
 * a {@link CodeTransformer}. The set of loaded Refaster rules can be restricted by passing {@code
 * -XepOpt:Refaster:NamePattern=<someRegex>}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Write idiomatic code when possible",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class Refaster extends BugChecker implements CompilationUnitTreeMatcher {
  /** Flag to pass a pattern that restricts which Refaster rules are loaded. */
  public static final String INCLUDED_RULES_PATTERN_FLAG = "Refaster:NamePattern";

  private static final long serialVersionUID = 1L;

  @SuppressWarnings({"java:S1948", "serial"} /* Concrete instance will be `Serializable`. */)
  private final CodeTransformer codeTransformer;

  /** Instantiates a default {@link Refaster} instance. */
  public Refaster() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link Refaster}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  @VisibleForTesting
  public Refaster(ErrorProneFlags flags) {
    codeTransformer = createCompositeCodeTransformer(flags);
  }

  @CanIgnoreReturnValue
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
   * <p>In the common case all matches will be reported. In case of overlap the match that replaces
   * the largest piece of source code is preferred. In case two matches wish to replace exactly the
   * same piece of code, preference is given to the match that suggests the shortest replacement.
   */
  // XXX: This selection logic solves an issue described in
  // https://github.com/google/error-prone/issues/559. Consider contributing it back upstream.
  private void applyMatches(
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
        /*
         * This suggested fix does not overlap with any ("larger") replacement seen until now, so
         * apply it.
         */
        state.reportMatch(augmentDescription(description, getSeverityOverride(state)));
        replacedSections.addAll(ranges);
      }
    }
  }

  private Optional<SeverityLevel> getSeverityOverride(VisitorState state) {
    return Optional.ofNullable(state.errorProneOptions().getSeverityMap().get(canonicalName()))
        .flatMap(Refaster::toSeverityLevel);
  }

  private static Optional<SeverityLevel> toSeverityLevel(Severity severity) {
    return switch (severity) {
      case DEFAULT -> Optional.empty();
      case WARN -> Optional.of(WARNING);
      case ERROR -> Optional.of(ERROR);
      default ->
          throw new IllegalStateException(String.format("Unsupported severity='%s'", severity));
    };
  }

  /**
   * Updates the given {@link Description}'s details by standardizing the reported check name,
   * updating the associated message, and optionally overriding its severity.
   *
   * <p>The assigned severity is overridden only if this bug checker's severity was explicitly
   * configured.
   *
   * <p>The original check name (i.e. the Refaster rule name) is prepended to the {@link
   * Description}'s message. The replacement check name ("Refaster Rule", a name which includes a
   * space) is chosen such that it is guaranteed not to match any canonical bug checker name (as
   * that could cause {@link VisitorState#reportMatch(Description)}} to override the reported
   * severity).
   */
  @SuppressWarnings("RestrictedApi" /* We create a heavily customized `Description` here. */)
  private static Description augmentDescription(
      Description description, Optional<SeverityLevel> severityOverride) {
    return Description.builder(
            description.position,
            "Refaster Rule",
            description.getLink(),
            String.join(": ", description.checkName, description.getRawMessage()))
        .overrideSeverity(severityOverride.orElseGet(description::severity))
        .addAllFixes(description.fixes)
        .build();
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

  private static CodeTransformer createCompositeCodeTransformer(ErrorProneFlags flags) {
    ImmutableListMultimap<String, CodeTransformer> allTransformers =
        CodeTransformers.getAllCodeTransformers();
    return CompositeCodeTransformer.compose(
        flags
            .get(INCLUDED_RULES_PATTERN_FLAG)
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
}
