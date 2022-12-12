package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

@BugPattern(linkType = CUSTOM, summary = "Apply naming algorithm", severity = ERROR)
public final class RefasterRuleNaming extends BugChecker implements ClassTreeMatcher {
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> AFTER_TEMPLATE_METHOD = hasAnnotation(AfterTemplate.class);

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!hasMatchingMember(tree, BEFORE_TEMPLATE_METHOD, state)) {
      /* This class does not contain a Refaster template. */
      return Description.NO_MATCH;
    }

    ImmutableList<MethodTree> collect =
        tree.getMembers().stream()
            .filter(member -> AFTER_TEMPLATE_METHOD.matches(member, state))
            .filter(MethodTree.class::isInstance)
            .map(MethodTree.class::cast)
            .collect(toImmutableList());

    if (collect.size() > 1) {
      return Description.NO_MATCH;
    }

    // XXX: Check if there is nicer way to get the only element from the list of members.
    MethodTree afterTemplate = Iterables.getOnlyElement(collect);
    String canonicalName = deduceCanonicalRefasterRuleName(afterTemplate);
    return tree.getSimpleName().contentEquals(canonicalName)
        ? Description.NO_MATCH
        : buildDescription(tree)
            .setMessage("Refaster rule should be named: " + canonicalName)
            .build();
  }

  private static String deduceCanonicalRefasterRuleName(MethodTree tree) {
    System.out.println("Tree: " + state.getSourceForNode(tree));
    // XXX: Get the first After template.
    // XXX: Otherwise get the first beforetemplate and use that as import.
    // XXX: In that case, prefix with `Flag`.
    // XXX: Use the expression:
    //  1. Get the objects on which a method is invoked.
    //  2. Check if there are many overloads, if so specify the extra name.
    //  3. Look at what else is after that and repeat.
    return "something";
  }

  // XXX: Copied over from RuleModifiers.
  private static boolean hasMatchingMember(
      ClassTree tree, Matcher<Tree> matcher, VisitorState state) {
    return tree.getMembers().stream().anyMatch(member -> matcher.matches(member, state));
  }
}
