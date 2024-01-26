package tech.picnic.errorprone.utils;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.raw;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.superOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class MoreTypesTest {
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
   * A {@link BugChecker} that flags method invocations that are a subtype of any type defined by
   * {@link #getTestTypes()}.
   */
  @BugPattern(summary = "Flags invocations of methods with select return types", severity = ERROR)
  public static final class SubtypeFlagger extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      Type treeType = ASTHelpers.getType(tree);

      List<String> matches = new ArrayList<>();

      for (Supplier<Type> type : getTestTypes()) {
        Type testType = type.get(state);
        if (testType != null && state.getTypes().isSubtype(treeType, testType)) {
          matches.add(Signatures.prettyType(testType));
        }
      }

      return matches.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree).setMessage(matches.toString()).build();
    }

    /**
     * Returns the type suppliers under test.
     *
     * @implNote The return value of this method should not be assigned to a field, as that would
     *     prevent mutations introduced by Pitest from being killed.
     */
    private static ImmutableSet<Supplier<Type>> getTestTypes() {
      return ImmutableSet.of(
          // Invalid types.
          type("java.lang.Nonexistent"),
          generic(type("java.util.Nonexistent"), unbound()),
          // Valid types.
          type(String.class.getCanonicalName()),
          type(Number.class.getCanonicalName()),
          superOf(type(Number.class.getCanonicalName())),
          subOf(type(Number.class.getCanonicalName())),
          type(Integer.class.getCanonicalName()),
          superOf(type(Integer.class.getCanonicalName())),
          subOf(type(Integer.class.getCanonicalName())),
          type(Optional.class.getCanonicalName()),
          raw(type(Optional.class.getCanonicalName())),
          generic(type(Optional.class.getCanonicalName()), unbound()),
          generic(type(Optional.class.getCanonicalName()), type(Number.class.getCanonicalName())),
          type(Collection.class.getCanonicalName()),
          raw(type(Collection.class.getCanonicalName())),
          generic(type(Collection.class.getCanonicalName()), unbound()),
          generic(type(Collection.class.getCanonicalName()), type(Number.class.getCanonicalName())),
          generic(
              type(Collection.class.getCanonicalName()),
              superOf(type(Number.class.getCanonicalName()))),
          generic(
              type(Collection.class.getCanonicalName()),
              subOf(type(Number.class.getCanonicalName()))),
          generic(
              type(Collection.class.getCanonicalName()), type(Integer.class.getCanonicalName())),
          generic(
              type(Collection.class.getCanonicalName()),
              superOf(type(Integer.class.getCanonicalName()))),
          generic(
              type(Collection.class.getCanonicalName()),
              subOf(type(Integer.class.getCanonicalName()))),
          type(List.class.getCanonicalName()),
          raw(type(List.class.getCanonicalName())),
          generic(type(List.class.getCanonicalName()), unbound()),
          generic(type(List.class.getCanonicalName()), type(Number.class.getCanonicalName())),
          generic(
              type(List.class.getCanonicalName()), superOf(type(Number.class.getCanonicalName()))),
          generic(
              type(List.class.getCanonicalName()), subOf(type(Number.class.getCanonicalName()))),
          generic(type(List.class.getCanonicalName()), type(Integer.class.getCanonicalName())),
          generic(
              type(List.class.getCanonicalName()), superOf(type(Integer.class.getCanonicalName()))),
          generic(
              type(List.class.getCanonicalName()), subOf(type(Integer.class.getCanonicalName()))),
          generic(
              type(Map.class.getCanonicalName()),
              type(String.class.getCanonicalName()),
              subOf(
                  generic(
                      type(Collection.class.getCanonicalName()),
                      superOf(type(Short.class.getCanonicalName()))))));
    }
  }
}
