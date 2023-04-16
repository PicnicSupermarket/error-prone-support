package tech.picnic.errorprone.openai;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.WillClose;

// XXX: Document that this is about extracting multi-line log messages.
final class LogLineExtractor {
  // XXX: document that this much match an issue log level as well.
  private final Pattern logLineStartMarker;
  private final ImmutableSet<String> issueLogLevels;

  private LogLineExtractor(Pattern logLineStartMarker, ImmutableSet<String> issueLogLevels) {
    this.logLineStartMarker = logLineStartMarker;
    this.issueLogLevels = issueLogLevels;
  }

  static LogLineExtractor mavenErrorAndWarningExtractor() {
    return new LogLineExtractor(
        Pattern.compile("^\\[([A-Z]+)\\] "), ImmutableSet.of("ERROR", "WARNING"));
  }

  List<String> extract(@WillClose InputStream logs) throws IOException {
    List<String> messages = new ArrayList<>();

    boolean shouldRead = false;
    StringBuilder nextMessage = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(logs, UTF_8))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        Optional<String> logLevel = getLogLevel(line);

        if (logLevel.isPresent()) {
          if (!nextMessage.isEmpty()) {
            messages.add(nextMessage.toString());
            nextMessage.setLength(0);
          }

          shouldRead = issueLogLevels.contains(logLevel.orElseThrow());
        }

        if (shouldRead) {
          if (!nextMessage.isEmpty()) {
            nextMessage.append(System.lineSeparator());
          }

          // XXX: This `+ 3` is hacky. Do better.
          nextMessage.append(
              logLevel.isPresent() ? line.substring(logLevel.orElseThrow().length() + 3) : line);
        }
      }
    }

    if (shouldRead && !nextMessage.isEmpty()) {
      messages.add(nextMessage.toString());
    }

    return messages;
  }

  private Optional<String> getLogLevel(String logLine) {
    return Optional.of(logLineStartMarker.matcher(logLine))
        .filter(Matcher::find)
        .map(m -> m.group(1));
  }
}
