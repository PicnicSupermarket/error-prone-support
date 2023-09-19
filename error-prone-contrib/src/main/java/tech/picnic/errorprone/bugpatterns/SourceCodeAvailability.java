package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that files for which no accurate source code appears to be available. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "This type's source code is unavailable or inaccurate",
    link = BUG_PATTERNS_BASE_URL + "SourceCodeAvailability",
    linkType = CUSTOM,
    severity = SUGGESTION)
public final class SourceCodeAvailability extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link SourceCodeAvailability} instance. */
  public SourceCodeAvailability() {}

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    Deque<Boolean> seenAccurateSource = new ArrayDeque<>();
    Set<Tree> maximalAccurateSubtrees = new LinkedHashSet<>();
    new TreeScanner<@Nullable Void, TreePath>() {
      @Override
      public @Nullable Void scan(Tree node, TreePath treePath) {
        if (node == null) {
          return null;
        }

        TreePath path = new TreePath(treePath, node);
        boolean isAccurate = SourceCode.isAccurateSourceLikelyAvailable(state.withPath(path));
        if (!isAccurate) {
          verify(!Boolean.TRUE.equals(seenAccurateSource.peek()));
        } else if (!Boolean.TRUE.equals(seenAccurateSource.peek())) {
          maximalAccurateSubtrees.add(node);
        }

        seenAccurateSource.push(isAccurate);
        try {
          return super.scan(node, path);
        } finally {
          seenAccurateSource.pop();
        }
      }
    }.scan(tree, state.getPath());

    if (maximalAccurateSubtrees.equals(ImmutableSet.of(tree))) {
      return Description.NO_MATCH;
    }

    String accurateSubtrees =
        maximalAccurateSubtrees.stream()
            .map(
                t ->
                    String.format(
                        "%s tree at position %s:\n%s",
                        t.getKind(), ASTHelpers.getStartPosition(t), state.getSourceForNode(t)))
            .collect(joining("\n\n", "Maximally accurate subtrees found:\n\n", ""));

    return buildDescription(tree).setMessage(accurateSubtrees).build();
  }
}
