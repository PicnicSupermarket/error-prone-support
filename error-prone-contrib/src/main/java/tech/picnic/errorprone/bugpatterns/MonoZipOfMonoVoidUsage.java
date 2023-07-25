package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags usages of {@link Mono#zip(Mono, Mono)}} and {@link
 * Mono#zipWith(Mono)}} with {@link Mono#empty()} parameters.
 *
 * <p>{@link Mono#zip(Mono, Mono)} and {@link Mono#zipWith(Mono)} perform incorrectly upon retrieval
 * of the empty publisher and prematurely terminates the reactive chain from the execution. In most
 * cases this is not the desired behaviour and {@link Mono#concatWith(Publisher)} or {@link
 * Mono#then(Mono)} should be preferred, as it produces consistent results and performs in the
 * predictable manner.
 *
 * <p>NB: Mono&lt;?>#zipWith(Mono&lt;Void>) is allowed be the Reactor API, but it is an incorrect
 * usage of the API. It will be replaced with Mono&lt;?>#concatWith(Mono&lt;Void>) but it will lead
 * to the compilation errors, indicating that the logic under the hood should be revisited and
 * fixed.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`Mono#zip` and `Mono#zipWith` should not be executed against `Mono#empty` parameter; "
            + "please use `Mono#then` or `Mono#concatWith` instead",
    link = BUG_PATTERNS_BASE_URL + "MonoZipOfMonoVoidUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MonoZipOfMonoVoidUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String MONO = "reactor.core.publisher.Mono";
  private static final Supplier<Type> MONO_VOID_TYPE =
      VisitorState.memoize(generic(type(MONO), type("java.lang.Void")));

  // On Mono.zip, at least one element should match empty in order to proceed.
  private static final Matcher<ExpressionTree> MONO_ZIP_AND_WITH =
      allOf(
          anyMethod().onDescendantOf(MONO).namedAnyOf("zip", "zipWith"),
          toType(MethodInvocationTree.class, hasArgumentOfType(MONO_VOID_TYPE)));

  // On Mono.zip, at least one element should match empty in order to proceed.
  private static final Matcher<ExpressionTree> STATIC_MONO_ZIP =
      allOf(
          staticMethod().onClass(MONO).named("zip"),
          toType(MethodInvocationTree.class, hasArgumentOfType(MONO_VOID_TYPE)));

  /** Instantiates a new {@link MonoZipOfMonoVoidUsage} instance. */
  public MonoZipOfMonoVoidUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_ZIP_AND_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    if (STATIC_MONO_ZIP.matches(tree, state)) {
      ImmutableList<? extends ExpressionTree> arguments = ImmutableList.copyOf(tree.getArguments());

      String replacement =
          Streams.concat(
                  Stream.of(
                      arguments.stream().findFirst().map(ExpressionTree::toString).orElseThrow()),
                  arguments.stream()
                      .skip(1)
                      .map(ExpressionTree::toString)
                      .map(arg -> String.format("(%s)", arg)))
              .collect(joining(".then"));
      SuggestedFix staticZipUsage = SuggestedFix.replace(tree, replacement);
      description.addFix(staticZipUsage);
    } else {
      SuggestedFix instanceZipUsage =
          SuggestedFixes.renameMethodInvocation(tree, "concatWith", state);
      description.addFix(instanceZipUsage);
    }

    return description.build();
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!MONO_ZIP_AND_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // Method references are expected to occur very infrequently; generating both variants of
    // suggested fixes is not worth the trouble.
    return describeMatch(tree);
  }

  private static Matcher<MethodInvocationTree> hasArgumentOfType(Supplier<Type> type) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(arg -> ASTHelpers.isSubtype(ASTHelpers.getType(arg), type.get(state), state));
  }
}
