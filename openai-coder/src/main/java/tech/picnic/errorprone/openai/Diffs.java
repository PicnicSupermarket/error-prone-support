package tech.picnic.errorprone.openai;

import static org.fusesource.jansi.Ansi.ansi;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.google.common.base.Splitter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

final class Diffs {
  private Diffs() {}

  // XXX: Review cross-platform newline usage.
  static String unifiedDiff(String before, String after, String path) {
    return String.join("\n", generateUnifiedDiffLines(before, after, path));
  }

  // XXX: Review cross-platform newline usage.
  // XXX: Test!
  // XXX: Name?
  static void printUnifiedDiff(String before, String after, Path path, PrintWriter out) {
    for (String line : generateUnifiedDiffLines(before, after, path.toString())) {
      if (line.startsWith("+")) {
        out.println(ansi().fgGreen().a(line).reset());
      } else if (line.startsWith("-")) {
        out.println(ansi().fgRed().a(line).reset());
      } else if (line.startsWith("@@")) {
        out.println(ansi().fgYellow().a(line).reset());
      } else {
        out.println(line);
      }
    }
  }

  private static List<String> generateUnifiedDiffLines(String before, String after, String path) {
    List<String> originalLines = Splitter.on('\n').splitToList(before);
    List<String> replacementLines = Splitter.on('\n').splitToList(after);

    Patch<String> diff;
    try {
      diff = DiffUtils.diff(originalLines, replacementLines);
    } catch (DiffException e) {
      throw new IllegalStateException("Failed to create diff", e);
    }

    return UnifiedDiffUtils.generateUnifiedDiff(path, path, originalLines, diff, 3);
  }
}
