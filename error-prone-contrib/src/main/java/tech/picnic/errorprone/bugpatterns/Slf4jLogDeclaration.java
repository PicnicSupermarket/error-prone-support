package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that warns when SLF4J declarations are not canonical.
 *
 * @apiNote The default canonical logger name can be overridden through {@link ErrorProneFlags flag
 *     arguments}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Make sure SLF4J log declarations follow the SLF4J conventions inside classes.",
    link = BUG_PATTERNS_BASE_URL + "Slf4jLogDeclaration",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class Slf4jLogDeclaration extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> GET_LOGGER_METHOD =
      staticMethod().onDescendantOf("org.slf4j.LoggerFactory").named("getLogger");
  private static final String CANONICAL_LOGGER_NAME_FLAG =
      "Slf4jLogDeclaration:CanonicalLoggerName";
  private static final String DEFAULT_CANONICAL_LOGGER_NAME = "LOG";
  private static final Pattern STRING_LITERAL_ARGUMENT_PATTERN = Pattern.compile("\"(.*?)\"");
  private static final Pattern CLASS_ARGUMENT_PATTERN = Pattern.compile("(.*?)\\.class");

  private final String canonicalLoggerName;

  /** Instantiates a default {@link Slf4jLogDeclaration} instance. */
  public Slf4jLogDeclaration() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link Slf4jLogDeclaration}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  Slf4jLogDeclaration(ErrorProneFlags flags) {
    canonicalLoggerName = getCanonicalLoggerName(flags);
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    if (!GET_LOGGER_METHOD.matches(tree.getInitializer(), state)) {
      return Description.NO_MATCH;
    }

    ClassTree clazz = state.findEnclosing(ClassTree.class);
    if (clazz == null) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    if (clazz.getKind() != Kind.INTERFACE) {
      suggestCanonicalModifiers(tree, fixBuilder, state);
    }
    canonicalizeLoggerVariableName(tree, fixBuilder, state);
    updateGetLoggerArgument(
        (MethodInvocationTree) tree.getInitializer(), clazz.getSimpleName(), fixBuilder, state);

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void canonicalizeLoggerVariableName(
      VariableTree variableTree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    if (!variableTree.getName().contentEquals(canonicalLoggerName)) {
      fixBuilder.merge(SuggestedFixes.renameVariable(variableTree, canonicalLoggerName, state));
    }
  }

  private static void updateGetLoggerArgument(
      MethodInvocationTree tree,
      Name className,
      SuggestedFix.Builder fixBuilder,
      VisitorState state) {
    ExpressionTree arg = Iterables.getOnlyElement(tree.getArguments());
    String argumentName = SourceCode.treeToString(arg, state);

    java.util.regex.Matcher matcher;
    if (arg.getKind() == Kind.STRING_LITERAL) {
      matcher = STRING_LITERAL_ARGUMENT_PATTERN.matcher(argumentName);
    } else {
      matcher = CLASS_ARGUMENT_PATTERN.matcher(argumentName);
    }

    checkState(matcher.matches());
    String argumentClassName = matcher.group(1);
    if (!className.contentEquals(argumentClassName)) {
      fixBuilder.merge(
          SuggestedFix.replace(arg, argumentName.replace(argumentClassName, className)));
    }
  }

  private static void suggestCanonicalModifiers(
      Tree tree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    SuggestedFixes.addModifiers(tree, state, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .ifPresent(fixBuilder::merge);
  }

  private static String getCanonicalLoggerName(ErrorProneFlags flags) {
    return flags.get(CANONICAL_LOGGER_NAME_FLAG).orElse(DEFAULT_CANONICAL_LOGGER_NAME);
  }
}
