package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import tech.picnic.errorprone.utils.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags {@link Collector Collectors} that don't clearly express
 * (im)mutability.
 *
 * <p>Replacing such collectors with alternatives that produce immutable collections is preferred.
 * Do note that Guava's immutable collections are null-hostile.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid `Collectors.to{List,Map,Set}` in favor of collectors that emphasize (im)mutability",
    link = BUG_PATTERNS_BASE_URL + "CollectorMutability",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class CollectorMutability extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> COLLECTOR_METHOD =
      staticMethod().onClass(Collectors.class.getCanonicalName());
  private static final Matcher<ExpressionTree> LIST_COLLECTOR =
      staticMethod().anyClass().named("toList");
  private static final Matcher<ExpressionTree> MAP_COLLECTOR =
      staticMethod().anyClass().named("toMap");
  private static final Matcher<ExpressionTree> SET_COLLECTOR =
      staticMethod().anyClass().named("toSet");

  /** Instantiates a new {@link CollectorMutability} instance. */
  public CollectorMutability() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)
        || !COLLECTOR_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (LIST_COLLECTOR.matches(tree, state)) {
      return suggestToCollectionAlternatives(
          tree,
          ImmutableList.class.getCanonicalName() + ".toImmutableList",
          ArrayList.class.getCanonicalName(),
          state);
    }

    if (MAP_COLLECTOR.matches(tree, state)) {
      return suggestToMapAlternatives(tree, state);
    }

    if (SET_COLLECTOR.matches(tree, state)) {
      return suggestToCollectionAlternatives(
          tree,
          ImmutableSet.class.getCanonicalName() + ".toImmutableSet",
          HashSet.class.getCanonicalName(),
          state);
    }

    return Description.NO_MATCH;
  }

  private Description suggestToCollectionAlternatives(
      MethodInvocationTree tree,
      String immutableReplacement,
      String mutableReplacement,
      VisitorState state) {
    SuggestedFix.Builder mutableFix = SuggestedFix.builder();
    String toCollectionSelect =
        SuggestedFixes.qualifyStaticImport(
            Collectors.class.getCanonicalName() + ".toCollection", mutableFix, state);
    String mutableCollection = SuggestedFixes.qualifyType(state, mutableFix, mutableReplacement);

    return buildDescription(tree)
        .addFix(replaceMethodInvocation(tree, immutableReplacement, state))
        .addFix(
            mutableFix
                .replace(tree, String.format("%s(%s::new)", toCollectionSelect, mutableCollection))
                .build())
        .build();
  }

  private Description suggestToMapAlternatives(MethodInvocationTree tree, VisitorState state) {
    int argCount = tree.getArguments().size();
    if (argCount > 3) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder mutableFix = SuggestedFix.builder();
    String hashMap =
        SuggestedFixes.qualifyType(state, mutableFix, HashMap.class.getCanonicalName());

    return buildDescription(tree)
        .addFix(
            replaceMethodInvocation(
                tree, ImmutableMap.class.getCanonicalName() + ".toImmutableMap", state))
        .addFix(
            mutableFix
                .postfixWith(
                    tree.getArguments().get(argCount - 1),
                    (argCount == 2 ? ", (a, b) -> { throw new IllegalStateException(); }" : "")
                        + String.format(", %s::new", hashMap))
                .build())
        .build();
  }

  private static SuggestedFix replaceMethodInvocation(
      MethodInvocationTree tree, String fullyQualifiedReplacement, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String replacement = SuggestedFixes.qualifyStaticImport(fullyQualifiedReplacement, fix, state);
    fix.merge(SuggestedFix.replace(tree.getMethodSelect(), replacement));
    return fix.build();
  }
}
