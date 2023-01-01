package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toCollection;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.Signatures;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.util.Constants;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BugChecker} that validates the claim made by {@link
 * tech.picnic.errorprone.refaster.annotation.TypeMigration} annotations.
 */
// XXX: As-is this checker assumes that a method is fully migrated if it is invoked inside at least
// one `@BeforeTemplate` method. A stronger check would be to additionally verify that:
// 1. Such invocations are not conditionally matched. That is, there should be no constraint on
//    their context (i.e any surrounding code), and their parameters must be `@BeforeTemplate`
//    method parameters with types that are not more restrictive than those of the method itself.
//    Additionally, the result of non-void methods should be "returned" by the `@BeforeTemplate`
//    method, so that Refaster will match any expression, rather than just statements. (One caveat
//    with this "context-independent migrations only" approach is that APIs often expose methods
//    that are only useful in combination with other methods of the API; insisting that such methods
//    are migrated in isolation is unreasonable.)
// 2. Where relevant, method references should also be migrated. (TBD what "relevant" means in this
//    case, and whether in fact method reference matchers can be _derived_ from the associated
//    method invocation matchers.)
// XXX: This checker currently does no concern itself with public fields. Consider adding support
// for those.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "The set of unmigrated methods listed by the `@TypeMigration` annotation must be minimal yet exhaustive",
    link = BUG_PATTERNS_BASE_URL + "ExhaustiveRefasterTypeMigration",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class ExhaustiveRefasterTypeMigration extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> IS_TYPE_MIGRATION =
      annotations(AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.TypeMigration"));
  private static final MultiMatcher<Tree, AnnotationTree> HAS_BEFORE_TEMPLATE =
      annotations(AT_LEAST_ONE, isType("com.google.errorprone.refaster.annotation.BeforeTemplate"));
  private static final String TYPE_MIGRATION_TYPE_ELEMENT = "of";
  private static final String TYPE_MIGRATION_UNMIGRATED_METHODS_ELEMENT = "unmigratedMethods";

  /** Instantiates a new {@link ExhaustiveRefasterTypeMigration} instance. */
  public ExhaustiveRefasterTypeMigration() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> migrationAnnotations =
        IS_TYPE_MIGRATION.multiMatchResult(tree, state);
    if (!migrationAnnotations.matches()) {
      return Description.NO_MATCH;
    }

    AnnotationTree migrationAnnotation = migrationAnnotations.onlyMatchingNode();
    AnnotationMirror annotationMirror = ASTHelpers.getAnnotationMirror(migrationAnnotation);
    TypeSymbol migratedType = getMigratedType(annotationMirror);
    if (migratedType.asType().isPrimitive() || !(migratedType instanceof ClassSymbol)) {
      return buildDescription(migrationAnnotation)
          .setMessage(String.format("Migration of type '%s' is unsupported", migratedType))
          .build();
    }

    ImmutableList<String> methodsClaimedUnmigrated = getMethodsClaimedUnmigrated(annotationMirror);
    ImmutableList<String> unmigratedMethods =
        getMethodsDefinitelyUnmigrated(
            tree, (ClassSymbol) migratedType, signatureOrder(methodsClaimedUnmigrated), state);

    if (unmigratedMethods.equals(methodsClaimedUnmigrated)) {
      return Description.NO_MATCH;
    }

    /*
     * The `@TypeMigration` annotation lists a different set of unmigrated methods than the one
     * produced by our analysis; suggest a replacement.
     */
    // XXX: `updateAnnotationArgumentValues` will prepend the new attribute argument if it is not
    // already present. It would be nicer if it _appended_ the new attribute.
    return describeMatch(
        migrationAnnotation,
        SuggestedFixes.updateAnnotationArgumentValues(
                migrationAnnotation,
                state,
                TYPE_MIGRATION_UNMIGRATED_METHODS_ELEMENT,
                unmigratedMethods.stream().map(Constants::format).collect(toImmutableList()))
            .build());
  }

  private static TypeSymbol getMigratedType(AnnotationMirror migrationAnnotation) {
    AnnotationValue value =
        AnnotationMirrors.getAnnotationValue(migrationAnnotation, TYPE_MIGRATION_TYPE_ELEMENT);
    verify(
        value instanceof Attribute.Class,
        "Value of annotation element `%s` is '%s' rather than a class",
        TYPE_MIGRATION_TYPE_ELEMENT,
        value);
    return ((Attribute.Class) value).classType.tsym;
  }

  private static ImmutableList<String> getMethodsClaimedUnmigrated(
      AnnotationMirror migrationAnnotation) {
    AnnotationValue value =
        AnnotationMirrors.getAnnotationValue(
            migrationAnnotation, TYPE_MIGRATION_UNMIGRATED_METHODS_ELEMENT);
    verify(
        value instanceof Attribute.Array,
        "Value of annotation element `%s` is '%s' rather than an array",
        TYPE_MIGRATION_UNMIGRATED_METHODS_ELEMENT,
        value);
    return ((Attribute.Array) value)
        .getValue().stream().map(a -> a.getValue().toString()).collect(toImmutableList());
  }

  private static ImmutableList<String> getMethodsDefinitelyUnmigrated(
      ClassTree tree, ClassSymbol migratedType, Comparator<String> comparator, VisitorState state) {
    Set<MethodSymbol> publicMethods =
        Streams.stream(
                ASTHelpers.scope(migratedType.members())
                    .getSymbols(m -> m.isPublic() && m instanceof MethodSymbol))
            .map(MethodSymbol.class::cast)
            .collect(toCollection(HashSet::new));

    /* Remove methods that *appear* to be migrated. Note that this is an imperfect heuristic. */
    removeMethodsInvokedInBeforeTemplateMethods(tree, publicMethods, state);

    return publicMethods.stream()
        .map(m -> Signatures.prettyMethodSignature(migratedType, m))
        .sorted(comparator)
        .collect(toImmutableList());
  }

  /**
   * Creates a {@link Comparator} that orders method signatures to match the given list of
   * signatures, with any signatures not listed ordered first, lexicographically.
   *
   * @implNote This method does not use {@code comparing(list::indexOf)}, as that would make each
   *     comparison a linear, rather than constant-time operation.
   */
  private static Comparator<String> signatureOrder(ImmutableList<String> existingOrder) {
    Map<String, Integer> knownEntries = new HashMap<>();
    for (int i = 0; i < existingOrder.size(); i++) {
      knownEntries.putIfAbsent(existingOrder.get(i), i);
    }

    return comparing((String v) -> knownEntries.getOrDefault(v, -1)).thenComparing(naturalOrder());
  }

  /**
   * Removes from the given set of {@link MethodSymbol}s the ones that refer to a method that is
   * invoked inside a {@link com.google.errorprone.refaster.annotation.BeforeTemplate} method inside
   * the specified {@link ClassTree}.
   */
  private static void removeMethodsInvokedInBeforeTemplateMethods(
      ClassTree tree, Set<MethodSymbol> candidates, VisitorState state) {
    new TreeScanner<@Nullable Void, Boolean>() {
      @Override
      public @Nullable Void visitMethod(MethodTree tree, Boolean inBeforeTemplate) {
        return HAS_BEFORE_TEMPLATE.matches(tree, state) ? super.visitMethod(tree, true) : null;
      }

      @Override
      public @Nullable Void visitNewClass(NewClassTree tree, Boolean inBeforeTemplate) {
        if (inBeforeTemplate) {
          candidates.remove(ASTHelpers.getSymbol(tree));
        }

        return super.visitNewClass(tree, inBeforeTemplate);
      }

      @Override
      public @Nullable Void visitMethodInvocation(
          MethodInvocationTree tree, Boolean inBeforeTemplate) {
        if (inBeforeTemplate) {
          candidates.remove(ASTHelpers.getSymbol(tree));
        }

        return super.visitMethodInvocation(tree, inBeforeTemplate);
      }
    }.scan(tree, false);
  }
}
