package tech.picnic.errorprone.refaster.matchers;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.matchers.FieldMatchers.staticField;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.isPrimitiveType;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A matcher of expressions that are guaranteed to yield "empty" instances, as defined by their
 * respective types.
 */
// XXX: Also match (effectively) final variables that reference provably-empty objects.
// XXX: Also handle `#copyOf(someEmptyInstance)`, `#sortedCopyOf(someEmptyInstance)`,
// `Sets.immutableEnumSet(emptyIterable)` (and other `Sets` methods), `EnumSet.noneOf(...)`,
// `emptyCollection.stream()`, `emptyMap.{keySet,values,entrySet}()`, etc.
// XXX: Also recognize null-hostile "container" expression types that can only reference empty
// instances, such as `ImmutableCollection<Void>` and `Flux<Void>`.
public final class IsEmpty implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Pattern EMPTY_INSTANCE_FACTORY_METHOD_PATTERN = Pattern.compile("empty.*");
  private static final Matcher<Tree> PRIMITIVE_TYPE = isPrimitiveType();
  // XXX: Extend this list to include additional JDK collection types with a public constructor.
  private static final Matcher<ExpressionTree> MUTABLE_COLLECTION_TYPE =
      anyOf(
          isSameType(ArrayList.class),
          isSameType(HashMap.class),
          isSameType(LinkedHashMap.class),
          isSameType(LinkedList.class),
          isSameType(Stack.class),
          isSameType(Vector.class));
  private static final Matcher<ExpressionTree> EMPTY_INSTANCE_FACTORY =
      anyOf(
          staticField(Collections.class.getName(), "EMPTY_LIST"),
          staticField(Collections.class.getName(), "EMPTY_MAP"),
          staticField(Collections.class.getName(), "EMPTY_SET"),
          toType(
              MethodInvocationTree.class,
              allOf(
                  argumentCount(0),
                  anyOf(
                      staticMethod()
                          .onClass(Collections.class.getName())
                          .withNameMatching(EMPTY_INSTANCE_FACTORY_METHOD_PATTERN),
                      staticMethod()
                          .onDescendantOfAny(
                              ImmutableCollection.class.getName(),
                              ImmutableMap.class.getName(),
                              ImmutableMultimap.class.getName(),
                              List.class.getName(),
                              Map.class.getName(),
                              Set.class.getName(),
                              Stream.class.getName())
                          .named("of"),
                      staticMethod()
                          .onClassAny(
                              Stream.class.getName(),
                              "reactor.core.publisher.Flux",
                              "reactor.core.publisher.Mono",
                              "reactor.util.context.Context")
                          .named("empty"),
                      staticMethod()
                          .onDescendantOf("reactor.core.publisher.Flux")
                          .named("just")))));

  /** Instantiates a new {@link IsEmpty} instance. */
  public IsEmpty() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return isEmptyArrayCreation(tree)
        || EMPTY_INSTANCE_FACTORY.matches(tree, state)
        || isEmptyCollectionConstructor(tree, state);
  }

  private boolean isEmptyCollectionConstructor(ExpressionTree tree, VisitorState state) {
    if (!(tree instanceof NewClassTree) || !MUTABLE_COLLECTION_TYPE.matches(tree, state)) {
      return false;
    }

    List<? extends ExpressionTree> arguments = ((NewClassTree) tree).getArguments();
    if (arguments.stream().allMatch(a -> PRIMITIVE_TYPE.matches(a, state))) {
      /*
       * This is a default constructor, or a constructor that creates an empty collection using
       * custom (re)size/load factor parameters.
       */
      return true;
    }

    /*
     * This looks like a copy constructor, in which case the resultant collection is empty if its
     * argument is.
     */
    verify(arguments.size() == 1, "Unexpected %s-ary constructor", arguments.size());
    return matches(arguments.get(0), state);
  }

  private static boolean isEmptyArrayCreation(ExpressionTree tree) {
    if (!(tree instanceof NewArrayTree)) {
      return false;
    }

    NewArrayTree newArray = (NewArrayTree) tree;
    return (!newArray.getDimensions().isEmpty()
            && ASTHelpers.constValue(newArray.getDimensions().get(0), Integer.class) == 0)
        || (newArray.getInitializers() != null && newArray.getInitializers().isEmpty());
  }
}
