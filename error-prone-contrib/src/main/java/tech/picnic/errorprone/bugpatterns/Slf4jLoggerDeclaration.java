package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.classLiteral;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import java.util.EnumSet;
import java.util.Objects;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} that flags non-canonical SLF4J logger declarations. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "SLF4J logger declarations should follow established best-practices",
    link = BUG_PATTERNS_BASE_URL + "Slf4jLoggerDeclaration",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class Slf4jLoggerDeclaration extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> IS_GET_LOGGER =
      staticMethod().onDescendantOf("org.slf4j.LoggerFactory").named("getLogger");
  private static final String CANONICAL_STATIC_LOGGER_NAME_FLAG =
      "Slf4jLogDeclaration:CanonicalStaticLoggerName";
  private static final String DEFAULT_CANONICAL_LOGGER_NAME = "LOG";
  private static final Matcher<ExpressionTree> IS_STATIC_ENCLOSING_CLASS_REFERENCE =
      classLiteral(Slf4jLoggerDeclaration::isEnclosingClassReference);
  private static final Matcher<ExpressionTree> IS_DYNAMIC_ENCLOSING_CLASS_REFERENCE =
      toType(
          MethodInvocationTree.class,
          allOf(
              instanceMethod().anyClass().named("getClass").withNoParameters(),
              Slf4jLoggerDeclaration::receiverIsEnclosingClassInstance));
  private static final ImmutableSet<Modifier> INSTANCE_DECLARATION_MODIFIERS =
      Sets.immutableEnumSet(Modifier.PRIVATE, Modifier.FINAL);
  private static final ImmutableSet<Modifier> STATIC_DECLARATION_MODIFIERS =
      Sets.immutableEnumSet(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

  private final String canonicalStaticName;
  private final String canonicalInstanceName;

  /** Instantiates a default {@link Slf4jLoggerDeclaration} instance. */
  public Slf4jLoggerDeclaration() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link Slf4jLoggerDeclaration}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  Slf4jLoggerDeclaration(ErrorProneFlags flags) {
    canonicalStaticName =
        flags.get(CANONICAL_STATIC_LOGGER_NAME_FLAG).orElse(DEFAULT_CANONICAL_LOGGER_NAME);
    canonicalInstanceName =
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, canonicalStaticName);
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    ExpressionTree initializer = tree.getInitializer();
    if (!IS_GET_LOGGER.matches(initializer, state)) {
      return Description.NO_MATCH;
    }

    ClassTree clazz = getEnclosingClass(state);
    ExpressionTree factoryArg =
        Iterables.getOnlyElement(((MethodInvocationTree) initializer).getArguments());

    SuggestedFix.Builder fix = SuggestedFix.builder();

    if (clazz.getModifiers().getFlags().contains(Modifier.ABSTRACT)
        && IS_DYNAMIC_ENCLOSING_CLASS_REFERENCE.matches(factoryArg, state)) {
      suggestModifiers(tree, INSTANCE_DECLARATION_MODIFIERS, fix, state);
      suggestRename(tree, canonicalInstanceName, fix, state);
    } else {
      suggestModifiers(
          tree,
          clazz.getKind() == Kind.INTERFACE ? ImmutableSet.of() : STATIC_DECLARATION_MODIFIERS,
          fix,
          state);
      suggestRename(tree, canonicalStaticName, fix, state);

      if (!ASTHelpers.isSameType(
              ASTHelpers.getType(factoryArg), state.getSymtab().stringType, state)
          && !IS_STATIC_ENCLOSING_CLASS_REFERENCE.matches(factoryArg, state)) {
        fix.merge(SuggestedFix.replace(factoryArg, clazz.getSimpleName() + ".class"));
      }
    }

    return fix.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fix.build());
  }

  private static void suggestModifiers(
      Tree tree,
      ImmutableSet<Modifier> modifiers,
      SuggestedFix.Builder fixBuilder,
      VisitorState state) {
    ModifiersTree modifiersTree = ASTHelpers.getModifiers(tree);
    verify(modifiersTree != null, "Given tree does not support modifiers");
    SuggestedFixes.addModifiers(tree, modifiersTree, state, modifiers).ifPresent(fixBuilder::merge);
    SuggestedFixes.removeModifiers(
            modifiersTree, state, Sets.difference(EnumSet.allOf(Modifier.class), modifiers))
        .ifPresent(fixBuilder::merge);
  }

  private static void suggestRename(
      VariableTree variableTree, String name, SuggestedFix.Builder fixBuilder, VisitorState state) {
    if (!variableTree.getName().contentEquals(name)) {
      fixBuilder.merge(SuggestedFixes.renameVariable(variableTree, name, state));
    }
  }

  private static boolean isEnclosingClassReference(ExpressionTree tree, VisitorState state) {
    ClassTree enclosing = state.findEnclosing(ClassTree.class);
    return enclosing != null
        && Objects.equals(ASTHelpers.getSymbol(tree), ASTHelpers.getSymbol(enclosing));
  }

  private static boolean receiverIsEnclosingClassInstance(
      MethodInvocationTree tree, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(tree);
    if (receiver == null) {
      return true;
    }

    Symbol symbol = ASTHelpers.getSymbol(receiver);
    return symbol != null
        && symbol.asType().tsym.equals(ASTHelpers.getSymbol(getEnclosingClass(state)));
  }

  private static ClassTree getEnclosingClass(VisitorState state) {
    ClassTree clazz = state.findEnclosing(ClassTree.class);
    // XXX: Review whether we should relax this constraint in the face of so-called anonymous
    // classes. See
    // https://docs.oracle.com/en/java/javase/23/language/implicitly-declared-classes-and-instance-main-methods.html
    verify(clazz != null, "Variable not defined inside class");
    return clazz;
  }
}
