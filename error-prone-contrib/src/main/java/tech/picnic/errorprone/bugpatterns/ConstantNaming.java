package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.Flags;

/**
 * A {@link BugChecker} that flags static constants that do not follow the upper snake case naming
 * convention.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Constant variables should adhere to the `UPPER_SNAKE_CASE` naming convention",
    link = BUG_PATTERNS_BASE_URL + "ConstantNaming",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class ConstantNaming extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<VariableTree> IS_CONSTANT =
      allOf(hasModifier(Modifier.STATIC), hasModifier(Modifier.FINAL));
  private static final Matcher<VariableTree> IS_PRIVATE = hasModifier(Modifier.PRIVATE);
  private static final Pattern SNAKE_CASE = Pattern.compile("([a-z])([A-Z])");
  private static final ImmutableSet<String> DEFAULT_EXEMPTED_NAMES =
      ImmutableSet.of("serialVersionUID");

  /**
   * Flag using which constant names that must not be flagged (in addition to those defined by
   * {@link #DEFAULT_EXEMPTED_NAMES}) can be specified.
   */
  private static final String ADDITIONAL_EXEMPTED_NAMES_FLAG =
      "CanonicalConstantNaming:ExemptedNames";

  private final ImmutableSet<String> exemptedNames;

  /** Instantiates a default {@link ConstantNaming} instance. */
  public ConstantNaming() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link ConstantNaming}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  ConstantNaming(ErrorProneFlags flags) {
    exemptedNames =
        Sets.union(DEFAULT_EXEMPTED_NAMES, Flags.getSet(flags, ADDITIONAL_EXEMPTED_NAMES_FLAG))
            .immutableCopy();
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    String variableName = tree.getName().toString();
    if (!IS_CONSTANT.matches(tree, state) || exemptedNames.contains(variableName)) {
      return Description.NO_MATCH;
    }

    String replacement = toUpperSnakeCase(variableName);
    if (replacement.equals(variableName)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);
    if (!IS_PRIVATE.matches(tree, state)) {
      description.setMessage(
          "%s; consider renaming to '%s', though note that this is not a private constant"
              .formatted(message(), replacement));
    } else if (isVariableNameInUse(replacement, state)) {
      description.setMessage(
          "%s; consider renaming to '%s', though note that a variable with this name is already declared"
              .formatted(message(), replacement));
    } else {
      description.addFix(SuggestedFixes.renameVariable(tree, replacement, state));
    }

    return description.build();
  }

  private static String toUpperSnakeCase(String variableName) {
    return SNAKE_CASE.matcher(variableName).replaceAll("$1_$2").toUpperCase(Locale.ROOT);
  }

  private static boolean isVariableNameInUse(String name, VisitorState state) {
    return Boolean.TRUE.equals(
        new TreeScanner<Boolean, @Nullable Void>() {
          @Override
          public Boolean visitVariable(VariableTree tree, @Nullable Void unused) {
            return ASTHelpers.getSymbol(tree).getSimpleName().contentEquals(name)
                || super.visitVariable(tree, null);
          }

          @Override
          public Boolean reduce(Boolean r1, Boolean r2) {
            return Boolean.TRUE.equals(r1) || Boolean.TRUE.equals(r2);
          }
        }.scan(state.getPath().getCompilationUnit(), null));
  }
}
