package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;

// XXX: The plan towards canonical Refaster templates:
// 1. Replace wildcard types.
// 2. Enforce rule naming conventions.
// 3. Split rules with incompatible parameter types.
// 4. (Un)annotate templates with incompatible result expressions.
//
// Also:
// 0. Canonicalize template method order (@PlaceHolder, @BeforeTemplate, @AfterTemplate).
//    ^ There may be logic by which to break ties; TBD.
// 1. Canonicalize template method parameter order.
//    ^ See https://github.com/PicnicSupermarket/error-prone-support/pull/775.
// 2. Canonicalize template type parameter order following a similar approach.
// 3. Canonicalize template method parameter names by deriving them from the corresponding parameter
//    names of the code invoked in the `@AfterTemplate` method.
// 4. Canonicalize type parameter names following a similar approach.

/**
 * A {@link BugChecker} that flags Refaster expression templates parameters with wildcard types, and
 * suggests simplifying these, possibly by declaring a corresponding class type parameter.
 */
// XXX: Given `? extends T`/`? super T`, suggest just dropping the wildcard if `T` is not referenced
// elsewhere in the same `@BeforeTemplate` signature.
// XXX: The type parameters can also be defined at the method level. Such cases should be moved to
// the class level.
// ^ Relatedly, if not already disallowed, constrain all Refaster method modifiers. Should not be
// `static`, etc.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Refaster template parameter types should not contain wildcards",
    link = BUG_PATTERNS_BASE_URL + "RefasterParameterTypeGenerics",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = {SIMPLIFICATION, STYLE})
public final class RefasterParameterTypeGenerics extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    for (Tree member : tree.getMembers()) {
      if (BEFORE_TEMPLATE_METHOD.matches(member, state)) {
        MethodTree method = (MethodTree) member;
        for (VariableTree parameter : method.getParameters()) {
          Type type = ASTHelpers.getType(parameter);
          if (type != null && type.isParameterized()) {
            return describeMatch(parameter);
          }
        }
      }
    }

    return Description.NO_MATCH;
  }
}
