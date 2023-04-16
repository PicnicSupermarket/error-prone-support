package tech.picnic.errorprone.openai;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.google.common.base.Splitter;
import java.util.List;

final class Diffs {
  private Diffs() {}

  // XXX: Review cross-platform newline usage.
  static String unifiedDiff(String before, String after, String path) {
    List<String> originalLines = Splitter.on('\n').splitToList(before);
    List<String> replacementLines = Splitter.on('\n').splitToList(after);

    Patch<String> diff;
    try {
      diff = DiffUtils.diff(originalLines, replacementLines);
    } catch (DiffException e) {
      throw new IllegalStateException("Failed to create diff", e);
    }

    return String.join(
        "\n", UnifiedDiffUtils.generateUnifiedDiff(path, path, originalLines, diff, 3));
  }
}
