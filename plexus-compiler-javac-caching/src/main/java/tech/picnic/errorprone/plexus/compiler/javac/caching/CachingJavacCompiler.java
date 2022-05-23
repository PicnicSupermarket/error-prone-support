package tech.picnic.errorprone.plexus.compiler.javac.caching;

import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
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
    // XXX: Could we instead simply provide another `JavaCompiler` to be service-loaded? Would
    // certainly be simpler.
    //    return ToolProvider.getSystemJavaCompiler();
    return new XxxJavaCompiler(JavacTool.create());
  }

  // XXX: Drop?
  @Override
  public CompilerResult compileInProcess(
      String[] args, CompilerConfiguration config, String[] sourceFiles) throws CompilerException {
    getLogger().error("XXXX I'm invoked!");
    return super.compileInProcess(args, config, sourceFiles);
  }

  // XXX: Name!
  private static final class XxxJavaCompiler implements JavaCompiler {
    private final ConcurrentHashMap<?, ?> cache = new ConcurrentHashMap<>();
    private final JavacTool delegate;

    private XxxJavaCompiler(JavacTool delegate) {
      this.delegate = delegate;
    }

    @Override
    public CompilationTask getTask(
        Writer out,
        JavaFileManager fileManager,
        DiagnosticListener<? super JavaFileObject> diagnosticListener,
        Iterable<String> options,
        Iterable<String> classes,
        Iterable<? extends JavaFileObject> compilationUnits) {
      System.out.println("XXXX I'm invoked!");
      Context context = new Context();
      // XXX: Explain.
      context.put(ConcurrentMap.class, cache);
      return delegate.getTask(
          out, fileManager, diagnosticListener, options, classes, compilationUnits, context);
    }

    @Override
    public StandardJavaFileManager getStandardFileManager(
        DiagnosticListener<? super JavaFileObject> diagnosticListener,
        Locale locale,
        Charset charset) {
      return delegate.getStandardFileManager(diagnosticListener, locale, charset);
    }

    @Override
    public int isSupportedOption(String option) {
      return delegate.isSupportedOption(option);
    }

    @Override
    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
      return delegate.run(in, out, err, arguments);
    }

    @Override
    public Set<SourceVersion> getSourceVersions() {
      return delegate.getSourceVersions();
    }
  }
}
