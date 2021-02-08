package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.InjectMatchers.IS_APPLICATION_OF_JAVAX_INJECT;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anything;

/**
 * A {@link BugChecker} which flags annotations which aren't sorted lexicographically.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same entry.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotation",
    summary = "Where possible, sort annotations lexicographically",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class LexicographicalAnnotationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
//  https://github.com/google/error-prone/pull/2125/files

  private static final MultiMatcher<MethodTree, AnnotationTree> FINDER =
          annotations(ALL, anything());

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    List<Attribute.Compound> rawAttributes = ASTHelpers.getSymbol(tree).getRawAttributes();

//    Stream<Attribute.Compound> declarationAndTypeAttributes = ASTHelpers.getDeclarationAndTypeAttributes(ASTHelpers.getSymbol(tree));
//    java.util.List<Attribute.Compound> collect1 = declarationAndTypeAttributes.collect(Collectors.toList());
    if (rawAttributes.length() < 2) {
      return Description.NO_MATCH;
    }

//    MultiMatcher.MultiMatchResult<AnnotationTree> annotationTreeMultiMatchResult = FINDER.multiMatchResult(tree, state);
//    if (annotationTreeMultiMatchResult.matches())

    ImmutableList<Attribute.Compound> collect = rawAttributes.stream()
            .sorted(Comparator.comparing(e -> e.type.tsym.getQualifiedName().toString()))
            .collect(toImmutableList());

    boolean annotationsMatch = IntStream.range(0, rawAttributes.size())
            .allMatch(i -> collect.get(i).equals(rawAttributes.get(i)));

    if (annotationsMatch) {
      return Description.NO_MATCH;
    }

    String treeToString = Util.treeToString(tree, state);

     Attribute.Compound value = rawAttributes.get(0).getValue();

    String suggestion = collect.stream()
            .map(annotation -> "@" + annotation.type.tsym.name.toString() + "()")
            .collect(Collectors.joining("\r\n"));
//    String suggestion =
//            collect.stream()
//                        .map(comp -> Util.treeToString(comp, state))
//            //            .collect(joining(", ", "{", "}"));
//            //    return Optional.of(SuggestedFix.builder().replace(array, suggestion));

//    SuggestedFix.builder().replace()

    return Description.NO_MATCH;

  }




  //    SuggestedFix.builder().
//    return describeMatch(collect, SuggestedFix.replace(, ));

}
