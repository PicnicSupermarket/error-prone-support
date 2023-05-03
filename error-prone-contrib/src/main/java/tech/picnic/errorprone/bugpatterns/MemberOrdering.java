package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.fixes.SuggestedFixes.addSuppressWarnings;
import static com.sun.source.tree.Tree.Kind.METHOD;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import static java.util.Comparator.comparing;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import java.util.Comparator;
import java.util.Set;
import javax.lang.model.element.Modifier;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Members should be ordered in a standard way.",
    link = BUG_PATTERNS_BASE_URL + "MemberOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public class MemberOrdering extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  /** A comparator that sorts members, constructors and methods in a standard order. */
  private static final Comparator<Tree> COMPARATOR =
      comparing(
              (Tree memberTree) -> {
                switch (memberTree.getKind()) {
                  case VARIABLE:
                    return 0;
                  case METHOD:
                    return 1;
                }
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
              })
          .thenComparing(
              (Tree memberTree) -> {
                switch (memberTree.getKind()) {
                  case VARIABLE:
                    return isStatic((JCVariableDecl) memberTree) ? 0 : 1;
                  case METHOD:
                    return isConstructor((JCMethodDecl) memberTree) ? 0 : 1;
                }
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
              });
  // XXX: Evaluate alternative implementation.
  /** A comparator that sorts members, constructors and methods in a standard order. */
  private static final Comparator<Tree> SQUASHED_COMPARATOR =
      Comparator.comparing(
          (Tree memberTree) -> {
            if (memberTree.getKind() == VARIABLE) {
              if (isStatic((JCVariableDecl) memberTree)) {
                // 1. static variables.
                return 1;
              } else {
                // 2. non-static variables.
                return 2;
              }
            }
            if (memberTree.getKind() == METHOD) {
              if (isConstructor((JCMethodDecl) memberTree)) {
                // 3. constructors.
                return 3;
              } else {
                // 4. methods.
                return 4;
              }
            }
            throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
          });

  /** Instantiates a new {@link MemberOrdering} instance. */
  public MemberOrdering() {
    super();
  }

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    var members =
        tree.getMembers().stream().filter(MemberOrdering::isHandled).collect(toImmutableList());
    var sortedMembers = members.stream().sorted(COMPARATOR).collect(toImmutableList());

    if (members.equals(sortedMembers)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(addSuppressWarnings(state, canonicalName()))
        .addFix(swapMembers(state, members, sortedMembers))
        .setMessage(
            "Members, constructors and methods should follow standard ordering. "
                + "The standard ordering is: static variables, non-static variables, "
                + "constructors and methods.")
        .build();
  }

  private static boolean isHandled(Tree tree) {
    return tree instanceof JCVariableDecl
        || (tree instanceof JCMethodDecl
            && !ASTHelpers.isGeneratedConstructor((JCMethodDecl) tree));
  }

  private static SuggestedFix swapMembers(
      VisitorState state,
      ImmutableList<? extends Tree> members,
      ImmutableList<? extends Tree> sortedMembers) {
    var fix = SuggestedFix.builder();
    for (int i = 0; i < members.size(); i++) {
      Tree original = members.get(i);
      Tree correct = sortedMembers.get(i);
      // XXX: Technically not necessary, but avoids redundant replacements.
      if (!original.equals(correct)) {
        fix.replace(original, state.getSourceForNode(correct));
      }
    }
    return fix.build();
  }

  private static boolean isStatic(JCVariableDecl memberTree) {
    Set<Modifier> modifiers = memberTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(JCMethodDecl methodDecl) {
    // XXX: Using state.getName(...) would be better, but than we'd need to introduce `state` as a
    // parameter and that would mean we have to instantiate a COMPARATOR for every matchClass(...)
    // call, which seems excessive for a simple check like this.
    // That may also violate some kind of contract about comparators being stateless.
    return methodDecl.getName().toString().equals("<init>");
  }
}
