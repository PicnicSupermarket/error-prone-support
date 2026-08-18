package tech.picnic.errorprone.documentation;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import java.util.List;
import java.util.Locale;
import javax.lang.model.element.Name;

/**
 * A renderer that expresses the body of a Javadoc comment as Markdown.
 *
 * <p>Rendering is a best-effort operation: the inline tags and HTML elements used by this project
 * are translated, while anything else is emitted verbatim. The latter is generally sufficient, as
 * Markdown renderers pass through inline HTML. Note that block tags such as {@code @param} are not
 * rendered at all, as only a comment's body is considered.
 */
// XXX: Consider rendering `{@link}` references as hyperlinks to the associated documentation.
final class MarkdownRenderer {
  private final StringBuilder markdown = new StringBuilder();

  /**
   * Whether the tree currently being rendered is nested inside a {@code pre} element, in which case
   * its content is already rendered verbatim.
   */
  private boolean preformatted;

  private MarkdownRenderer() {}

  /**
   * Renders the body of the given Javadoc comment as Markdown.
   *
   * @param docComment The Javadoc comment of interest.
   * @return A possibly empty string.
   */
  static String render(DocCommentTree docComment) {
    MarkdownRenderer renderer = new MarkdownRenderer();
    renderer.append(docComment.getFullBody());
    return renderer.markdown.toString().strip();
  }

  private void append(List<? extends DocTree> nodes) {
    nodes.forEach(this::append);
  }

  /*
   * Note that HTML elements are not nested inside the `DocTree` representation: a start and end tag
   * are siblings of the content they surround.
   */
  private void append(DocTree node) {
    switch (node) {
      case TextTree text -> markdown.append(text.getBody());
      case LiteralTree literal -> appendLiteral(literal);
      case LinkTree link -> appendLink(link);
      case StartElementTree element -> appendStartElement(element);
      case EndElementTree element -> appendEndElement(element);
      default -> markdown.append(node);
    }
  }

  private void appendLiteral(LiteralTree literal) {
    String body = literal.getBody().getBody();
    if (preformatted || literal.getKind() == Kind.LITERAL) {
      markdown.append(body);
    } else {
      appendCode(body);
    }
  }

  private void appendLink(LinkTree link) {
    String signature = link.getReference().getSignature();
    if (!link.getLabel().isEmpty()) {
      append(link.getLabel());
    } else if (link.getKind() == Kind.LINK_PLAIN) {
      markdown.append(signature);
    } else {
      appendCode(signature);
    }
  }

  private void appendStartElement(StartElementTree element) {
    switch (elementName(element.getName())) {
      case "p" -> appendParagraphBreak();
      case "br" -> {
        /* Two trailing spaces make this a Markdown hard line break. */
        stripTrailingWhitespace();
        markdown.append("  \n");
      }
      case "b", "strong" -> markdown.append("**");
      case "em", "i" -> markdown.append('*');
      case "code", "tt" -> markdown.append('`');
      case "pre" -> {
        preformatted = true;
        appendParagraphBreak();
        markdown.append("```java\n");
      }
      default -> markdown.append(element);
    }
  }

  private void appendEndElement(EndElementTree element) {
    switch (elementName(element.getName())) {
      /* The end tags of these elements carry no meaning of their own. */
      case "br", "p" -> {}
      case "b", "strong" -> markdown.append("**");
      case "em", "i" -> markdown.append('*');
      case "code", "tt" -> markdown.append('`');
      case "pre" -> {
        preformatted = false;
        stripTrailingWhitespace();
        markdown.append("\n```\n\n");
      }
      default -> markdown.append(element);
    }
  }

  private void appendCode(String code) {
    markdown.append('`').append(code).append('`');
  }

  /**
   * Starts a new Markdown paragraph, replacing any whitespace already emitted; Javadoc paragraphs
   * are commonly preceded by a blank line as well.
   */
  private void appendParagraphBreak() {
    stripTrailingWhitespace();
    markdown.append("\n\n");
  }

  private void stripTrailingWhitespace() {
    while (!markdown.isEmpty() && Character.isWhitespace(markdown.charAt(markdown.length() - 1))) {
      markdown.setLength(markdown.length() - 1);
    }
  }

  private static String elementName(Name name) {
    return name.toString().toLowerCase(Locale.ROOT);
  }
}
