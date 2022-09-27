/*
 * Copyright 2015 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone;

import com.google.common.base.Preconditions;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.apache.commons.text.StringEscapeUtils;

/** A serialization-friendly POJO of the information in a {@link BugPattern}. */
public final class BugPatternInstance {
  private static final Formatter FORMATTER = new Formatter();

  private static final Pattern INPUT_LINES_PATTERN = Pattern.compile("\\.addInputLines\\((.*?)\\)\n", Pattern.DOTALL);
  private static final Pattern OUTPUT_LINES_PATTERN = Pattern.compile("\\.addOutputLines\\((.*?)\\)\n", Pattern.DOTALL);
  private static final Pattern LINES_PATTERN = Pattern.compile("\"(.*)\"");

  public String className;
  public String name;
  public String summary;
  public String explanation;
  public String[] altNames;
  public String category;
  public String[] tags;
  public SeverityLevel severity;
  public String[] suppressionAnnotations;
  public boolean documentSuppression = true;

  public String testContent;
  public String sampleInput;
  public String sampleOutput;

  public static BugPatternInstance fromElement(Element element) {
    BugPatternInstance instance = new BugPatternInstance();
    instance.className = element.toString();

    BugPattern annotation = element.getAnnotation(BugPattern.class);
    instance.name = annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name();
    instance.altNames = annotation.altNames();
    instance.tags = annotation.tags();
    instance.severity = annotation.severity();
    instance.summary = annotation.summary();
    instance.explanation = annotation.explanation();
    instance.documentSuppression = annotation.documentSuppression();

    Map<String, Object> keyValues = getAnnotation(element, BugPattern.class.getName());
    Object suppression = keyValues.get("suppressionAnnotations");
    if (suppression == null) {
      instance.suppressionAnnotations = new String[] { SuppressWarnings.class.getName() };
    } else {
      Preconditions.checkState(suppression instanceof List);
      @SuppressWarnings("unchecked") // Always List<? extends AnnotationValue>, see above.
      List<? extends AnnotationValue> resultList = (List<? extends AnnotationValue>) suppression;
      instance.suppressionAnnotations = resultList.stream().map(AnnotationValue::toString).toArray(String[]::new);
    }

    Path testPath = getPath(instance);
    System.out.println("test class for " + instance.name + " = " + testPath.toAbsolutePath());

    try {
      instance.testContent = String.join("\n", Files.readAllLines(testPath));
      instance.sampleInput = getInputLines(instance.testContent);
      instance.sampleOutput = getOutputLines(instance.testContent);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return instance;
  }

  private static Path getPath(BugPatternInstance instance) {
    return Path.of(
        "error-prone-contrib/src/test/java/"
            + instance.className.replace(".", "/")
            + "Test.java");
  }

  private static String getInputLines(String content) {
    return getLines(INPUT_LINES_PATTERN, content);
  }

  private static String getOutputLines(String content) {
    return getLines(OUTPUT_LINES_PATTERN, content);
  }

  private static String getLines(Pattern pattern, String content) {
    Matcher match = pattern.matcher(content);

    if (!match.find()) {
      return null;
    }

    String argument = match.group(1);

    List<String> lines = findAllGroups(argument, LINES_PATTERN);
    // Remove in/A.java and out/A.java
    lines.remove(0);

    String sampleCode = String.join("\n", lines);
    sampleCode = StringEscapeUtils.unescapeJava(sampleCode);

    try {
      // Trim to remove trailing line-break.
      return FORMATTER.formatSource(sampleCode).trim();
    } catch (FormatterException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static List<String> findAllGroups(String text, Pattern pattern) {
    List<String> list = new ArrayList<>();
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      for (int i = 1; i <= matcher.groupCount(); i++) {
        list.add(matcher.group(i));
      }
    }

    return list;
  }

  private static Map<String, Object> getAnnotation(Element element, String name) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().toString().equals(name)) {
        return annotationKeyValues(mirror);
      }
    }
    throw new IllegalArgumentException(String.format("%s has no annotation %s", element, name));
  }

  private static Map<String, Object> annotationKeyValues(AnnotationMirror mirror) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (ExecutableElement key : mirror.getElementValues().keySet()) {
      result.put(key.getSimpleName().toString(), mirror.getElementValues().get(key).getValue());
    }
    return result;
  }
}
