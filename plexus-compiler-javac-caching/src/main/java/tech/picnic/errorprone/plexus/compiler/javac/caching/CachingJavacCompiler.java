package tech.picnic.errorprone.plexus.compiler.javac.caching;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.javac.JavaxToolsCompiler;

/** A {@link JavaxToolsCompiler} implementation which... */
// XXX: Extend documentation. Make it reality.
public final class CachingJavacCompiler extends JavaxToolsCompiler {
  @Override
  protected JavaCompiler newJavaCompiler() {
    // XXX: Tweak!
    return ToolProvider.getSystemJavaCompiler();
  }

  // XXX: Drop?
  @Override
  public CompilerResult compileInProcess(
      String[] args, CompilerConfiguration config, String[] sourceFiles) throws CompilerException {
    getLogger().error("XXXX I'm invoked!");
    return super.compileInProcess(args, config, sourceFiles);
  }
}
