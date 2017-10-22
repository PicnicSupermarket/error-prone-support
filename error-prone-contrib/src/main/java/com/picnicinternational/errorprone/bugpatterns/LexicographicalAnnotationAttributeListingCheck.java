package com.picnicinternational.errorprone.bugpatterns;

import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;

// XXX: Add documentation. Explain that sorting reduces changes of conflicts and simplifies their resolution when they do happen.
// XXX: Add support for inclusions and exclusions.
// XXX: Disable until implemented.
//@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotationAttributeListing",
    summary = "Where possible, sort annotation array attributes lexicographically",
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE
)
public final class LexicographicalAnnotationAttributeListingCheck extends BugChecker
        implements AnnotationTreeMatcher {
    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
        return null;
    }
}
