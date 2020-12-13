package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.bugpatterns.StaticImports;
import com.google.errorprone.bugpatterns.StaticImports.StaticImportInfo;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.util.Objects;
import java.util.Optional;

/** A {@link BugChecker} which flags methods that can and should be statically imported. */
// XXX: Tricky cases:
// - `org.springframework.http.MediaType` (do except for `ALL`?)
// - `org.springframework.http.HttpStatus` (not always an improvement, and `valueOf` must
//    certainly be excluded)
// - `com.google.common.collect.Tables`
// - `ch.qos.logback.classic.Level.{DEBUG, ERROR, INFO, TRACE, WARN"}`
// XXX: Also introduce a check which disallows static imports of certain methods. Candidates:
// - `com.google.common.base.Strings`
// - `java.util.Optional.empty`
// - `java.util.Locale.ROOT`
// - `ZoneOffset.ofHours` and other `ofXXX`-style methods.
// - `java.time.Clock`.
// - Several other `java.time` classes.
// - Likely any of `*.{ZERO, ONE, MIX, MAX, MIN_VALUE, MAX_VALUE}`.
@AutoService(BugChecker.class)
@BugPattern(
    name = "StaticImport",
    summary = "Method should be statically imported",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = BugPattern.ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class StaticImportCheck extends BugChecker implements MemberSelectTreeMatcher {
  private static final long serialVersionUID = 1L;

  @VisibleForTesting
  static final ImmutableSet<String> STATIC_IMPORT_CANDIDATE_CLASSES =
      ImmutableSet.of(
          "com.google.common.base.Preconditions",
          "com.google.common.base.Predicates",
          "com.google.common.base.Verify",
          "com.google.common.collect.MoreCollectors",
          "com.mongodb.client.model.Accumulators",
          "com.mongodb.client.model.Aggregates",
          "com.mongodb.client.model.Filters",
          "com.mongodb.client.model.Indexes",
          "com.mongodb.client.model.Projections",
          "com.mongodb.client.model.Sorts",
          "com.mongodb.client.model.Updates",
          "java.nio.charset.StandardCharsets",
          "java.util.Comparator",
          "java.util.Map.Entry",
          "java.util.stream.Collectors",
          "org.assertj.core.api.Assertions",
          "org.assertj.core.api.InstanceOfAssertFactories",
          "org.assertj.core.api.SoftAssertions",
          "org.assertj.core.data.Offset",
          "org.assertj.core.groups.Tuple",
          "org.hamcrest.Matchers",
          "org.hamcrest.text.MatchesPattern",
          "org.hibernate.validator.testutil.ConstraintViolationAssert",
          "org.junit.jupiter.api.Assertions",
          "org.mockito.AdditionalAnswers",
          "org.mockito.Answers",
          "org.mockito.ArgumentMatchers",
          "org.mockito.Mockito",
          "org.springframework.format.annotation.DateTimeFormat.ISO",
          "org.springframework.http.HttpHeaders",
          "org.springframework.http.HttpMethod",
          "org.testng.Assert",
          "reactor.function.TupleUtils");

  @VisibleForTesting
  static final ImmutableSetMultimap<String, String> STATIC_IMPORT_CANDIDATE_METHODS =
      ImmutableSetMultimap.<String, String>builder()
          .putAll(
              "com.google.common.collect.ImmutableListMultimap",
              "flatteningToImmutableListMultimap",
              "toImmutableListMultimap")
          .put("com.google.common.collect.ImmutableList", "toImmutableList")
          .put("com.google.common.collect.ImmutableMap", "toImmutableMap")
          .put("com.google.common.collect.ImmutableMultiset", "toImmutableMultiset")
          .put("com.google.common.collect.ImmutableRangeSet", "toImmutableRangeSet")
          .putAll(
              "com.google.common.collect.ImmutableSetMultimap",
              "flatteningToImmutableSetMultimap",
              "toImmutableSetMultimap")
          .put("com.google.common.collect.ImmutableSet", "toImmutableSet")
          .put("com.google.common.collect.ImmutableSortedMap", "toImmutableSortedMap")
          .put("com.google.common.collect.ImmutableSortedMultiset", "toImmutableSortedMultiset")
          .put("com.google.common.collect.ImmutableSortedSet", "toImmutableSortedSet")
          .put("com.google.common.collect.ImmutableTable", "toImmutableTable")
          .put("com.google.common.base.Functions", "identity")
          .put("java.util.function.Function", "identity")
          .put("java.util.function.Predicate", "not")
          .put("org.junit.jupiter.params.provider.Arguments", "arguments")
          .putAll(
              "java.util.Objects",
              "checkIndex",
              "checkFromIndexSize",
              "checkFromToIndex",
              "requireNonNull",
              "requireNonNullElse",
              "requireNonNullElseGet")
          .putAll("com.google.common.collect.Comparators", "emptiesFirst", "emptiesLast")
          .build();

  @Override
  public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
    if (!isCandidate(state)) {
      return Description.NO_MATCH;
    }

    StaticImportInfo importInfo = StaticImports.tryCreate(tree, state);
    if (importInfo == null) {
      return Description.NO_MATCH;
    }

    return getCandidateSimpleName(importInfo)
        .flatMap(n -> tryStaticImport(tree, importInfo.canonicalName() + '.' + n, n, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static boolean isCandidate(VisitorState state) {
    Tree parentTree =
        Objects.requireNonNull(
                state.getPath().getParentPath(), "MemberSelectTree lacks enclosing node")
            .getLeaf();
    switch (parentTree.getKind()) {
      case IMPORT:
      case MEMBER_SELECT:
        return false;
      case METHOD_INVOCATION:
        return ((MethodInvocationTree) parentTree).getTypeArguments().isEmpty();
      default:
        return true;
    }
  }

  private static Optional<String> getCandidateSimpleName(StaticImportInfo importInfo) {
    String canonicalName = importInfo.canonicalName();
    return importInfo
        .simpleName()
        .toJavaUtil()
        .filter(
            name ->
                STATIC_IMPORT_CANDIDATE_CLASSES.contains(canonicalName)
                    || STATIC_IMPORT_CANDIDATE_METHODS.containsEntry(canonicalName, name));
  }

  private static Optional<Fix> tryStaticImport(
      MemberSelectTree tree, String fullyQualifiedName, String simpleName, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder().replace(tree, simpleName);

    if (!simpleName.equals(SuggestedFixes.qualifyStaticImport(fullyQualifiedName, fix, state))) {
      /* Statically importing this symbol would clash with an existing import. */
      return Optional.empty();
    }

    return Optional.of(fix.build());
  }
}
