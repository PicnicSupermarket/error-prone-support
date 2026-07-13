package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.bugpatterns.NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS;
import static tech.picnic.errorprone.bugpatterns.NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS;
import static tech.picnic.errorprone.bugpatterns.NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Verify;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.bugpatterns.StaticImports;
import com.google.errorprone.bugpatterns.StaticImports.StaticImportInfo;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import tech.picnic.errorprone.utils.Flags;

/**
 * A {@link BugChecker} that flags type members that can and should be statically imported.
 *
 * <p>Additional candidate types and type members can be specified using the {@code
 * StaticImport:CandidateTypes} and {@code StaticImport:CandidateMembers} flags, respectively.
 * Candidate members must be specified in {@code <type>#<member>} format. The latter flag is also
 * honored by the {@link NonStaticImport} check, such that it does not undo the static imports
 * introduced by this check.
 */
// XXX: This check is closely linked to `NonStaticImport`. Consider merging the two.
// XXX: Tricky cases:
// - `org.springframework.http.HttpStatus` (not always an improvement, and `valueOf` must
//    certainly be excluded)
// - `com.google.common.collect.Tables`
// - `ch.qos.logback.classic.Level.{DEBUG, ERROR, INFO, TRACE, WARN}`
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Identifier should be statically imported",
    link = BUG_PATTERNS_BASE_URL + "StaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
@SuppressWarnings({
  "java:S2160" /* Super class equality definition suffices. */,
  "javaarchitecture:S7027" /* `StaticImport` and `NonStaticImport` ensure mutual exclusivity. */,
  "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
})
public final class StaticImport extends BugChecker implements MemberSelectTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * Types whose members should be statically imported, unless exempted by {@link
   * NonStaticImport#NON_STATIC_IMPORT_CANDIDATE_MEMBERS} or {@link
   * NonStaticImport#NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS}.
   *
   * <p>Types listed here should be mutually exclusive with {@link
   * NonStaticImport#NON_STATIC_IMPORT_CANDIDATE_TYPES}.
   */
  @VisibleForTesting
  static final ImmutableSet<String> STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of(
          BugPattern.LinkType.class.getCanonicalName(),
          BugPattern.SeverityLevel.class.getCanonicalName(),
          BugPattern.StandardTags.class.getCanonicalName(),
          Collections.class.getCanonicalName(),
          Collectors.class.getCanonicalName(),
          Comparator.class.getCanonicalName(),
          ImportPolicy.class.getCanonicalName(),
          Map.Entry.class.getCanonicalName(),
          Matchers.class.getCanonicalName(),
          MoreCollectors.class.getCanonicalName(),
          Pattern.class.getCanonicalName(),
          Preconditions.class.getCanonicalName(),
          Predicates.class.getCanonicalName(),
          StandardCharsets.class.getCanonicalName(),
          TypePredicates.class.getCanonicalName(),
          Verify.class.getCanonicalName(),
          "com.fasterxml.jackson.annotation.JsonCreator.Mode",
          "com.fasterxml.jackson.annotation.JsonFormat.Shape",
          "com.fasterxml.jackson.annotation.JsonInclude.Include",
          "com.fasterxml.jackson.annotation.JsonProperty.Access",
          "com.mongodb.client.model.Accumulators",
          "com.mongodb.client.model.Aggregates",
          "com.mongodb.client.model.Filters",
          "com.mongodb.client.model.Indexes",
          "com.mongodb.client.model.Projections",
          "com.mongodb.client.model.Sorts",
          "com.mongodb.client.model.Updates",
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
          "tech.picnic.errorprone.utils.MoreTypes");

  /**
   * Type members that should be statically imported.
   *
   * <p>Please note that:
   *
   * <ul>
   *   <li>Types listed by {@link #STATIC_IMPORT_CANDIDATE_TYPES} should be omitted from this
   *       collection.
   *   <li>This collection should be mutually exclusive with {@link
   *       NonStaticImport#NON_STATIC_IMPORT_CANDIDATE_MEMBERS}.
   *   <li>This collection should not list members contained in {@link
   *       NonStaticImport#NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS}.
   * </ul>
   */
  static final ImmutableSetMultimap<String, String> STATIC_IMPORT_CANDIDATE_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .putAll(Comparators.class.getCanonicalName(), "emptiesFirst", "emptiesLast")
          .put(Function.class.getCanonicalName(), "identity")
          .put(Functions.class.getCanonicalName(), "identity")
          .put(ImmutableList.class.getCanonicalName(), "toImmutableList")
          .putAll(
              ImmutableListMultimap.class.getCanonicalName(),
              "flatteningToImmutableListMultimap",
              "toImmutableListMultimap")
          .put(ImmutableMap.class.getCanonicalName(), "toImmutableMap")
          .put(ImmutableMultiset.class.getCanonicalName(), "toImmutableMultiset")
          .put(ImmutableRangeSet.class.getCanonicalName(), "toImmutableRangeSet")
          .put(ImmutableSet.class.getCanonicalName(), "toImmutableSet")
          .putAll(
              ImmutableSetMultimap.class.getCanonicalName(),
              "flatteningToImmutableSetMultimap",
              "toImmutableSetMultimap")
          .put(ImmutableSortedMap.class.getCanonicalName(), "toImmutableSortedMap")
          .put(ImmutableSortedMultiset.class.getCanonicalName(), "toImmutableSortedMultiset")
          .put(ImmutableSortedSet.class.getCanonicalName(), "toImmutableSortedSet")
          .put(ImmutableTable.class.getCanonicalName(), "toImmutableTable")
          .putAll(
              Objects.class.getCanonicalName(),
              "checkIndex",
              "checkFromIndexSize",
              "checkFromToIndex",
              "requireNonNull",
              "requireNonNullElse",
              "requireNonNullElseGet")
          .put(Predicate.class.getCanonicalName(), "not")
          .put(Sets.class.getCanonicalName(), "toImmutableEnumSet")
          .put(UUID.class.getCanonicalName(), "randomUUID")
          .put(ZoneOffset.class.getCanonicalName(), "UTC")
          .putAll("org.junit.jupiter.params.provider.Arguments", "argumentSet", "arguments")
          .build();

  /**
   * Flag using which additional types whose members should be statically imported (in addition to
   * those defined by {@link #STATIC_IMPORT_CANDIDATE_TYPES}) can be specified.
   */
  private static final String CANDIDATE_TYPES_FLAG = "StaticImport:CandidateTypes";

  /**
   * Flag using which additional type members that should be statically imported (in addition to
   * those defined by {@link #STATIC_IMPORT_CANDIDATE_MEMBERS}) can be specified, in {@code
   * <type>#<member>} format.
   */
  private static final String CANDIDATE_MEMBERS_FLAG = "StaticImport:CandidateMembers";

  private final ImmutableSet<String> candidateTypes;
  private final ImmutableSetMultimap<String, String> candidateMembers;

  /** Instantiates a default {@link StaticImport} instance. */
  public StaticImport() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link StaticImport}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  StaticImport(ErrorProneFlags flags) {
    candidateTypes = getCandidateTypes(flags);
    candidateMembers = getCandidateMembers(flags);
  }

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

