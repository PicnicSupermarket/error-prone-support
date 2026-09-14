package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
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
import com.google.errorprone.bugpatterns.StaticImports.StaticImportInfo;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.code.Type;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.InstantSource;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
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
import tech.picnic.errorprone.utils.TypeMemberSpec;

/**
 * The shared configuration and matching logic for the {@link StaticImport} and {@link
 * NonStaticImport} checks.
 *
 * <p>Additional candidates can be specified using the {@code StaticImport:AdditionalCandidates}
 * flag: a comma-separated list of entries, each either a canonical type name (all of whose members
 * are then eligible for static import) or a {@code <type>#<member>} pair (identifying a single
 * additional member). Entries specified this way are honored by both checks, so {@link
 * NonStaticImport} never undoes a static import introduced by {@link StaticImport}.
 */
final class StaticImportConfig implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Types whose members should be statically imported, unless exempted by {@link
   * #NON_STATIC_IMPORT_CANDIDATE_MEMBERS} or {@link #NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS}.
   *
   * <p>Types listed here should be mutually exclusive with {@link
   * #NON_STATIC_IMPORT_CANDIDATE_TYPES}.
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
   *       #NON_STATIC_IMPORT_CANDIDATE_MEMBERS}.
   *   <li>This collection should not list members contained in {@link
   *       #NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS}.
   * </ul>
   */
  @VisibleForTesting
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
   * Types whose members should not be statically imported, unless exempted by {@link
   * #STATIC_IMPORT_CANDIDATE_MEMBERS}.
   *
   * <p>Types listed here should be mutually exclusive with {@link #STATIC_IMPORT_CANDIDATE_TYPES}.
   */
  @VisibleForTesting
  static final ImmutableSet<String> NON_STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of(
          ASTHelpers.class.getCanonicalName(),
          Clock.class.getCanonicalName(),
          InstantSource.class.getCanonicalName(),
          Strings.class.getCanonicalName(),
          VisitorState.class.getCanonicalName(),
          ZoneOffset.class.getCanonicalName(),
          "com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode",
          "reactor.core.publisher.Flux",
          "reactor.core.publisher.Mono");

  /**
   * Type members that should never be statically imported.
   *
   * <p>Please note that:
   *
   * <ul>
   *   <li>Types listed by {@link #NON_STATIC_IMPORT_CANDIDATE_TYPES} and members listed by {@link
   *       #NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS} should be omitted from this collection.
   *   <li>This collection should be mutually exclusive with {@link
   *       #STATIC_IMPORT_CANDIDATE_MEMBERS}.
   * </ul>
   */
  // XXX: Perhaps the set of exempted `java.util.Collections` methods is too strict. For now any
  // method name that could be considered "too vague" or could conceivably mean something else in a
  // specific context is left out.
  @VisibleForTesting
  static final ImmutableSetMultimap<String, String> NON_STATIC_IMPORT_CANDIDATE_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .putAll(
              Collections.class.getCanonicalName(),
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
          .put(Locale.class.getCanonicalName(), "ROOT")
          .put(Optional.class.getCanonicalName(), "empty")
          .putAll(Pattern.class.getCanonicalName(), "compile", "matches", "quote")
          .put(Predicates.class.getCanonicalName(), "contains")
          .put("org.springframework.http.MediaType", "ALL")
          .build();

  /**
   * Identifiers that should never be statically imported.
   *
   * <p>Please note that:
   *
   * <ul>
   *   <li>Identifiers listed by {@link #STATIC_IMPORT_CANDIDATE_MEMBERS} should be mutually
   *       exclusive with identifiers listed here.
   *   <li>This list should contain a superset of the identifiers flagged by {@link
   *       com.google.errorprone.bugpatterns.BadImport}.
   * </ul>
   */
  @VisibleForTesting
  static final ImmutableSet<String> NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS =
      ImmutableSet.of(
          "builder",
          "copyOf",
          "create",
          "EPOCH",
          "from",
          "getDefaultInstance",
          "INSTANCE",
          "MAX",
          "MAX_VALUE",
          "MIN",
          "MIN_VALUE",
          "newBuilder",
          "newInstance",
          "of",
          "parse",
          "valueOf",
          "values");

  /**
   * Flag using which additional candidates can be specified, in {@code <type>} or {@code
   * <type>#<member>} format.
   */
  private static final String ADDITIONAL_CANDIDATES_FLAG = "StaticImport:AdditionalCandidates";

  private final ImmutableSet<String> candidateTypes;
  private final ImmutableSetMultimap<String, String> candidateMembers;

  /**
   * Instantiates a {@link StaticImportConfig}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  StaticImportConfig(ErrorProneFlags flags) {
    AdditionalCandidates additional = parseAdditionalCandidates(flags);
    candidateTypes =
        // XXX: The parameter swap mutant of this `Sets#union` invocation is unkillable: the
        // argument order influences only iteration order, while the result is queried
        // exclusively using `ImmutableSet#contains`.
        Sets.union(STATIC_IMPORT_CANDIDATE_TYPES, additional.types()).immutableCopy();
    candidateMembers =
        ImmutableSetMultimap.<String, String>builder()
            .putAll(STATIC_IMPORT_CANDIDATE_MEMBERS)
            .putAll(additional.members())
            .build();
  }

  /**
   * Tells whether the given {@link MemberSelectTree} identifies a type member that could be
   * eligible for static import, regardless of whether it is actually a configured candidate.
   */
  boolean isCandidate(MemberSelectTree tree) {
    String identifier = tree.getIdentifier().toString();
    if (NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(identifier)) {
      return false;
    }

    Type type = ASTHelpers.getType(tree.getExpression());
    return type != null
        && !NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type.toString(), identifier);
  }

  /** Returns the simple name under which the given static import candidate should be imported. */
  Optional<String> getCandidateSimpleName(StaticImportInfo importInfo) {
    String canonicalName = importInfo.canonicalName();
    return importInfo
        .simpleName()
        .toJavaUtil()
        .filter(
            name ->
                candidateTypes.contains(canonicalName)
                    || candidateMembers.containsEntry(canonicalName, name));
  }

  /** Tells whether the given statically imported type member should not have been. */
  boolean shouldNotBeStaticallyImported(String type, String member) {
    return (NON_STATIC_IMPORT_CANDIDATE_TYPES.contains(type)
            && !candidateMembers.containsEntry(type, member))
        || NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type, member)
        || NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(member);
  }

  private static AdditionalCandidates parseAdditionalCandidates(ErrorProneFlags flags) {
    ImmutableSet.Builder<String> types = ImmutableSet.builder();
    ImmutableSetMultimap.Builder<String, String> members = ImmutableSetMultimap.builder();

    for (String entry : Flags.getSet(flags, ADDITIONAL_CANDIDATES_FLAG)) {
      TypeMemberSpec spec = TypeMemberSpec.parse(entry);
      checkArgument(
          isWellFormed(spec.type())
              && spec.member().map(StaticImportConfig::isWellFormed).orElse(true),
          "Invalid `%s` flag value '%s': expected '<type>' or '<type>#<member>'",
          ADDITIONAL_CANDIDATES_FLAG,
          entry);

      if (spec.member().isEmpty()) {
        checkArgument(
            !NON_STATIC_IMPORT_CANDIDATE_TYPES.contains(spec.type()),
            "Invalid `%s` flag value '%s': type is a non-static import candidate",
            ADDITIONAL_CANDIDATES_FLAG,
            entry);
        types.add(spec.type());
      } else {
        String member = spec.member().orElseThrow();
        checkArgument(
            !NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(member),
            "Invalid `%s` flag value '%s': identifier is a non-static import candidate",
            ADDITIONAL_CANDIDATES_FLAG,
            entry);
        checkArgument(
            !NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(spec.type(), member),
            "Invalid `%s` flag value '%s': member is a non-static import candidate",
            ADDITIONAL_CANDIDATES_FLAG,
            entry);
        members.put(spec.type(), member);
      }
    }

    return new AdditionalCandidates(types.build(), members.build());
  }

  private static boolean isWellFormed(String typeOrMember) {
    return !typeOrMember.isEmpty()
        && CharMatcher.whitespace().matchesNoneOf(typeOrMember)
        && typeOrMember.indexOf('#') < 0;
  }

  private record AdditionalCandidates(
      ImmutableSet<String> types, ImmutableSetMultimap<String, String> members) {}
}
