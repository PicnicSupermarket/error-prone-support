package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.bugpatterns.StaticImports;
import com.google.errorprone.bugpatterns.StaticImports.StaticImportInfo;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;

/**
 * A {@link BugChecker} that flags methods and constants that can and should be statically imported.
 */
// XXX: Tricky cases:
// - `org.springframework.http.HttpStatus` (not always an improvement, and `valueOf` must
//    certainly be excluded)
// - `com.google.common.collect.Tables`
// - `ch.qos.logback.classic.Level.{DEBUG, ERROR, INFO, TRACE, WARN"}`
// XXX: Also introduce a check that disallows static imports of certain methods. Candidates:
// - `com.google.common.base.Strings`
// - `java.util.Optional.empty`
// - `java.util.Locale.ROOT`
// - `ZoneOffset.ofHours` and other `ofXXX`-style methods.
// - `java.time.Clock`.
// - Several other `java.time` classes.
// - Likely any of `*.{ZERO, ONE, MIX, MAX, MIN_VALUE, MAX_VALUE}`.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Identifier should be statically imported",
    link = BUG_PATTERNS_BASE_URL + "StaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class StaticImport extends BugChecker implements MemberSelectTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * Types whose members should be statically imported, unless exempted by {@link
   * #STATIC_IMPORT_EXEMPTED_MEMBERS} or {@link #STATIC_IMPORT_EXEMPTED_IDENTIFIERS}.
   */
  @VisibleForTesting
  static final ImmutableSet<String> STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of(
          "com.google.common.base.Preconditions",
          "com.google.common.base.Predicates",
          "com.google.common.base.Verify",
          "com.google.common.collect.MoreCollectors",
          "com.google.errorprone.BugPattern.LinkType",
          "com.google.errorprone.BugPattern.SeverityLevel",
          "com.google.errorprone.BugPattern.StandardTags",
          "com.google.errorprone.matchers.Matchers",
          "com.google.errorprone.refaster.ImportPolicy",
          "com.mongodb.client.model.Accumulators",
          "com.mongodb.client.model.Aggregates",
          "com.mongodb.client.model.Filters",
          "com.mongodb.client.model.Indexes",
          "com.mongodb.client.model.Projections",
          "com.mongodb.client.model.Sorts",
          "com.mongodb.client.model.Updates",
          "java.nio.charset.StandardCharsets",
          "java.util.Collections",
          "java.util.Comparator",
          "java.util.Map.Entry",
          "java.util.regex.Pattern",
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
          "org.springframework.boot.test.context.SpringBootTest.WebEnvironment",
          "org.springframework.format.annotation.DateTimeFormat.ISO",
          "org.springframework.http.HttpHeaders",
          "org.springframework.http.HttpMethod",
          "org.springframework.http.MediaType",
          "org.testng.Assert",
          "reactor.function.TupleUtils",
          "tech.picnic.errorprone.bugpatterns.util.MoreTypes",
          "tech.picnic.errorprone.bugpatterns.util.ThirdPartyLibrary");

  /** Type members that should be statically imported. */
  @VisibleForTesting
  static final ImmutableSetMultimap<String, String> STATIC_IMPORT_CANDIDATE_MEMBERS =
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
          .put("com.google.common.collect.Sets", "toImmutableEnumSet")
          .put("com.google.common.base.Functions", "identity")
          .put("java.time.ZoneOffset", "UTC")
          .put("java.util.function.Function", "identity")
          .put("java.util.function.Predicate", "not")
          .put("java.util.UUID", "randomUUID")
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

  /**
   * Type members that should never be statically imported.
   *
   * <p>Identifiers listed by {@link #STATIC_IMPORT_EXEMPTED_IDENTIFIERS} should be omitted from
   * this collection.
   */
  // XXX: Perhaps the set of exempted `java.util.Collections` methods is too strict. For now any
  // method name that could be considered "too vague" or could conceivably mean something else in a
  // specific context is left out.
  @VisibleForTesting
  static final ImmutableSetMultimap<String, String> STATIC_IMPORT_EXEMPTED_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .put("com.mongodb.client.model.Filters", "empty")
          .putAll(
              "java.util.Collections",
              "addAll",
              "copy",
              "fill",
              "list",
              "max",
              "min",
              "nCopies",
              "rotate",
              "sort",
              "swap")
          .putAll("java.util.regex.Pattern", "compile", "matches", "quote")
          .put("org.springframework.http.MediaType", "ALL")
          .build();

  /**
   * Identifiers that should never be statically imported.
   *
   * <p>This should be a superset of the identifiers flagged by {@link
   * com.google.errorprone.bugpatterns.BadImport}.
   */
  @VisibleForTesting
  static final ImmutableSet<String> STATIC_IMPORT_EXEMPTED_IDENTIFIERS =
      ImmutableSet.of(
          "builder",
          "create",
          "copyOf",
          "from",
          "getDefaultInstance",
          "INSTANCE",
          "newBuilder",
          "of",
          "valueOf",
          "values");

  /** Instantiates a new {@link StaticImport} instance. */
  public StaticImport() {}

  @Override
  public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
    if (!isCandidateContext(state) || !isCandidate(tree)) {
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

  private static boolean isCandidateContext(VisitorState state) {
    Tree parentTree =
        requireNonNull(state.getPath().getParentPath(), "MemberSelectTree lacks enclosing node")
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

  private static boolean isCandidate(MemberSelectTree tree) {
    String identifier = tree.getIdentifier().toString();
    if (STATIC_IMPORT_EXEMPTED_IDENTIFIERS.contains(identifier)) {
      return false;
    }

    Type type = ASTHelpers.getType(tree.getExpression());
    return type != null
        && !STATIC_IMPORT_EXEMPTED_MEMBERS.containsEntry(type.toString(), identifier);
  }

  private static Optional<String> getCandidateSimpleName(StaticImportInfo importInfo) {
    String canonicalName = importInfo.canonicalName();
    return importInfo
        .simpleName()
        .toJavaUtil()
        .filter(
            name ->
                STATIC_IMPORT_CANDIDATE_TYPES.contains(canonicalName)
                    || STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(canonicalName, name));
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
