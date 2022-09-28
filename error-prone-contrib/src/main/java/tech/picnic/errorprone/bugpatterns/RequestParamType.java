package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BASE_URL_BUGPATTERNS;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.VariableTree;

/** A {@link BugChecker} that flags {@code @RequestParam} parameters with an unsupported type. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "`@RequestParam` does not support `ImmutableCollection` and `ImmutableMap` subtypes",
    link = BASE_URL_BUGPATTERNS + "RequestParamType",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class RequestParamType extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<VariableTree> HAS_UNSUPPORTED_REQUEST_PARAM =
      allOf(
          annotations(AT_LEAST_ONE, isType("org.springframework.web.bind.annotation.RequestParam")),
          anyOf(isSubtypeOf(ImmutableCollection.class), isSubtypeOf(ImmutableMap.class)));

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    return HAS_UNSUPPORTED_REQUEST_PARAM.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }
}
