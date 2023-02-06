package tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.argument;

import static com.google.common.base.Preconditions.checkState;
import static com.sun.source.tree.Tree.Kind.NEW_ARRAY;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.parser.Tokens;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.testng.annotations.Test;
import tech.picnic.errorprone.bugpatterns.testngtojunit.ArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.Migrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMigrationContext;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMigrationContext.MigrationState;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** An {@link Migrator} that migrates the {@link Test#dataProvider()} argument. */
@Immutable
public class DataProviderArgumentMigrator implements ArgumentMigrator {

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return Optional.empty();
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return metadata.getDataProviderMetadata().containsKey();
  }

  private static String getDataProviderName(ExpressionTree dataValue, VisitorState state) {
    return SourceCode.treeToString(dataValue, state);
  }
}
