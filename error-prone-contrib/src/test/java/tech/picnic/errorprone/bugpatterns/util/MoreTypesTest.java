package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.raw;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.supOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.unbound;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.Signatures;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MoreTypesTest {
  private static final ImmutableSet<Supplier<Type>> TYPES =
      ImmutableSet.of(
          type("java.lang.Nonexistent"),
          type("java.lang.String"),
          type("java.lang.Number"),
          supOf(type("java.lang.Number")),
          subOf(type("java.lang.Number")),
          type("java.lang.Integer"),
          supOf(type("java.lang.Integer")),
          subOf(type("java.lang.Integer")),
          type("java.util.Optional"),
          raw(type("java.util.Optional")),
          generic(type("java.util.Optional"), unbound()),
          generic(type("java.util.Optional"), type("java.lang.Number")),
          type("java.util.Collection"),
          raw(type("java.util.Collection")),
          generic(type("java.util.Collection"), unbound()),
          generic(type("java.util.Collection"), type("java.lang.Number")),
          generic(type("java.util.Collection"), supOf(type("java.lang.Number"))),
          generic(type("java.util.Collection"), subOf(type("java.lang.Number"))),
          generic(type("java.util.Collection"), type("java.lang.Integer")),
          generic(type("java.util.Collection"), supOf(type("java.lang.Integer"))),
          generic(type("java.util.Collection"), subOf(type("java.lang.Integer"))),
          type("java.util.List"),
          raw(type("java.util.List")),
          generic(type("java.util.List"), unbound()),
          generic(type("java.util.List"), type("java.lang.Number")),
          generic(type("java.util.List"), supOf(type("java.lang.Number"))),
          generic(type("java.util.List"), subOf(type("java.lang.Number"))),
          generic(type("java.util.List"), type("java.lang.Integer")),
          generic(type("java.util.List"), supOf(type("java.lang.Integer"))),
          generic(type("java.util.List"), subOf(type("java.lang.Integer"))),
          generic(
              type("java.util.Map"),
              type("java.lang.String"),
              subOf(generic(type("java.util.Collection"), supOf(type("java.lang.Short"))))));

  @Test
  void matcher() {
    CompilationTestHelper.newInstance(SubtypeFlagger.class, getClass())
        .addSourceLines(
            "/A.java",
            "import java.util.Collection;",
            "import java.util.List;",
            "import java.util.Map;",
            "import java.util.Optional;",
            "import java.util.Set;",
            "",
            "class A<S, T> {",
            "  void m() {",
            "    Object object = factory();",
            "    A a = factory();",
            "",
            "    // BUG: Diagnostic contains: [Number, ? super Number, Integer, ? super Integer]",
            "    int integer = factory();",
            "",
            "    // BUG: Diagnostic contains: [String]",
            "    String string = factory();",
            "",
            "    // BUG: Diagnostic contains: [Optional]",
            "    Optional rawOptional = factory();",
            "    // BUG: Diagnostic contains: [Optional, Optional<?>]",
            "    Optional<S> optionalOfS = factory();",
            "    // BUG: Diagnostic contains: [Optional, Optional<?>]",
            "    Optional<T> optionalOfT = factory();",
            "    // BUG: Diagnostic contains: [Optional, Optional<?>, Optional<Number>]",
            "    Optional<Number> optionalOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Optional, Optional<?>]",
            "    Optional<Integer> optionalOfInteger = factory();",
            "",
            "    // BUG: Diagnostic contains: [Collection]",
            "    Collection rawCollection = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<Number>, Collection<? super",
            "    // Number>, Collection<? extends Number>, Collection<? super Integer>]",
            "    Collection<Number> collectionOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>,",
            "    // Collection<Integer>, Collection<? super Integer>, Collection<? extends Integer>]",
            "    Collection<Integer> collectionOfInteger = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>]",
            "    Collection<Short> collectionOfShort = factory();",
            "",
            "    // BUG: Diagnostic contains: [Collection, List]",
            "    List rawList = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<Number>, Collection<? super",
            "    // Number>, Collection<? extends Number>, Collection<? super Integer>, List, List<?>,",
            "    // List<Number>, List<? super Number>, List<? extends Number>, List<? super Integer>]",
            "    List<Number> listOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>,",
            "    // Collection<Integer>, Collection<? super Integer>, Collection<? extends Integer>, List,",
            "    // List<?>, List<? extends Number>, List<Integer>, List<? super Integer>, List<? extends",
            "    // Integer>]",
            "    List<Integer> listOfInteger = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>, List,",
            "    // List<?>, List<? extends Number>]",
            "    List<Short> listOfShort = factory();",
            "",
            "    // BUG: Diagnostic contains: [Collection]",
            "    Set rawSet = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<Number>, Collection<? super",
            "    // Number>, Collection<? extends Number>, Collection<? super Integer>]",
            "    Set<Number> setOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>,",
            "    // Collection<Integer>, Collection<? super Integer>, Collection<? extends Integer>]",
            "    Set<Integer> setOfInteger = factory();",
            "    // BUG: Diagnostic contains: [Collection, Collection<?>, Collection<? extends Number>]",
            "    Set<Short> setOfShort = factory();",
            "",
            "    Map rawMap = factory();",
            "    Map<Number, Collection<Number>> mapFromNumberToCollectionOfNumber = factory();",
            "    Map<Number, Collection<Short>> mapFromNumberToCollectionOfShort = factory();",
            "    Map<Number, Collection<Integer>> mapFromNumberToCollectionOfInteger = factory();",
            "    // BUG: Diagnostic contains: [Map<String, ? extends Collection<? super Short>>]",
            "    Map<String, Collection<Number>> mapFromStringToCollectionOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Map<String, ? extends Collection<? super Short>>]",
            "    Map<String, Collection<Short>> mapFromStringToCollectionOfShort = factory();",
            "    Map<String, Collection<Integer>> mapFromStringToCollectionOfInteger = factory();",
            "    // BUG: Diagnostic contains: [Map<String, ? extends Collection<? super Short>>]",
            "    Map<String, List<Number>> mapFromStringToListOfNumber = factory();",
            "    // BUG: Diagnostic contains: [Map<String, ? extends Collection<? super Short>>]",
            "    Map<String, List<Short>> mapFromStringToListOfShort = factory();",
            "    Map<String, List<Integer>> mapFromStringToListOfInteger = factory();",
            "  }",
            "",
            "  private <T> T factory() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} which flags method invocations that are a subtype of any type contained in
   * {@link #TYPES}.
   */
  @BugPattern(summary = "Flags invocations of methods with select return types", severity = ERROR)
  public static final class SubtypeFlagger extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      Type treeType = ASTHelpers.getType(tree);

      List<String> matches = new ArrayList<>();

      for (Supplier<Type> type : TYPES) {
        Type testType = type.get(state);
        if (testType != null && state.getTypes().isSubtype(treeType, testType)) {
          matches.add(Signatures.prettyType(testType));
        }
      }

      return matches.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree).setMessage(matches.toString()).build();
    }
  }
}
