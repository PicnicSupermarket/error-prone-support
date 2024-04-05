package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.Flags;

/**
 * A {@link BugChecker} that flags constant variables that do not follow the upper snake case naming
 * convention.
 *
 * <p>This check will rewrite the following variables with all its references:
 *
 * <pre>{@code
 * private static final int simpleNumber = 1;
 * }</pre>
 *
 * <p>To the following:
 *
 * <pre>{@code
 * private static final int SIMPLE_NUMBER = 1;
 * }</pre>
 *
 * @apiNote This check has an optional flag `ExcludedConstantFieldNames` that represents a list of
 *     field names to exclude from this check.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Constant variables should adhere to the `UPPER_SNAKE_CASE` naming convention",
    link = BUG_PATTERNS_BASE_URL + "CanonicalConstantFieldName",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class CanonicalConstantFieldName extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> IS_CONSTANT =
      allOf(
          hasModifier(Modifier.STATIC), hasModifier(Modifier.PRIVATE), hasModifier(Modifier.FINAL));
  private static final Pattern TO_SNAKE_CASE = Pattern.compile("([a-z])([A-Z])");
  private static final ImmutableSet<String> DEFAULT_EXCLUDED_CONSTANT_FIELD_NAMES =
      ImmutableSet.of("serialVersionUID");
  private static final String EXCLUDED_CONSTANT_FIELD_NAMES =
      "CanonicalConstantFieldName:ExcludedConstantFieldNames";

  private final ImmutableList<String> optionalExcludedConstantFieldNames;

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
    optionalExcludedConstantFieldNames = getAllowedFieldNames(flags);
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    String variableName = tree.getName().toString();
    if (IS_CONSTANT.matches(tree, state)
        && !isUpperSnakeCase(variableName)
        && !isVariableNameExcluded(variableName)) {
      SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

      ImmutableList<VariableTree> variablesInCompilationUnit =
          getVariablesInCompilationUnit(state.getPath().getCompilationUnit());
      String replacement = toUpperSnakeCase(variableName);
      if (variablesInCompilationUnit.stream()
          .map(ASTHelpers::getSymbol)
          .noneMatch(s -> s.getSimpleName().toString().equals(replacement))) {
        fixBuilder.merge(SuggestedFixes.renameVariable(tree, replacement, state));
      } else {
        reportConstantRenameBlocker(tree, replacement, state);
      }

      return describeMatch(tree, fixBuilder.build());
    }

    return Description.NO_MATCH;
  }

  private static ImmutableList<VariableTree> getVariablesInCompilationUnit(
      CompilationUnitTree tree) {
    ImmutableList.Builder<VariableTree> variablesInFileBuilder = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitVariable(VariableTree variableTree, @Nullable Void unused) {
        variablesInFileBuilder.add(variableTree);
        return super.visitVariable(variableTree, unused);
      }
    }.scan(tree, null);

    return variablesInFileBuilder.build();
  }

  private void reportConstantRenameBlocker(
      VariableTree tree, String replacement, VisitorState state) {
    state.reportMatch(
        buildDescription(tree)
            .setMessage(
                String.format(
                    "a variable named `%s` is already defined in this scope", replacement))
            .build());
  }

  private static boolean isUpperSnakeCase(String name) {
    return name.contentEquals(toUpperSnakeCase(name));
  }

  private boolean isVariableNameExcluded(String variableName) {
    return optionalExcludedConstantFieldNames.contains(variableName)
        || DEFAULT_EXCLUDED_CONSTANT_FIELD_NAMES.contains(variableName);
  }

  private static String toUpperSnakeCase(String variableName) {
    return TO_SNAKE_CASE.matcher(variableName).replaceAll("$1_$2").toUpperCase(Locale.ROOT);
  }

  private static ImmutableList<String> getAllowedFieldNames(ErrorProneFlags flags) {
    return Flags.getList(flags, EXCLUDED_CONSTANT_FIELD_NAMES);
  }
}
