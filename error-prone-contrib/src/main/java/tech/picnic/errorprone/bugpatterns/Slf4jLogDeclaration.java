package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
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
// XXX: Do not match on `Class` but on `VariableTree`. That improves on the reporting.
public final class Slf4jLogDeclaration extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> LOGGER = isSubtypeOf("org.slf4j.Logger");
  private static final Matcher<ExpressionTree> GET_LOGGER_METHOD =
      staticMethod().onDescendantOf("org.slf4j.LoggerFactory").named("getLogger");

  private static final String CANONICAL_LOGGER_NAME_FLAG =
      "Slf4jLogDeclaration:CanonicalLoggerName";
  private static final String DEFAULT_CANONICAL_LOGGER_NAME = "LOG";
  private static final Pattern STRING_LITERAL_ARGUMENT = Pattern.compile("\"(.*?)\"");
  private static final Pattern CLASS_ARGUMENT = Pattern.compile("(.*?)\\.class");

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
    if (!LOGGER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

    Symbol enclosingElement = ASTHelpers.getSymbol(tree).getEnclosingElement();
    if (enclosingElement == null) {
      return Description.NO_MATCH;
    }

    if (enclosingElement.getKind() != ElementKind.INTERFACE) {
      suggestCanonicalModifiers(tree, fixBuilder, state);
    }
    canonicalizeLoggerVariable(tree, fixBuilder, state);
    updateGetLoggerArgument(tree, enclosingElement.getSimpleName(), fixBuilder, state);

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void canonicalizeLoggerVariable(
      VariableTree variableTree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    if (!variableTree.getName().contentEquals(canonicalLoggerName)) {
      fixBuilder.merge(SuggestedFixes.renameVariable(variableTree, canonicalLoggerName, state));
    }
  }

  private static void updateGetLoggerArgument(
      VariableTree tree,
      Name enclosingElementName,
      SuggestedFix.Builder fixBuilder,
      VisitorState state) {
    new TreeScanner<@Nullable Void, Name>() {
      @Override
      public @Nullable Void visitMethodInvocation(
          MethodInvocationTree tree, Name enclosingElementName) {
        if (GET_LOGGER_METHOD.matches(tree, state)) {
          ExpressionTree arg = Iterables.getOnlyElement(tree.getArguments());
          String argumentName = SourceCode.treeToString(arg, state);

          String argumentClassName;
          if (arg.getKind() == Kind.STRING_LITERAL) {
            java.util.regex.Matcher matcher = STRING_LITERAL_ARGUMENT.matcher(argumentName);
            checkArgument(matcher.matches(), "Invalid argument name.");
            argumentClassName = matcher.group(1);
          } else {
            java.util.regex.Matcher matcher = CLASS_ARGUMENT.matcher(argumentName);
            checkArgument(matcher.matches(), "Invalid argument name.");
            argumentClassName = matcher.group(1);
          }

          if (!enclosingElementName.contentEquals(argumentClassName)) {
            fixBuilder.merge(
                SuggestedFix.replace(
                    arg, argumentName.replace(argumentClassName, enclosingElementName)));
          }
        }
        return super.visitMethodInvocation(tree, enclosingElementName);
      }
    }.scan(tree, enclosingElementName);
  }

  private static void suggestCanonicalModifiers(
      Tree member, SuggestedFix.Builder fixBuilder, VisitorState state) {
    SuggestedFixes.addModifiers(member, state, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .ifPresent(fixBuilder::merge);
  }

  private static String getCanonicalLoggerName(ErrorProneFlags flags) {
    return flags.get(CANONICAL_LOGGER_NAME_FLAG).orElse(DEFAULT_CANONICAL_LOGGER_NAME);
  }
}