    return parentTree instanceof MethodInvocationTree methodInvocation
        ? methodInvocation.getTypeArguments().isEmpty()
        : !(parentTree instanceof ImportTree || parentTree instanceof MemberSelectTree);
  }

  private static boolean isCandidate(MemberSelectTree tree) {
    String identifier = tree.getIdentifier().toString();
    if (NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(identifier)) {
      return false;
    }

    Type type = ASTHelpers.getType(tree.getExpression());
    return type != null
        && !NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type.toString(), identifier);
  }

  private Optional<String> getCandidateSimpleName(StaticImportInfo importInfo) {
    String canonicalName = importInfo.canonicalName();
    return importInfo
        .simpleName()
        .toJavaUtil()
        .filter(
            name ->
                candidateTypes.contains(canonicalName)
                    || candidateMembers.containsEntry(canonicalName, name));
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

  private static ImmutableSet<String> getCandidateTypes(ErrorProneFlags flags) {
    ImmutableSet<String> additionalTypes = Flags.getSet(flags, CANDIDATE_TYPES_FLAG);
    for (String type : additionalTypes) {
      checkArgument(
          isWellFormed(type),
          "Invalid `%s` flag value '%s': expected a canonical type name",
          CANDIDATE_TYPES_FLAG,
          type);
      checkArgument(
          !NON_STATIC_IMPORT_CANDIDATE_TYPES.contains(type),
          "Invalid `%s` flag value '%s': type is a non-static import candidate",
          CANDIDATE_TYPES_FLAG,
          type);
    }

    // XXX: The parameter swap mutant of this `Sets#union` invocation is unkillable: the argument
    // order influences only iteration order, while the result is queried exclusively using
    // `ImmutableSet#contains`.
    return Sets.union(STATIC_IMPORT_CANDIDATE_TYPES, additionalTypes).immutableCopy();
  }

  /**
   * Returns the union of {@link #STATIC_IMPORT_CANDIDATE_MEMBERS} and any additional candidate
   * members specified using the {@link #CANDIDATE_MEMBERS_FLAG} flag.
   */
  static ImmutableSetMultimap<String, String> getCandidateMembers(ErrorProneFlags flags) {
    ImmutableSetMultimap.Builder<String, String> members =
        ImmutableSetMultimap.<String, String>builder().putAll(STATIC_IMPORT_CANDIDATE_MEMBERS);

    for (String candidate : Flags.getSet(flags, CANDIDATE_MEMBERS_FLAG)) {
      List<String> parts = Splitter.on('#').splitToList(candidate);
      checkArgument(
          parts.size() == 2 && parts.stream().allMatch(StaticImport::isWellFormed),
          "Invalid `%s` flag value '%s': expected format is '<type>#<member>'",
          CANDIDATE_MEMBERS_FLAG,
          candidate);
      String type = parts.getFirst();
      String member = parts.get(1);
      checkArgument(
          !NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(member),
          "Invalid `%s` flag value '%s': identifier is a non-static import candidate",
          CANDIDATE_MEMBERS_FLAG,
          candidate);
      checkArgument(
          !NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type, member),
          "Invalid `%s` flag value '%s': member is a non-static import candidate",
          CANDIDATE_MEMBERS_FLAG,
          candidate);
      members.put(type, member);
    }

    return members.build();
  }

  private static boolean isWellFormed(String typeOrMember) {
    return !typeOrMember.isEmpty() && CharMatcher.whitespace().matchesNoneOf(typeOrMember);
  }
}
