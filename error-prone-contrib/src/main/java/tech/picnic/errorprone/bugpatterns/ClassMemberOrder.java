package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.base.VerifyException;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.util.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} that flags classes with a non-canonical member order.
 *
 * <p>Class members should be ordered as follows:
 *
 * <ul>
 *   <li>Static fields
 *   <li>Instance fields
 *   <li>Static initializer blocks
 *   <li>Instance initializer blocks
 *   <li>Constructors
 *   <li>Methods
 *   <li>Nested classes
 * </ul>
 *
 * @see <a
 *     href="https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/coding/DeclarationOrderCheck.html">Checkstyle's
 *     {@code DeclarationOrderCheck}</a>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Class members should be ordered in a canonical way",
    link = BUG_PATTERNS_BASE_URL + "ClassMemberOrder",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
// XXX: class -> type.
public final class ClassMemberOrder extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Orders {@link Tree}s to match the standard Java type member declaration order. */
  private static final Comparator<Tree> BY_PREFERRED_TYPE_MEMBER_ORDER =
      comparing(
          tree -> {
            switch (tree.getKind()) {
              case VARIABLE:
                return isStatic((VariableTree) tree) ? 1 : 2;
              case BLOCK:
                return ((BlockTree) tree).isStatic() ? 3 : 4;
              case METHOD:
                return isConstructor((MethodTree) tree) ? 5 : 6;
              case CLASS:
                // XXX: Ideally we also sort static nested classes after non-static nested classes.
                // But as this could create quite some unnecessary churn, we should first introduce
                // a check that flags nested classes that could be static, but aren't.
                return 7;
              default:
                throw new VerifyException("Unexpected member kind: " + tree.getKind());
            }
          });

  /** Instantiates a new {@link ClassMemberOrder} instance. */
  public ClassMemberOrder() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<Tree> sortableMembers =
        tree.getMembers().stream().filter(m -> canMove(m, state)).collect(toImmutableList());

    if (Comparators.isInOrder(sortableMembers, BY_PREFERRED_TYPE_MEMBER_ORDER)) {
      return Description.NO_MATCH;
    }

    int classBodyStart = getBodyStartPos(tree, state);
    if (classBodyStart == Position.NOPOS) {
      /*
       * We can't determine the class body's start position in the source code. This generally means
       * that (part of) its code was generated. Even if the source code for a subset of its members
       * is available, dealing with this edge case is not worth the trouble.
       */
      return Description.NO_MATCH;
    }

    return describeMatch(tree, sortClassMembers(classBodyStart, sortableMembers, state));
  }

  private boolean canMove(Tree tree, VisitorState state) {
    return state.getEndPosition(tree) != Position.NOPOS && !isSuppressed(tree, state);
  }

  private static int getBodyStartPos(ClassTree clazz, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    int classStart = ASTHelpers.getStartPosition(clazz);
    int classEnd = state.getEndPosition(clazz);
    if (sourceCode == null || classStart == Position.NOPOS || classEnd == Position.NOPOS) {
      return Position.NOPOS;
    }

    /*
     * We return the source code position that follows the first left brace after the `class`
     * keyword.
     */
    // XXX: !! Doesn't work for interfaces. How to make exhaustive? For records we'd even need
    // special handling. (But maybe we should support only classes and interfaces for now? Enums
    // have other considerations!)
    return ErrorProneTokens.getTokens(
            sourceCode.subSequence(classStart, classEnd).toString(), classStart, state.context)
        .stream()
        .dropWhile(t -> t.kind() != TokenKind.CLASS)
        .dropWhile(t -> t.kind() != TokenKind.LBRACE)
        .findFirst()
        .map(ErrorProneToken::endPos)
        .orElse(Position.NOPOS);
  }

  /**
   * Suggests a different way of ordering the given class members.
   *
   * @implNote For each member, this method tracks the source code between the end of the definition
   *     of the member that precedes it (or the start of the class body if there is no such member)
   *     and the end of the definition of the member itself. This subsequently enables moving
   *     members around, including any preceding comments and Javadoc. This approach isn't perfect,
   *     and may at times move too much code or documentation around; users will have to manually
   *     resolve this.
   */
  private static SuggestedFix sortClassMembers(
      int classBodyStart, ImmutableList<Tree> members, VisitorState state) {
    List<ClassMember> membersWithSource = new ArrayList<>();

    @Var int start = classBodyStart;
    for (Tree member : members) {
      int end = state.getEndPosition(member);
      verify(end != Position.NOPOS && start < end, "Unexpected member end position");
      membersWithSource.add(new AutoValue_ClassMemberOrder_ClassMember(member, start, end));
      start = end;
    }

    CharSequence sourceCode = requireNonNull(state.getSourceCode(), "Source code");
    return Streams.zip(
            membersWithSource.stream(),
            membersWithSource.stream()
                .sorted(comparing(ClassMember::tree, BY_PREFERRED_TYPE_MEMBER_ORDER)),
            (original, replacement) -> original.replaceWith(replacement, sourceCode))
        .reduce(SuggestedFix.builder(), SuggestedFix.Builder::merge, SuggestedFix.Builder::merge)
        .build();
  }

  private static boolean isStatic(VariableTree variableTree) {
    return variableTree.getModifiers().getFlags().contains(Modifier.STATIC);
  }

  private static boolean isConstructor(MethodTree methodTree) {
    return ASTHelpers.getSymbol(methodTree).isConstructor();
  }

  @AutoValue
  abstract static class ClassMember {
    abstract Tree tree();

    abstract int startPosition();

    abstract int endPosition();

    SuggestedFix replaceWith(ClassMember other, CharSequence fullSourceCode) {
      return equals(other)
          ? SuggestedFix.emptyFix()
          : SuggestedFix.replace(
              startPosition(),
              endPosition(),
              fullSourceCode.subSequence(other.startPosition(), other.endPosition()).toString());
    }
  }
}
