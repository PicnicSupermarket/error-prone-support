package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkState;
import static tech.picnic.errorprone.documentation.DocumentationProcessor.OUTPUT_DIRECTORY_OPTION;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.jspecify.annotations.Nullable;

/** An annotation processor that extracts documentation from source files and writes it to disk. */
@AutoService(Processor.class)
// XXX: This annotation processor approach won't work if any `Extractor` operations on unannotated
// classes. This is currently true at least for bug checker unit tests.
// @SupportedAnnotationTypes({"com.google.errorprone.BugPattern"})
@SupportedOptions(OUTPUT_DIRECTORY_OPTION)
public final class DocumentationProcessor extends AbstractProcessor {
  static final String OUTPUT_DIRECTORY_OPTION = "documentationOutputDirectory";

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final ImmutableList<Extractor<?>> EXTRACTORS =
      (ImmutableList)
          ImmutableList.copyOf(
              ServiceLoader.load(
                  Extractor.class, DocumentationGeneratorTaskListener.class.getClassLoader()));

  @Nullable private Path docsPath;

  /** Instantiates a new {@link DocumentationProcessor} instance. */
  public DocumentationProcessor() {}

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    String outputDir = processingEnv.getOptions().get(OUTPUT_DIRECTORY_OPTION);
    checkState(
        outputDir != null,
        "Output directory not specified. Use -A%s=<path>",
        OUTPUT_DIRECTORY_OPTION);
    docsPath = Path.of(outputDir);

    try {
      Files.createDirectories(docsPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", docsPath), e);
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    return false;
  }
}
