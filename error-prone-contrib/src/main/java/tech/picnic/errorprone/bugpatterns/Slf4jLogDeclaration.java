package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static javax.tools.JavaFileObject.Kind.CLASS;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
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
import com.sun.source.util.TreeScanner;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that warns when SLF4J declarations are not canonicalized across the project.
 *
 * @apiNote The default canonicalized logger name can be overridden through {@link ErrorProneFlags
 *     flag arguments}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Make sure SLF4J log declarations follow the SLF4J conventions inside classes.",
    link = BUG_PATTERNS_BASE_URL + "Slf4jLogDeclaration",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class Slf4jLogDeclaration extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> LOGGER = isSubtypeOf("org.slf4j.Logger");
  private static final String CANONICAL_LOGGER_NAME_FLAG =
      "Slf4jLogDeclaration:CanonicalLoggerName";
  private static final String DEFAULT_CANONICAL_LOGGER_NAME = "LOG";
  private static final Matcher<ExpressionTree> GET_LOGGER_METHOD =
      staticMethod().onDescendantOf("org.slf4j.LoggerFactory").named("getLogger");

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
  public Description matchClass(ClassTree tree, VisitorState state) {
    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

    for (Tree member : tree.getMembers()) {
      if (LOGGER.matches(member, state)) {
        if (tree.getKind() != Kind.INTERFACE) {
          suggestCanonicalModifiers(member, fixBuilder, state);
        }
        canonicalizeLoggerVariable((VariableTree) member, fixBuilder, state);
      }
    }
    fixLoggerVariableDeclarations(tree, fixBuilder, state);

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void suggestCanonicalModifiers(
      Tree member, SuggestedFix.Builder fixBuilder, VisitorState state) {
    SuggestedFixes.addModifiers(member, state, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .ifPresent(fixBuilder::merge);
  }

  private void canonicalizeLoggerVariable(
      VariableTree variableTree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    if (!variableTree.getName().contentEquals(canonicalLoggerName)) {
      fixBuilder
          .merge(SuggestedFixes.renameVariable(variableTree, canonicalLoggerName, state))
          .build();
    }
  }

  private static void fixLoggerVariableDeclarations(
      ClassTree tree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    new TreeScanner<@Nullable Void, Name>() {
      @Override
      public @Nullable Void visitClass(ClassTree classTree, Name className) {
        return super.visitClass(classTree, classTree.getSimpleName());
      }

      @Override
      public @Nullable Void visitMethodInvocation(MethodInvocationTree tree, Name className) {
        if (GET_LOGGER_METHOD.matches(tree, state)) {
          ExpressionTree arg = tree.getArguments().get(0);
          String argumentName = SourceCode.treeToString(arg, state);

          String findAName = argumentName.substring(0, argumentName.indexOf(CLASS.extension));
          if (!className.contentEquals(findAName)) {
            fixBuilder.merge(SuggestedFix.replace(arg, className + CLASS.extension));
          }
        }
        return super.visitMethodInvocation(tree, className);
      }
    }.scan(tree, tree.getSimpleName());
  }

  private static String getCanonicalLoggerName(ErrorProneFlags flags) {
    return flags.get(CANONICAL_LOGGER_NAME_FLAG).orElse(DEFAULT_CANONICAL_LOGGER_NAME);
  }
}
