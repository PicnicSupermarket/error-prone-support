package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.util.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags types with non-standard member ordering.
 *
 * <p>Type members should be ordered in a standard way, which is: static fields, non-static fields,
 * constructors and methods.
 */
// XXX: Reference
// https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/coding/DeclarationOrderCheck.html
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Type members should be ordered in a standard way",
    link = BUG_PATTERNS_BASE_URL + "TypeMemberOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class TypeMemberOrdering extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  // TODO: Copy should be sorted and comparator in-sync.
  /** Orders {@link Tree}s to match the standard Java type member declaration order. */
  private static final Comparator<Tree> BY_PREFERRED_TYPE_MEMBER_ORDER =
      comparing(
          tree -> {
            switch (tree.getKind()) {
              case VARIABLE:
                return isStatic((VariableTree) tree) ? 1 : 2;
              case METHOD:
                return isConstructor((MethodTree) tree) ? 3 : 4;
              default:
                throw new IllegalStateException("Unexpected kind: " + tree.getKind());
            }
          });

  /** Instantiates a new {@link TypeMemberOrdering} instance. */
  public TypeMemberOrdering() {}

  @Override
  public Description matchClass(ClassTree classTree, VisitorState state) {
    ImmutableList<TypeMemberWithComments> typeMembers =
        getTypeMembersWithComments(classTree, state).stream()
            .filter(typeMember -> shouldBeSorted(typeMember.tree()))
            .collect(toImmutableList());

    ImmutableList<TypeMemberWithComments> sortedTypeMembers =
        ImmutableList.sortedCopyOf(
            comparing(TypeMemberWithComments::tree, BY_PREFERRED_TYPE_MEMBER_ORDER), typeMembers);

    if (typeMembers.equals(sortedTypeMembers)) {
      return Description.NO_MATCH;
    }

    return buildDescription(classTree)
        .addFix(replaceTypeMembers(typeMembers, sortedTypeMembers, state))
        .build();
  }

  private static boolean isStatic(VariableTree variableTree) {
    Set<Modifier> modifiers = variableTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(MethodTree methodTree) {
    return ASTHelpers.getSymbol(methodTree).isConstructor();
  }

  private static boolean shouldBeSorted(Tree tree) {
    return tree instanceof VariableTree
        || (tree instanceof MethodTree && !ASTHelpers.isGeneratedConstructor((MethodTree) tree));
  }

  private static SuggestedFix replaceTypeMembers(
      ImmutableList<TypeMemberWithComments> typeMembers,
      ImmutableList<TypeMemberWithComments> replacementTypeMembers,
      VisitorState state) {
    return Streams.zip(
            typeMembers.stream(),
            replacementTypeMembers.stream(),
            (original, replacement) -> replaceTypeMember(state, original, replacement))
        .reduce(SuggestedFix.builder(), SuggestedFix.Builder::merge, SuggestedFix.Builder::merge)
        .build();
  }

  private static SuggestedFix replaceTypeMember(
      VisitorState state, TypeMemberWithComments original, TypeMemberWithComments replacement) {
    /* Technically this check is not necessary, but it avoids redundant replacements. */
    if (original.equals(replacement)) {
      return SuggestedFix.emptyFix();
    }

    String replacementSource =
        Stream.concat(
                replacement.comments().stream(),
                Stream.of(SourceCode.treeToString(replacement.tree(), state)))
            .collect(joining(System.lineSeparator()));
    return SuggestedFixes.replaceIncludingComments(
        TreePath.getPath(state.getPath(), original.tree()), replacementSource, state);
  }

  /** Returns the type's members with their comments. */
  private static ImmutableList<TypeMemberWithComments> getTypeMembersWithComments(
      ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .map(
            member ->
                new AutoValue_TypeMemberOrdering_TypeMemberWithComments(
                    member, getTypeMemberComments(state, classTree, member)))
        .collect(toImmutableList());
  }

  private static ImmutableList<String> getTypeMemberComments(
      VisitorState state, ClassTree classTree, Tree member) {
    int typeStart = ASTHelpers.getStartPosition(classTree);
    int typeEnd = state.getEndPosition(classTree);
    int memberStart = ASTHelpers.getStartPosition(member);
    int memberEnd = state.getEndPosition(member);
    if (typeStart == Position.NOPOS
        || typeEnd == Position.NOPOS
        || memberStart == Position.NOPOS
        || memberEnd == Position.NOPOS) {
      /* Source code details appear to be unavailable. */
      return ImmutableList.of();
    }

    // TODO: Move identifying "previous member end position" to an outer loop,
    //  Loop once and identify for all members
    // TODO: Check if this handles properly comments on the first member.
    Optional<Integer> previousMemberEndPos =
        state.getOffsetTokens(typeStart, typeEnd).stream()
            .map(ErrorProneToken::endPos)
            .takeWhile(endPos -> endPos < memberStart)
            .reduce((earlierPos, laterPos) -> laterPos);

    List<ErrorProneToken> typeMemberTokens =
        state.getOffsetTokens(previousMemberEndPos.orElse(memberStart), memberEnd);

    // TODO: double check this .get(0)
    return typeMemberTokens.get(0).comments().stream()
        .map(Tokens.Comment::getText)
        .collect(toImmutableList());
  }

  @AutoValue
  abstract static class TypeMemberWithComments {
    abstract Tree tree();

    abstract ImmutableList<String> comments();
  }
}
