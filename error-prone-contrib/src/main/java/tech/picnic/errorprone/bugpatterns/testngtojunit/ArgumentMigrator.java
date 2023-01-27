package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;

// XXX: General feedback not specific to this class.
// Make sure to:
// - Check if everything needs to be `public`. I suspect most things can actually be package
// private.
// That makes it easier for yourself because public things should be documented with Javadoc :).
// - Make sure there is a completely green build, now `mvn clean install` still gives problems.
// - For some classes it would still be nice to have some Javadoc nonetheless.
// - Maybe we should start working on a README where we document a few things.
//   1. What does is and how it works.
//   2. How to use this.
//   3. The script used to perform the migration?
//   4. Maybe we can think of something else? Things not supported?
// - We have kind of "integration tests" for the `TestNGScanner` which indirectly tests the
// `ArgumentMigrator`s but we should probably add tests for the separate `ArgumentMigrator`s as
// well.
public interface ArgumentMigrator {
  Optional<SuggestedFix> createFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      ExpressionTree content,
      VisitorState state);

  boolean canFix(TestNGMigrationContext context, TestNGMetadata.Annotation annotation);
}
