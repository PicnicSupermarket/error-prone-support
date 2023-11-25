package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

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
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that warns when SLF4J declarations are not canonicalized across the project.
 *
 * @apiNote The default canonicalized logger name can be overriden through {@link ErrorProneFlags
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
  private static final String CANONICALIZED_LOGGER_NAME_FLAG =
      "Slf4jLogDeclaration:CanonicalizedLoggerName";
  private static final String DEFAULT_CANONICALIZED_LOGGER_NAME = "LOG";
  private static final Matcher<ExpressionTree> GET_LOGGER_METHOD =
      staticMethod().onDescendantOf("org.slf4j.LoggerFactory").named("getLogger");

  private final String canonicalizedLoggerName;

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
    canonicalizedLoggerName = getCanonicalizedLoggerName(flags);
  }

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

    for (Tree member : tree.getMembers()) {
      if (LOGGER.matches(member, state)) {
        if (tree.getKind() != Kind.INTERFACE) {
          fixLoggerVariableModifiers(member, state, fixBuilder);
        }
        canonicalizeLoggerVariable(member, state, fixBuilder);
      }
    }
    fixLoggerVariableDeclaration(state, fixBuilder);

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private static void fixLoggerVariableModifiers(
      Tree member, VisitorState state, SuggestedFix.Builder fixBuilder) {
    SuggestedFixes.addModifiers(member, state, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .ifPresent(fixBuilder::merge);
  }

  private void canonicalizeLoggerVariable(
      Tree member, VisitorState state, SuggestedFix.Builder fixBuilder) {
    VariableTree variable = (VariableTree) member;
    if (!variable.getName().toString().equals(canonicalizedLoggerName)) {
      fixBuilder.merge(SuggestedFixes.renameVariable(variable, canonicalizedLoggerName, state));
    }
  }

  private static void fixLoggerVariableDeclaration(
      VisitorState state, SuggestedFix.Builder fixBuilder) {
    for (Tree typeDeclaration : state.getPath().getCompilationUnit().getTypeDecls()) {
      if (typeDeclaration instanceof ClassTree) {
        new TreeScanner<@Nullable Void, Name>() {
          @Override
          public @Nullable Void visitClass(ClassTree classTree, Name className) {
            return super.visitClass(classTree, classTree.getSimpleName());
          }

          @Override
          public @Nullable Void visitMethodInvocation(
              MethodInvocationTree methodTree, Name className) {
            if (GET_LOGGER_METHOD.matches(methodTree, state)) {
              ExpressionTree arg1 = methodTree.getArguments().get(0);
              String argumentName = SourceCode.treeToString(arg1, state);

              if (!className.contentEquals(argumentName)) {
                fixBuilder.merge(
                    SuggestedFix.replace(arg1, className + JavaFileObject.Kind.CLASS.extension));
              }
            }
            return super.visitMethodInvocation(methodTree, className);
          }
        }.scan(typeDeclaration, null);
      }
    }
  }

  private static String getCanonicalizedLoggerName(ErrorProneFlags flags) {
    return flags.get(CANONICALIZED_LOGGER_NAME_FLAG).orElse(DEFAULT_CANONICALIZED_LOGGER_NAME);
  }
}
