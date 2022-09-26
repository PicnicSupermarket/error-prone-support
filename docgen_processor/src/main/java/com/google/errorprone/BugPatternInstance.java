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

import static java.util.stream.Collectors.joining;

import com.google.common.base.Preconditions;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.eclipse.jgit.util.IO;

/** A serialization-friendly POJO of the information in a {@link BugPattern}. */
public final class BugPatternInstance {
  private static final Formatter FORMATTER = new Formatter();

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
    instance.name =
        annotation.name().isEmpty() ? element.getSimpleName().toString() : annotation.name();
    instance.altNames = annotation.altNames();
    instance.tags = annotation.tags();
    instance.severity = annotation.severity();
    instance.summary = annotation.summary();
    instance.explanation = annotation.explanation();
    instance.documentSuppression = annotation.documentSuppression();

    Map<String, Object> keyValues = getAnnotation(element, BugPattern.class.getName());
    Object suppression = keyValues.get("suppressionAnnotations");
    if (suppression == null) {
      instance.suppressionAnnotations = new String[] {SuppressWarnings.class.getName()};
    } else {
      Preconditions.checkState(suppression instanceof List);
      @SuppressWarnings("unchecked") // Always List<? extends AnnotationValue>, see above.
      List<? extends AnnotationValue> resultList = (List<? extends AnnotationValue>) suppression;
      instance.suppressionAnnotations =
          resultList.stream().map(AnnotationValue::toString).toArray(String[]::new);
    }

    Path testPath =
        Path.of(
            "error-prone-contrib/src/test/java/"
                + instance.className.replace(".", "/")
                + "Test.java");
    System.out.println("test class for " + instance.name + " = " + testPath.toAbsolutePath());

    try {
      Pattern inputPattern =
          Pattern.compile("\\.addInputLines\\((\\n.*?\".*?\",)\\n(.*?)\\)\\n", Pattern.DOTALL);
      instance.testContent = String.join("\n", Files.readAllLines(testPath));
      Matcher inputMatch = inputPattern.matcher(instance.testContent);
      if (inputMatch.find()) {
        String inputSrc =
            inputMatch
                .group(2) + ",\n";
        System.out.println(inputSrc);
        inputSrc = inputSrc.replaceAll("\\s*\"(.*?)\"(,\\n)", "$1\n");
        System.out.println(inputSrc);
        inputSrc = inputSrc
                .replaceAll("\\\\\"(.*?)\\\\\"", "\"$1\"");
        System.out.println(inputSrc);
        instance.sampleInput = FORMATTER.formatSource(inputSrc);
      }
    } catch (IOException | IllegalStateException | FormatterException e) {
      e.printStackTrace();
    }

    return instance;
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
