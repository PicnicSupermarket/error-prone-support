package tech.picnic.errorprone.openai;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static org.fusesource.jansi.Ansi.ansi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import javax.annotation.concurrent.NotThreadSafe;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.Parser;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Parameters;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Review whether to enable a *subset* of JLine's built-ins. See
// https://github.com/remkop/picocli/tree/main/picocli-shell-jline3#jline-316-and-picocli-44-example.
// XXX: Consider utilizing `less` for paging. See https://github.com/jline/jline3/wiki/Nano-and-Less-Customization.
public final class Cli {
  // XXX: Drop the `IOException` and properly handle it.
  public static void main(String... args) throws IOException {
    // XXX: Allow the path to be specified.
    IssueExtractor<Path> issueExtractor =
        new PathResolvingIssueExtractor(
            new PathFinder(FileSystems.getDefault(), Path.of("")),
            new SelectFirstIssueExtractor<>(
                ImmutableSet.of(
                    new MavenCheckstyleIssueExtractor(), new PlexusCompilerIssueExtractor())));

    // XXX: Force a file or command to be passed. Can be stdin in non-interactive mode.
    ImmutableSet<Issue<Path>> issues =
        LogLineExtractor.mavenErrorAndWarningExtractor()
            .extract(new FileInputStream("/tmp/g"))
            .stream()
            .flatMap(issueExtractor::extract)
            .collect(toImmutableSet());
    ;

    // new Issue<>(Path.of("xxx"), OptionalInt.of(1), OptionalInt.empty(), "msg"),
    // new Issue<>(Path.of("yyy"), OptionalInt.of(2), OptionalInt.empty(), "msg"),
    // new Issue<>(Path.of("xxx"), OptionalInt.of(3), OptionalInt.empty(), "msg")

    AnsiConsole.systemInstall();
    try (Terminal terminal = TerminalBuilder.terminal()) {
      IssueResolutionController issueResolutionController =
          new IssueResolutionController(terminal.writer(), issues);

      PicocliCommandsFactory factory = new PicocliCommandsFactory();
      factory.setTerminal(terminal);

      CommandLine commandLine = new CommandLine(issueResolutionController, factory);
      PicocliCommands commands = new PicocliCommands(commandLine);

      Parser parser = new DefaultParser();

      SystemRegistry systemRegistry =
          new SystemRegistryImpl(parser, terminal, () -> Path.of("").toAbsolutePath(), null);
      systemRegistry.setCommandRegistries(commands);
      systemRegistry.register("help", commands);

      LineReader reader =
          LineReaderBuilder.builder()
              .terminal(terminal)
              .completer(systemRegistry.completer())
              .parser(parser)
              .build();
      new TailTipWidgets(
              reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER)
          .enable();
      reader.getKeyMaps().get("main").bind(new Reference("tailtip-toggle"), KeyMap.ctrl('t'));

      issueResolutionController.issues();
      while (true) {
        try {
          systemRegistry.cleanUp();
          systemRegistry.execute(
              reader.readLine(
                  issueResolutionController.prompt(), null, (MaskingCallback) null, null));
        } catch (UserInterruptException e) {
          /* User pressed Ctrl+C. */
        } catch (EndOfFileException e) {
          /* User pressed Ctrl+D. */
          return;
        } catch (Exception e) {
          systemRegistry.trace(e);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create terminal", e);
    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  // XXX: Should we even support those `files.isEmpty()` cases?
  // XXX: Allow issues to be dropped?
  // XXX: Mark files modified/track modification count?
  // XXX: List full diff over multiple rounds?
  // XXX: Allow submission of a custom instruction.
  @Command(name = "")
  @NotThreadSafe
  static final class IssueResolutionController {
    private final PrintWriter out;
    private final ImmutableList<FileIssues> files;
    private int currentIndex = 0;

    IssueResolutionController(PrintWriter output, ImmutableSet<Issue<Path>> issues) {
      this.out = output;
      this.files =
          issues.stream()
              .collect(
                  collectingAndThen(
                      groupingBy(Issue::file),
                      m ->
                          m.entrySet().stream()
                              .map(
                                  e ->
                                      new FileIssues(
                                          e.getKey(), ImmutableList.copyOf(e.getValue())))
                              .collect(toImmutableList())));
    }

    @Command(aliases = "i", subcommands = HelpCommand.class, description = "List issues.")
    void issues() {
      if (files.isEmpty()) {
        out.println(ansi().fgRed().a("No issues."));
        return;
      }

      renderIssueDetails(files.get(currentIndex));
    }

    @Command(
        aliases = "s",
        subcommands = HelpCommand.class,
        description = "Submit issues to OpenAI.")
    void submit(
        @Parameters(arity = "0..*", description = "The subset of issues to submit (default: all)")
            List<Integer> issues) {
      // XXX: Validate indices.
      // XXX: Implement.
    }

    @Command(
        aliases = "a",
        subcommands = HelpCommand.class,
        description = "Apply the changes suggested by OpenAI")
    void apply() {
      // XXX: Implement.
    }

    @Command(
        aliases = "n",
        subcommands = HelpCommand.class,
        description = "Move to the next issue.")
    void next() {
      if (currentIndex < files.size() - 1) {
        currentIndex++;
        issues();
      } else {
        out.println("No next issue.");
      }
    }

    @Command(
        aliases = "p",
        subcommands = HelpCommand.class,
        description = "Move to the previous issue.")
    void previous() {
      if (currentIndex > 0) {
        currentIndex--;
        issues();
      } else {
        out.println("No previous issue.");
      }
    }

    String prompt() {
      if (files.isEmpty()) {
        return ansi().fgRed().a("No issues").reset().a('>').toString();
      }

      return ansi()
          .fgCyan()
          .a(files.get(currentIndex).relativeFile())
          .reset()
          .a(" (")
          .bold()
          .a(currentIndex + 1)
          .a('/')
          .a(files.size())
          .boldOff()
          .a(")>")
          .toString();
    }

    private void renderIssueDetails(FileIssues fileIssues) {
      out.println(ansi().a("Issues for ").fgCyan().a(fileIssues.relativeFile()).reset().a(':'));
      renderIssueContext(fileIssues);
      renderIssues(fileIssues);
    }

    private void renderIssueContext(FileIssues fileIssues) {
      ImmutableMap<Integer, ImmutableSet<Integer>> issueLines =
          fileIssues.issues().stream()
              .filter(issue -> issue.line().isPresent())
              .collect(
                  toImmutableMap(
                      issue -> issue.line().getAsInt(),
                      issue ->
                          issue.column().isPresent()
                              ? ImmutableSet.of(issue.column().getAsInt())
                              : ImmutableSet.of(),
                      (a, b) -> ImmutableSet.<Integer>builder().addAll(a).addAll(b).build()));

      // XXX: Make context configurable.
      // XXX: This would be nicer with a `RangeSet`, but then we'd hit
      // https://github.com/google/guava/issues/3033.
      ImmutableSet<Integer> ranges =
          issueLines.keySet().stream()
              .flatMap(line -> IntStream.range(line - 3, line + 4).boxed())
              .collect(toImmutableSet());

      boolean printedCode = false;
      try {
        List<String> lines = Files.readAllLines(fileIssues.file(), UTF_8);
        for (int i = 1; i <= lines.size(); i++) {
          int salience =
              (ranges.contains(i - 1) ? 1 : 0)
                  + (ranges.contains(i) ? 1 : 0)
                  + (ranges.contains(i + 1) ? 1 : 0);
          if (salience > 1) {
            String line = lines.get(i - 1);
            out.print(ansi().fgBlue().a(String.format(Locale.ROOT, "%4d: ", i)).reset());
            out.println(
                issueLines.containsKey(i) ? highlightIssueLine(line, issueLines.get(i)) : line);
            printedCode = true;
          } else if (salience > 0 && printedCode) {
            out.println(ansi().fgBlue().a("....: ").reset());
            printedCode = false;
          }
        }

      } catch (IOException e) {
        // XXX: Review.
        throw new UncheckedIOException("Failed to read file", e);
      }
    }

    private static Ansi highlightIssueLine(String line, ImmutableSet<Integer> positions) {
      Ansi ansi = ansi().fgRed();
      for (int i = 0; i < line.length(); i++) {
        if (positions.contains(i + 1)) {
          ansi.bold().a(line.charAt(i)).boldOff();
        } else {
          ansi.a(line.charAt(i));
        }
      }
      return ansi.reset();
    }

    private void renderIssues(FileIssues fileIssues) {
      ImmutableList<Issue<Path>> issues = fileIssues.issues();
      for (int i = 0; i < issues.size(); i++) {
        out.println(
            ansi()
                .fgBlue()
                .format(String.format(Locale.ROOT, "%2d. ", i + 1))
                .reset()
                .a(' ')
                .a(issues.get(i).description()));
      }
    }

    record FileIssues(Path file, ImmutableList<Issue<Path>> issues) {
      // XXX: Validate that issues are non-empty, and perhaps derive `file`.

      Path relativeFile() {
        return file.getFileSystem().getPath("").toAbsolutePath().relativize(file);
      }
    }
  }
}
