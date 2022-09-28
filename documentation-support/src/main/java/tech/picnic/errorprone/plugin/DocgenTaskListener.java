package tech.picnic.errorprone.plugin;

import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.picnic.errorprone.plugin.DocType.BUG_PATTERN;
import static tech.picnic.errorprone.plugin.DocType.BUG_PATTERN_TEST;
import static tech.picnic.errorprone.plugin.DocType.REFASTER_TEMPLATE_TEST_INPUT;
import static tech.picnic.errorprone.plugin.DocType.REFASTER_TEMPLATE_TEST_OUTPUT;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

/** XXX: Write this. */
final class DocgenTaskListener implements TaskListener {
  private final Context context;

  private final String basePath;

  private final VisitorState state;

  private final ObjectMapper mapper =
      new ObjectMapper()
          .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, /* state= */ false)
          .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, /* state= */ false);

  DocgenTaskListener(Context context, String path) {
    this.context = context;
    this.basePath = path.substring(path.indexOf('=') + 1) + "/docs";
    this.state = VisitorState.createForUtilityPurposes(context);

    // XXX: Move this somewhere else?
    try {
      Files.createDirectories(Paths.get(basePath));
    } catch (IOException e) {
      throw new VerifyException(e);
    }
  }

  @Override
  @SuppressWarnings("SystemOut")
  public void finished(TaskEvent taskEvent) {
    ClassTree tree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    JavaFileObject sourceFile = taskEvent.getSourceFile();
    if (tree == null || sourceFile == null || taskEvent.getKind() != TaskEvent.Kind.ANALYZE) {
      return;
    }

    getDocType(tree, sourceFile, state)
        .ifPresent(
            docType ->
                writeToFile(
                    docType.getDocExtractor().extractData(tree, taskEvent, state),
                    docType.getOutputFileNamePrefix(),
                    getSimpleClassName(sourceFile.getName())));
  }

  private static Optional<DocType> getDocType(
      ClassTree tree, JavaFileObject sourceFile, VisitorState state) {
    if (isBugPattern(tree)) {
      return Optional.of(BUG_PATTERN);
    } else if (isBugPatternTest(tree, state)) {
      return Optional.of(BUG_PATTERN_TEST);
    } else if (sourceFile.getName().contains("TestInput")) {
      return Optional.of(REFASTER_TEMPLATE_TEST_INPUT);
    } else if (sourceFile.getName().contains("TestOutput")) {
      return Optional.of(REFASTER_TEMPLATE_TEST_OUTPUT);
    }
    return Optional.empty();
  }

  private <T> void writeToFile(T data, String fileName, String name) {
    File file = new File(basePath + "/" + fileName + "-" + name + ".json");
    // XXX: Use Path instead of File.

    try (FileWriter fileWriter = new FileWriter(file, UTF_8, /* append= */ true)) {
      mapper.writeValue(fileWriter, data);
      fileWriter.write("\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isBugPattern(ClassTree tree) {
    return ASTHelpers.hasDirectAnnotationWithSimpleName(tree, BugPattern.class.getSimpleName());
  }

  private static boolean isBugPatternTest(ClassTree tree, VisitorState state) {
    String className = tree.getSimpleName().toString();
    if (!className.endsWith("Test")) {
      return false;
    }

    ScanBugPatternTest scanBugPatternTest = new ScanBugPatternTest();
    scanBugPatternTest.scan(tree, state);

    String bugPatternName = className.substring(0, className.lastIndexOf("Test"));
    return scanBugPatternTest.hasTestUsingClassInstance(bugPatternName);
  }

  private static String getSimpleClassName(String path) {
    int index = path.lastIndexOf('/');
    String fileName = path.substring(index + 1);
    return fileName.replace(".java", "");
  }

  private static final class ScanBugPatternTest extends TreeScanner<@Nullable Void, VisitorState> {
    private static final Matcher<ExpressionTree> BUG_PATTERN_TEST_METHOD =
        staticMethod()
            .onDescendantOfAny(
                "com.google.errorprone.CompilationTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("newInstance");

    private final List<String> encounteredClasses = new ArrayList<>();

    boolean hasTestUsingClassInstance(String clazz) {
      return encounteredClasses.contains(clazz);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (BUG_PATTERN_TEST_METHOD.matches(node, state)) {
        MemberSelectTree firstArgumentTree = (MemberSelectTree) node.getArguments().get(0);
        encounteredClasses.add(firstArgumentTree.getExpression().toString());
      }
      return super.visitMethodInvocation(node, state);
    }
  }
}
