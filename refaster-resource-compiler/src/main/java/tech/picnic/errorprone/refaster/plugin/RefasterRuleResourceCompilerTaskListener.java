package tech.picnic.errorprone.refaster.plugin;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.refaster.RefasterRuleBuilderScanner;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

/**
 * A variant of {@code com.google.errorprone.refaster.RefasterRuleCompilerAnalyzer} which stores
 * compiled Refaster rules in a {@code .refaster} file next to the compiled {@code .class} file,
 * rather than at a fixed location.
 *
 * <p>This {@link TaskListener} thus supports compulation of multiple Refaster rules.
 */
final class RefasterRuleResourceCompilerTaskListener implements TaskListener {
  private final Context context;

  RefasterRuleResourceCompilerTaskListener(Context context) {
    this.context = context;
  }

  @Override
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE || JavaCompiler.instance(context).errorCount() > 0) {
      return;
    }

    ClassTree tree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    if (tree == null || !containsRefasterTemplates(tree)) {
      return;
    }

    ImmutableList<CodeTransformer> rules = compileRefasterTemplates(tree);
    if (rules.isEmpty()) {
      return;
    }

    try {
      outputCodeTransformers(rules, getOutputFile(taskEvent, tree));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private boolean containsRefasterTemplates(ClassTree tree) {
    return Boolean.TRUE.equals(
        new TreeScanner<Boolean, Void>() {
          @Override
          public Boolean visitAnnotation(AnnotationTree node, Void ctx) {
            Symbol sym = ASTHelpers.getSymbol(node);
            return (sym != null
                    && sym.getQualifiedName().contentEquals(AfterTemplate.class.getCanonicalName()))
                || super.visitAnnotation(node, ctx);
          }

          @Override
          public Boolean reduce(Boolean r1, Boolean r2) {
            return Boolean.TRUE.equals(r1) || Boolean.TRUE.equals(r2);
          }
        }.scan(tree, null));
  }

  private ImmutableList<CodeTransformer> compileRefasterTemplates(ClassTree tree) {
    List<CodeTransformer> rules = new ArrayList<>();
    new TreeScanner<Void, Context>() {
      @Override
      public Void visitClass(ClassTree node, Context ctx) {
        rules.addAll(RefasterRuleBuilderScanner.extractRules(node, ctx));
        return super.visitClass(node, ctx);
      }
    }.scan(tree, context);
    return ImmutableList.copyOf(rules);
  }

  private FileObject getOutputFile(TaskEvent taskEvent, ClassTree tree) throws IOException {
    String packageName =
        Optional.ofNullable(ASTHelpers.getSymbol(tree))
            .map(ASTHelpers::enclosingPackage)
            .map(PackageSymbol::toString)
            .orElse("");
    String relativeName = tree.getSimpleName() + ".refaster";

    JavaFileManager fileManager = context.get(JavaFileManager.class);
    return fileManager.getFileForOutput(
        StandardLocation.CLASS_OUTPUT, packageName, relativeName, taskEvent.getSourceFile());
  }

  private static void outputCodeTransformers(
      ImmutableList<CodeTransformer> rules, FileObject target) throws IOException {
    try (ObjectOutputStream output = new ObjectOutputStream(target.openOutputStream())) {
      output.writeObject(CompositeCodeTransformer.compose(rules));
    }
  }
}
