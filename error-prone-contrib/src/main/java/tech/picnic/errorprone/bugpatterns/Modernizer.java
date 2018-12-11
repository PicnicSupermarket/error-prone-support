package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.allOf;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Value;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.IdentifierTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.Target;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;

/**
 * A {@link BugChecker} which flags the same legacy APIs as the <a
 * href="https://github.com/gaul/modernizer-maven-plugin">Modernizer Maven Plugin</a>.
 *
 * <p>This checker is primarily useful for people who run Error Prone anyway; it obviates the need
 * for an additional source code analysis pass using another Maven plugin.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid constants and methods superceded by more recent equivalents",
    linkType = NONE,
    severity = SUGGESTION,
    tags = REFACTORING)
public final class Modernizer extends BugChecker
    implements AnnotationTreeMatcher,
        IdentifierTreeMatcher,
        MemberReferenceTreeMatcher,
        MemberSelectTreeMatcher,
        NewClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  // XXX: Load lazily?
  private final ImmutableTable<String, Matcher<ExpressionTree>, String> violations =
      loadViolations();

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    // XXX: Use or drop
    // XXX: See
    // https://github.com/google/guava-beta-checker/commit/9b26aa980be7f70631921fd6695013547728eb1e;
    // we may be on the right track without this.
    return Description.NO_MATCH;
  }

  @Override
  public Description matchIdentifier(IdentifierTree tree, VisitorState state) {
    return match(tree.getName(), tree, state);
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    return match(tree.getName(), tree, state);
  }

  @Override
  public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
    return match(tree.getIdentifier(), tree, state);
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    return Optional.ofNullable(ASTHelpers.getSymbol(tree))
        .map(Symbol::getEnclosingElement)
        .map(Symbol::getQualifiedName)
        .map(name -> match(name, tree, state))
        .orElse(Description.NO_MATCH);
  }

  private Description match(Name identifier, ExpressionTree tree, VisitorState state) {
    return this.violations.row(identifier.toString()).entrySet().stream()
        .filter(e -> e.getKey().matches(tree, state))
        .findFirst()
        .map(e -> buildDescription(tree).setMessage(e.getValue()).build())
        .orElse(Description.NO_MATCH);
  }

  private ImmutableTable<String, Matcher<ExpressionTree>, String> loadViolations() {
    InputStream resource = getClass().getResourceAsStream("/modernizer.xml");
    // XXX: Or silently skip?
    checkState(resource != null, "Modernizer configuration not found on classpath");

    try (resource) {
      return Violations.loadFromConfig(resource);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to parse Modernizer configuration", e);
    }
  }

  static final class Violations {
    @JacksonXmlElementWrapper(useWrapping = false)
    private final ImmutableList<Violation> violation;

    @JsonCreator
    private Violations(@JsonProperty("violation") List<Violation> violation) {
      this.violation = ImmutableList.copyOf(violation);
    }

    static ImmutableTable<String, Matcher<ExpressionTree>, String> loadFromConfig(
        InputStream config) throws IOException {
      XmlMapper mapper = new XmlMapper();
      mapper.setDefaultVisibility(Value.construct(PropertyAccessor.FIELD, Visibility.ANY));
      return mapper.readValue(config, Violations.class).getViolations();
    }

    private ImmutableTable<String, Matcher<ExpressionTree>, String> getViolations() {
      return violation.stream()
          .filter(v -> v.getChecker().isPresent())
          .collect(
              toImmutableTable(
                  Violation::getIdentifier,
                  v -> v.getChecker().orElseThrow(),
                  Violation::getComment));
    }
  }

  static final class Violation {
    private static final Pattern NAME_PATTERN =
        Pattern.compile(
            "(?<type>[^.]+)(?:\\.(?<member>[^:]+):(?:\\((?<params>[^)]*)\\))?(?<return>[^()]+))?");

    private final Optional<Target> target;
    private final String identifier;
    private final Matcher<ExpressionTree> matcher;
    private final String comment;

    private Violation(
        Optional<Target> target,
        String identifier,
        Matcher<ExpressionTree> matcher,
        String comment) {
      this.target = target;
      this.identifier = identifier;
      this.matcher = matcher;
      this.comment = comment;
    }

    String getIdentifier() {
      return this.identifier;
    }

    Optional<Matcher<ExpressionTree>> getChecker() {
      return target.map(t -> allOf(this.matcher, targetMatcher(t)));
    }

    String getComment() {
      return this.comment;
    }

    // XXX: Modernizer also flags annotation declarations, presumably by type.
    // XXX: `ExpressionTree` is wrong here. Depends on type.
    @JsonCreator
    static Violation create(
        @JsonProperty("version") String version,
        @JsonProperty("name") String signature,
        @JsonProperty("comment") String comment) {
      Optional<Target> target = Optional.ofNullable(Target.lookup(version));

      java.util.regex.Matcher matcher = NAME_PATTERN.matcher(signature);
      checkState(matcher.matches(), "Failed to parse signature '%s'", signature);

      String type =
          replaceSlahes(requireNonNull(matcher.group("type"), "Signature must contain type"));

      String member = matcher.group("member");
      if (member == null) {
        // XXX: Should not implement this interface. Something like:
        // violations.put(type, allOf(isSubtypeOf(type), versionRequirement), this.comment)
        return new Violation(target, type, (t, s) -> false, comment);
      }

      String params = matcher.group("params");
      if (params == null) {
        return new Violation(target, member, isField(type), comment);
      }

      ImmutableList<Supplier<Type>> parameters = parseParams(params);

      if ("\"<init>\"".equals(member)) {
        return new Violation(target, type, isConstructor(type, parameters), comment);
      }

      // XXX: Should we disallow _extension_ of this method?
      return new Violation(target, member, isMethod(type, parameters), comment);
    }

    private static Matcher<ExpressionTree> targetMatcher(Target target) {
      return (tree, state) -> target.compareTo(getTargetVersion(state)) <= 0;
    }

    private static Target getTargetVersion(VisitorState state) {
      return Target.instance(
          Optional.ofNullable(state.context.get(JavacTask.class))
              .filter(BasicJavacTask.class::isInstance)
              .map(BasicJavacTask.class::cast)
              .map(BasicJavacTask::getContext)
              .orElse(state.context));
    }

    private static Matcher<ExpressionTree> isField(String onDescendantOf) {
      return isMember(
          ElementKind::isField, TypePredicates.isDescendantOf(onDescendantOf), ImmutableList.of());
    }

    private static Matcher<ExpressionTree> isConstructor(
        String ofClass, ImmutableList<Supplier<Type>> withParameters) {
      return isMember(
          k -> k == ElementKind.CONSTRUCTOR, TypePredicates.isExactType(ofClass), withParameters);
    }

    private static Matcher<ExpressionTree> isMethod(
        String onDescendantOf, ImmutableList<Supplier<Type>> withParameters) {
      return isMember(
          k -> k == ElementKind.METHOD,
          TypePredicates.isDescendantOf(onDescendantOf),
          withParameters);
    }

    private static Matcher<ExpressionTree> isMember(
        Predicate<ElementKind> ofKind,
        TypePredicate ownedBy,
        ImmutableList<Supplier<Type>> withParameters) {
      return (tree, state) ->
          Optional.ofNullable(ASTHelpers.getSymbol(tree))
              .filter(s -> ofKind.test(s.getKind()))
              .filter(s -> isOwnedBy(s, ownedBy, state))
              .filter(s -> hasSameParameters(s, withParameters, state))
              .isPresent();
    }

    private static boolean isOwnedBy(Symbol symbol, TypePredicate expected, VisitorState state) {
      Symbol owner = symbol.getEnclosingElement();
      return owner != null && expected.apply(owner.asType(), state);
    }

    private static boolean hasSameParameters(
        Symbol method, ImmutableList<Supplier<Type>> expected, VisitorState state) {
      List<Type> actual = method.asType().getParameterTypes();
      if (actual.size() != expected.size()) {
        return false;
      }

      for (int i = 0; i < actual.size(); ++i) {
        if (!ASTHelpers.isSameType(actual.get(i), expected.get(i).get(state), state)) {
          return false;
        }
      }

      return true;
    }

    private static ImmutableList<Supplier<Type>> parseParams(String params) {
      ImmutableList.Builder<Supplier<Type>> types = ImmutableList.builder();

      @Var int index = 0;
      while (index < params.length()) {
        index = parseType(params, index, types::add);
      }

      return types.build();
    }

    private static int parseType(String params, int index, Consumer<Supplier<Type>> sink) {
      switch (params.charAt(index)) {
        case '[':
          return parseArrayType(params, index, sink);
        case 'L':
          return parseTypeReference(params, index, sink);
        default:
          return parsePrimitiveType(params, index, sink);
      }
    }

    private static int parseArrayType(String params, int index, Consumer<Supplier<Type>> sink) {
      int typeIndex = index + 1;
      checkArgument(
          params.length() > typeIndex && params.charAt(index) == '[',
          "Cannot parse array type in parameter string '%s' at index %s",
          params,
          index);

      return parseType(
          params,
          typeIndex,
          type ->
              sink.accept(s -> s.getType(type.get(s), /* isArray= */ true, ImmutableList.of())));
    }

    private static int parsePrimitiveType(String params, int index, Consumer<Supplier<Type>> sink) {
      String primitive =
          Optional.of(params)
              .filter(p -> p.length() > index)
              .flatMap(p -> fromPrimitiveAlias(p.charAt(index)))
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          String.format(
                              "Cannot parse primitive type in parameter string '%s' at index %s",
                              params, index)));
      sink.accept(s -> s.getTypeFromString(primitive));
      return index + 1;
    }

    private static Optional<String> fromPrimitiveAlias(char alias) {
      switch (alias) {
        case 'Z':
          return Optional.of("boolean");
        case 'B':
          return Optional.of("byte");
        case 'C':
          return Optional.of("char");
        case 'S':
          return Optional.of("short");
        case 'I':
          return Optional.of("int");
        case 'J':
          return Optional.of("long");
        case 'F':
          return Optional.of("float");
        case 'D':
          return Optional.of("double");
        default:
          return Optional.empty();
      }
    }

    private static int parseTypeReference(String params, int index, Consumer<Supplier<Type>> sink) {
      int identifierIndex = index + 1;
      if (params.length() > identifierIndex && params.charAt(index) == 'L') {
        int delimiter = params.indexOf(';', identifierIndex);
        if (delimiter > index) {
          sink.accept(
              s ->
                  s.getTypeFromString(replaceSlahes(params.substring(identifierIndex, delimiter))));
          return delimiter + 1;
        }
      }

      throw new IllegalArgumentException(
          String.format(
              "Cannot parse reference type in parameter string '%s' at index %s", params, index));
    }

    private static String replaceSlahes(String typeName) {
      return typeName.replace('/', '.');
    }
  }
}
