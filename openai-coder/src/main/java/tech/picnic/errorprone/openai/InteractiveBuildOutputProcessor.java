package tech.picnic.errorprone.openai;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static org.fusesource.jansi.Ansi.ansi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.concurrent.NotThreadSafe;
import org.fusesource.jansi.Ansi;
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
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Parameters;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Review whether to enable a *subset* of JLine's built-ins. See
// https://github.com/remkop/picocli/tree/main/picocli-shell-jline3#jline-316-and-picocli-44-example.
// XXX: Consider utilizing `less` for paging. See
// https://github.com/jline/jline3/wiki/Nano-and-Less-Customization.
// XXX: Should we even support those `files.isEmpty()` cases?
// XXX: Allow issues to be dropped?
// XXX: Mark files modified/track modification count?
// XXX: List full diff over multiple rounds?
// XXX: Allow submission of a custom instruction.
@Command(name = "")
@NotThreadSafe
final class InteractiveBuildOutputProcessor {
  private final OpenAi openAi;
  private final PrintWriter out;
  private final Supplier<ImmutableSet<Issue<Path>>> issueSupplier;
  private ImmutableList<FileIssues> files = ImmutableList.of();
  private int currentIndex = 0;

  InteractiveBuildOutputProcessor(
      OpenAi openAi, PrintWriter output, Supplier<ImmutableSet<Issue<Path>>> issueSupplier) {
    this.openAi = openAi;
    this.out = output;
    this.issueSupplier = issueSupplier;
  }

  // XXX: Replace `Supplier<ImmutableSet<Issue<Path>>>` with a custom type that exposes the issue
  // source and can be configured?
  public static void run(OpenAi openAi, Supplier<ImmutableSet<Issue<Path>>> issueSupplier) {
    try (Terminal terminal = TerminalBuilder.terminal()) {
      InteractiveBuildOutputProcessor processor =
          new InteractiveBuildOutputProcessor(openAi, terminal.writer(), issueSupplier);

      PicocliCommandsFactory factory = new PicocliCommandsFactory();
      factory.setTerminal(terminal);

      CommandLine commandLine = new CommandLine(processor, factory);
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

      processor.restart();
      while (true) {
        try {
          systemRegistry.cleanUp();
          systemRegistry.execute(
              reader.readLine(processor.prompt(), null, (MaskingCallback) null, null));
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
    }
  }

  @Command(aliases = "r", subcommands = HelpCommand.class, description = "Restarts issue analysis.")
  void restart() {
    files =
        Multimaps.asMap(Multimaps.index(issueSupplier.get(), Issue::file)).values().stream()
            .map(fileIssues -> FileIssues.of(ImmutableList.copyOf(fileIssues)))
            .collect(toImmutableList());
    currentIndex = 0;
    issues();
  }

  @Command(aliases = "i", subcommands = HelpCommand.class, description = "List issues.")
  void issues() {
    if (files.isEmpty()) {
      out.println(ansi().fgRed().a("No issues.").reset());
      return;
    }

    renderIssueDetails(files.get(currentIndex));
  }

  // XXX: Review `throws` clause.
  // XXX: Allow to submit a custom instruction.
  @Command(aliases = "s", subcommands = HelpCommand.class, description = "Submit issues to OpenAI.")
  void submit(
      @Parameters(description = "The subset of issues to submit (default: all)") @Nullable
          Set<Integer> issueNumbers)
      throws IOException {
    if (files.isEmpty()) {
      out.println(ansi().fgRed().a("No issues.").reset());
      return;
    }

    FileIssues fileIssues = files.get(currentIndex);
    if (issueNumbers != null) {
      for (int i : issueNumbers) {
        if (i <= 0 || i > fileIssues.issues().size()) {
          out.println(ansi().fgRed().a("Invalid issue number: " + i).reset());
          return;
        }
      }
    }

    ImmutableList<Issue<Path>> allIssues = fileIssues.issues();
    ImmutableList<Issue<Path>> selectedIssues =
        issueNumbers == null
            ? allIssues
            : issueNumbers.stream().map(n -> allIssues.get(n - 1)).collect(toImmutableList());

    // XXX: Use `ansi()` and a separate thread to show a spinner.
    out.println("Submitting issue(s) OpenAI...");

    String originalCode = Files.readString(fileIssues.file());
    String instruction =
        Streams.mapWithIndex(
                selectedIssues.stream(),
                (description, index) -> String.format("%s. %s", index + 1, description))
            .collect(joining("\n", "Resolve the following issues:\n", "\n"));
    String result = openAi.requestEdit(originalCode, instruction);

    fileIssues.setProposal(result);

    Diffs.printUnifiedDiff(originalCode, result, fileIssues.relativeFile(), out);
  }

  @Command(
      aliases = "a",
      subcommands = HelpCommand.class,
      description = "Apply the changes suggested by OpenAI")
  void apply() {
    if (files.isEmpty()) {
      out.println(ansi().fgRed().a("No issues.").reset());
      return;
    }

    FileIssues fileIssues = files.get(currentIndex);
    fileIssues
        .proposal()
        .ifPresentOrElse(
            proposal -> {
              try {
                // XXX: Apply the result only if it applies cleanly! Consider using the diff.
                Files.writeString(fileIssues.file(), proposal);
                out.println(ansi().fgGreen().a("Applied changes.").reset());
              } catch (IOException e) {
                out.println(ansi().fgRed().a("Failed to apply changes"));
                e.printStackTrace(out);
                out.print(ansi().reset());
              }
            },
            () ->
                out.println(
                    ansi().fgRed().a("No changes generated yet; run `submit` first.").reset()));
    next();
  }

  @Command(aliases = "n", subcommands = HelpCommand.class, description = "Move to the next issue.")
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
    // XXX: Here, also list the currently suggested patch, if already generated.
    // (...and not yet submitted?)
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
          out.print(ansi().fgYellow().a(String.format(Locale.ROOT, "%4d: ", i)).reset());
          out.println(
              issueLines.containsKey(i) ? highlightIssueLine(line, issueLines.get(i)) : line);
          printedCode = true;
        } else if (salience > 0 && printedCode) {
          out.println(ansi().fgBlue().a(".....").reset());
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
              .format(String.format(Locale.ROOT, "%4d. ", i + 1))
              .reset()
              .a(issues.get(i).description()));
    }
  }

  private static final class FileIssues {
    private final Path file;
    private final ImmutableList<Issue<Path>> issues;
    private Optional<String> proposal = Optional.empty();

    FileIssues(Path file, ImmutableList<Issue<Path>> issues) {
      this.file = file;
      this.issues = issues;
      checkArgument(
          issues.stream().allMatch(issue -> issue.file().equals(file)),
          "Issues must all reference the same file");
    }

    static FileIssues of(ImmutableList<Issue<Path>> issues) {
      return new FileIssues(
          issues.stream()
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("No issues provided"))
              .file(),
          ImmutableList.sortedCopyOf(
              comparingInt((Issue<Path> issue) -> issue.line().orElse(-1))
                  .thenComparingInt(issue -> issue.column().orElse(-1)),
              issues));
    }

    Path file() {
      return file;
    }

    Path relativeFile() {
      return file.getFileSystem().getPath("").toAbsolutePath().relativize(file);
    }

    ImmutableList<Issue<Path>> issues() {
      return issues;
    }

    void setProposal(String proposal) {
      this.proposal = Optional.of(proposal);
    }

    Optional<String> proposal() {
      return proposal;
    }
  }
}
