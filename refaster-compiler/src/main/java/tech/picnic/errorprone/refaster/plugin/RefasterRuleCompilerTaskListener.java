package tech.picnic.errorprone.refaster.plugin;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.refaster.RefasterRuleBuilderScanner;
import com.google.errorprone.refaster.UTemplater;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nullable;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

/**
 * A variant of {@code com.google.errorprone.refaster.RefasterRuleCompilerAnalyzer} that stores
 * compiled Refaster rules in a {@code .refaster} file next to the compiled {@code .class} file,
 * rather than at a fixed location.
 *
 * <p>This {@link TaskListener} thus supports compilation of multiple Refaster rules.
 */
final class RefasterRuleCompilerTaskListener implements TaskListener {
  private final Context context;

  RefasterRuleCompilerTaskListener(Context context) {
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

    ImmutableMap<ClassTree, CodeTransformer> rules = compileRefasterTemplates(tree);
    for (Map.Entry<ClassTree, CodeTransformer> rule : rules.entrySet()) {
      try {
        outputCodeTransformer(rule.getValue(), getOutputFile(taskEvent, rule.getKey()));
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to persist compiled Refaster templates", e);
      }
    }
  }

  private boolean containsRefasterTemplates(ClassTree tree) {
    return Boolean.TRUE.equals(
        new TreeScanner<Boolean, Void>() {
          @Override
          public Boolean visitAnnotation(AnnotationTree node, @Nullable Void v) {
            Symbol sym = ASTHelpers.getSymbol(node);
            return (sym != null
                    && sym.getQualifiedName()
                        .contentEquals(BeforeTemplate.class.getCanonicalName()))
                || super.visitAnnotation(node, v);
          }

          @Override
          public Boolean reduce(Boolean r1, Boolean r2) {
            return Boolean.TRUE.equals(r1) || Boolean.TRUE.equals(r2);
          }
        }.scan(tree, null));
  }

  private ImmutableMap<ClassTree, CodeTransformer> compileRefasterTemplates(ClassTree tree) {
    ImmutableMap.Builder<ClassTree, CodeTransformer> rules = ImmutableMap.builder();
    new TreeScanner<Void, ImmutableClassToInstanceMap<Annotation>>() {
      @Nullable
      @Override
      public Void visitClass(ClassTree node, ImmutableClassToInstanceMap<Annotation> annotations) {
        ImmutableList<CodeTransformer> transformers =
            ImmutableList.copyOf(RefasterRuleBuilderScanner.extractRules(node, context));
        if (!transformers.isEmpty()) {
          rules.put(
              node,
              new AnnotatedCompositeCodeTransformer(
                  String.valueOf(toSimpleFlatName(ASTHelpers.getSymbol(node))),
                  transformers,
                  annotations));
        }

        return super.visitClass(
            node, merge(annotations, UTemplater.annotationMap(ASTHelpers.getSymbol(node))));
      }
    }.scan(tree, ImmutableClassToInstanceMap.of());
    return rules.buildOrThrow();
  }

  // XXX: Move down?
  private static ImmutableClassToInstanceMap<Annotation> merge(
      ImmutableClassToInstanceMap<Annotation> left, ImmutableClassToInstanceMap<Annotation> right) {
    return ImmutableClassToInstanceMap.<Annotation>builder()
        .putAll(Maps.filterKeys(left, k -> !right.containsKey(k)))
        .putAll(right)
        .build();
  }

  private FileObject getOutputFile(TaskEvent taskEvent, ClassTree tree) throws IOException {
    ClassSymbol symbol = ASTHelpers.getSymbol(tree);
    PackageSymbol enclosingPackage = ASTHelpers.enclosingPackage(symbol);
    String packageName = enclosingPackage == null ? "" : enclosingPackage.toString();
    String relativeName = toSimpleFlatName(symbol) + ".refaster";

    JavaFileManager fileManager = context.get(JavaFileManager.class);
    return fileManager.getFileForOutput(
        StandardLocation.CLASS_OUTPUT, packageName, relativeName, taskEvent.getSourceFile());
  }

  private static CharSequence toSimpleFlatName(ClassSymbol classSymbol) {
    Name flatName = classSymbol.flatName();
    int lastDot = flatName.lastIndexOf((byte) '.');
    return lastDot < 0 ? flatName : flatName.subSequence(lastDot + 1, flatName.length());
  }

  private static void outputCodeTransformer(CodeTransformer codeTransformer, FileObject target)
      throws IOException {
    try (ObjectOutput output = new ObjectOutputStream(target.openOutputStream())) {
      output.writeObject(codeTransformer);
    }
  }
}
