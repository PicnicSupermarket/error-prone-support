package tech.picnic.errorprone.refaster;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.DescriptionListener;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import java.io.Serializable;
import java.lang.annotation.Annotation;

// XXX: Move? Rename? Use `@AutoValue`?
// XXX: This is a bit of an implementation detail. Alternative idea: move this to `refaster-runner`,
// and have `refaster-compiler` depend on `refaster-runner`. (Or the other way around?)
public final class AnnotatedCompositeCodeTransformer implements CodeTransformer, Serializable {
  private final ImmutableList<CodeTransformer> transformers;
  private final ImmutableClassToInstanceMap<Annotation> annotations;

  public AnnotatedCompositeCodeTransformer(
      ImmutableList<CodeTransformer> transformers,
      ImmutableClassToInstanceMap<Annotation> annotations) {
    this.transformers = transformers;
    this.annotations = annotations;
  }

  @Override
  public ImmutableClassToInstanceMap<Annotation> annotations() {
    return annotations;
  }

  @Override
  public void apply(TreePath path, Context context, DescriptionListener listener) {
    for (CodeTransformer transformer : transformers) {
      transformer.apply(path, context, listener);
    }
  }
}
