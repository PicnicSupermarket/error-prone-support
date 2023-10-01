package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.Flags;

/**
 * A {@link BugChecker} that warns and suggests the fix when a constant variable does not follow the
 * upper snake case naming convention.
 *
 * <p>Example:
 *
 * <p>This checker will re-write the following variables with all its references:
 *
 * <ul>
 *   <li>private static final int number = 1;
 * </ul>
 *
 * <p>To the following:
 *
 * <ul>
 *   <li>private static final int NUMBER = 1;
 * </ul>
 *
 * @apiNote This {@link BugChecker checker} has two optional flags:
 *     <ul>
 *       <li>ExcludedConstantFliedNames: A list of field names to exclude from this check.
 *       <li>IncludePublicConstantFields: Whether to include public constants when running this
 *           check.
 *     </ul>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Make sure that all constant variables follow the `UPPER_SNAKE_CASE` naming convention.",
    link = BUG_PATTERNS_BASE_URL + "CanonicalConstantFieldName",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class CanonicalConstantFieldName extends BugChecker
    implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> IS_CONSTANT =
      allOf(hasModifier(Modifier.STATIC), hasModifier(Modifier.FINAL));
  private static final Matcher<Tree> IS_PRIVATE = hasModifier(Modifier.PRIVATE);
  private static final Pattern TO_SNAKE_CASE = Pattern.compile("([a-z])([A-Z])");
  private static final ImmutableSet<String> DEFAULT_EXCLUDED_CONSTANT_FIELD_NAMES =
      ImmutableSet.of("serialVersionUID");
  private static final String EXCLUDED_CONSTANT_FIELD_NAMES =
      "CanonicalConstantFieldName:ExcludedConstantFliedNames";
  private static final String IS_INCLUDE_PUBLIC_CONSTANT_FIELDS =
      "CanonicalConstantFieldName:IncludePublicConstantFields";
  private final ImmutableList<String> optionalExcludedConstantFliedNames;
  private final boolean includePublicConstantFieldNames;

  /** Instantiates a default {@link CanonicalConstantFieldName} instance. */
  public CanonicalConstantFieldName() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link CanonicalConstantFieldName}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  CanonicalConstantFieldName(ErrorProneFlags flags) {
    optionalExcludedConstantFliedNames = getCanonicalizedLoggerName(flags);
    includePublicConstantFieldNames = isIncludePrivateConstantFieldNames(flags);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    ImmutableList.Builder<VariableTree> variablesInCompilationUnitBuilder = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitClass(ClassTree classTree, @Nullable Void unused) {
        for (Tree member : classTree.getMembers()) {
          if (member.getKind() == Kind.VARIABLE) {
            variablesInCompilationUnitBuilder.add((VariableTree) member);
          }
        }
        return super.visitClass(classTree, unused);
      }
    }.scan(tree, null);

    ImmutableList<VariableTree> variables = variablesInCompilationUnitBuilder.build();
    if (variables.isEmpty()) {
      return Description.NO_MATCH;
    }

    ImmutableList<VarSymbol> variableSymbols =
        variables.stream().map(ASTHelpers::getSymbol).collect(toImmutableList());
    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    variables.forEach(
        variableTree -> {
          if (IS_CONSTANT.matches(variableTree, state)
              && isFieldAccessModifierApplicable(variableTree, state)) {
            VarSymbol variableSymbol = ASTHelpers.getSymbol(variableTree);

            if (variableSymbol != null) {
              String variableName = variableSymbol.getSimpleName().toString();

              if (!isVariableUpperSnakeCase(variableName)
                  && !isVariableNameExcluded(variableName)) {
                String replacement = toUpperSnakeCase(variableName);

                if (variableSymbols.stream()
                    .noneMatch(s -> s.getSimpleName().toString().equals(replacement))) {
                  fixBuilder.merge(SuggestedFixes.renameVariable(variableTree, replacement, state));
                } else {
                  reportConstantRenameBlocker(variableTree, replacement, state);
                }
              }
            }
          }
        });

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void reportConstantRenameBlocker(
      VariableTree tree, String replacement, VisitorState state) {
    state.reportMatch(
        buildDescription(tree)
            .setMessage(
                String.format(
                    "Suggested fix for constant name conflicts with an already defined variable `%s`.",
                    replacement))
            .build());
  }

  private boolean isFieldAccessModifierApplicable(VariableTree tree, VisitorState state) {
    return includePublicConstantFieldNames || IS_PRIVATE.matches(tree, state);
  }

  private static boolean isVariableUpperSnakeCase(String variableName) {
    return variableName.equals(toUpperSnakeCase(variableName));
  }

  private boolean isVariableNameExcluded(String variableName) {
    return optionalExcludedConstantFliedNames.contains(variableName)
        || DEFAULT_EXCLUDED_CONSTANT_FIELD_NAMES.contains(variableName);
  }

  private static String toUpperSnakeCase(String variableName) {
    return TO_SNAKE_CASE.matcher(variableName).replaceAll("$1_$2").toUpperCase(Locale.ROOT);
  }

  private static ImmutableList<String> getCanonicalizedLoggerName(ErrorProneFlags flags) {
    return Flags.getList(flags, EXCLUDED_CONSTANT_FIELD_NAMES);
  }

  private static boolean isIncludePrivateConstantFieldNames(ErrorProneFlags flags) {
    return flags.getBoolean(IS_INCLUDE_PUBLIC_CONSTANT_FIELDS).orElse(false);
  }
}
