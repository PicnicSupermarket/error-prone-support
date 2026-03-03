package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags enumerations of permitted subtypes that are not lexicographically
 * sorted.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Sort permitted subtypes lexicographically where possible",
    link = BUG_PATTERNS_BASE_URL + "LexicographicalPermitsListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class LexicographicalPermitsListing extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link LexicographicalPermitsListing} instance. */
  public LexicographicalPermitsListing() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    SuggestedFix fix =
        SourceCode.sortTrees(
            tree.getPermitsClause(),
            comparing(annotation -> SourceCode.treeToString(annotation, state)),
            state);
    return fix.isEmpty()
        ? Description.NO_MATCH
        : describeMatch(tree.getPermitsClause().getFirst(), fix);
  }
}
