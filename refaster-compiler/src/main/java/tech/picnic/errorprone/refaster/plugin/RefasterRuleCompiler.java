package tech.picnic.errorprone.refaster.plugin;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;

/**
 * A variant of {@code com.google.errorprone.refaster.RefasterRuleCompiler} which outputs a {@code
 * fully/qualified/Class.refaster} file for each compiled {@code fully.qualified.Class} that
 * contains a Refaster template.
 */
@AutoService(Plugin.class)
public final class RefasterRuleCompiler implements Plugin {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    javacTask.addTaskListener(
        new RefasterRuleCompilerTaskListener(((BasicJavacTask) javacTask).getContext()));
  }
}
