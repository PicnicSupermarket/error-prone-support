package tech.picnic.errorprone.refaster.util;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.VisitorState;
import com.google.errorprone.dataflow.nullnesspropagation.Nullness;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.NullnessMatcher;
import com.sun.source.tree.ExpressionTree;
import java.util.Optional;
import javax.annotation.Nullable;

/** A matcher of nullable expressions, for use with Refaster's {@code @Matches} annotation. */
public final class IsNonNull implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    NullnessMatcher nullnessMatcher = new NullnessMatcher(Nullness.NONNULL);
    return nullnessMatcher.matches(tree, state);
  }
  ImmutableSet<Optional<String>> testOptionalOfNullableFilterPositive(
          @Nullable String nullableString) {
    return ImmutableSet.of(
            nullableString.length() > 5 ? Optional.of(nullableString) : Optional.empty(),
            !nullableString.contains("baz") ? Optional.of(nullableString) : Optional.empty());
  }

}
