package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableList;

// XXX: Use or drop.
final class FileEditSuggester {
  private final OpenAi openAi;

  FileEditSuggester(OpenAi openAi) {
    this.openAi = openAi;
  }

  ImmutableList<EditSuggestion> suggestEdits(String fileContent) {
    return ImmutableList.of();
  }

  record EditSuggestion(String replacement, String unifiedPatch) {}
}
