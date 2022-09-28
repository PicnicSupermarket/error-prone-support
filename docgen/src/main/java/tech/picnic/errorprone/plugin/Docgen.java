package tech.picnic.errorprone.plugin;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;

/** XXX: Write. */
@AutoService(Plugin.class)
public final class Docgen implements Plugin {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    javacTask.addTaskListener(
        new DocgenTaskListener(((BasicJavacTask) javacTask).getContext(), args[0]));
  }
}
