/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.datatypes.adapter.types;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

import gov.nist.secauto.metaschema.datatypes.types.markup.AbstractMarkupString;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatypes.types.markup.flexmark.insertanchor.InsertAnchorNode;
import gov.nist.secauto.metaschema.datatypes.util.IteratorUtil;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class MarkupParser {
  private static final Logger logger = LogManager.getLogger(MarkupParser.class);

  private static final Pattern LEADING_WHITESPACE = Pattern.compile("^\\s+");
  private static final Pattern TRAILING_WHITESPACE = Pattern.compile("\\s+$");

  public MarkupLine parseMarkupline(XMLEventReader2 reader) throws XMLStreamException {
    StringBuilder buffer = new StringBuilder();
    parseContents(reader, null, buffer);
    String html = buffer.toString().trim();
    logger.debug(html);
    MarkupLine retval;
    if (html.isEmpty()) {
      retval = null;
    } else {
      retval = MarkupLine.fromHtml(html);
      logger.debug(retval.toMarkdown());
    }
    return retval;
  }

  public MarkupMultiline parseMarkupMultiline(XMLEventReader2 reader) throws XMLStreamException {
    StringBuilder buffer = new StringBuilder();
    parseToString(reader, buffer);
    String html = buffer.toString().trim();
    logger.debug(html);
    MarkupMultiline retval;
    if (html.isEmpty()) {
      retval = null;
    } else {
      retval = MarkupMultiline.fromHtml(html);
      logger.debug(retval.toMarkdown());
    }
    return retval;
  }

  protected void parseToString(XMLEventReader2 reader, StringBuilder buffer) throws XMLStreamException {
    XMLEvent nextEvent = reader.peek();
    if (logger.isDebugEnabled()) {
      logger.debug("parseToString(enter): {}", XmlEventUtil.toString(nextEvent));
    }

    outer: while (reader.hasNextEvent() && !(nextEvent = reader.peek()).isEndElement()) {
      // skip whitespace before the next block element
      nextEvent = XmlEventUtil.skipWhitespace(reader);

      if (logger.isDebugEnabled()) {
        logger.debug("parseToString: {}", XmlEventUtil.toString(nextEvent));
      }

      if (nextEvent.isStartElement()) {
        StartElement start = nextEvent.asStartElement();
        QName name = start.getName();

        // Note: the next element is not consumed. The called method is expected to
        // consume it
        switch (name.getLocalPart()) {
        case "h1":
        case "h2":
        case "h3":
        case "h4":
        case "h5":
        case "h6":
        case "p":
        case "ul":
        case "ol":
        case "pre":
        case "table":
          parseStartElement(reader, start, buffer);

          // the next event should be the event after the start's END_ELEMENT
          // assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());
          break;
        default:
          // throw new IllegalStateException();
          // stop parsing on first unrecognized event
          break outer;
        }
      }
      // reader.nextEvent();

      // skip whitespace before the next block element
      XmlEventUtil.skipWhitespace(reader);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("parseToString(exit): {}", reader.peek() != null ? XmlEventUtil.toString(reader.peek()) : "");
    }
  }

  private void parseStartElement(XMLEventReader2 reader, StartElement start, StringBuilder buffer)
      throws XMLStreamException {
    if (logger.isDebugEnabled()) {
      logger.debug("parseStartElement(enter): {}", XmlEventUtil.toString(start));
    }

    // consume the start event
    reader.nextEvent();

    QName name = start.getName();
    buffer.append('<');
    buffer.append(name.getLocalPart());
    for (Attribute attribute : IteratorUtil.toIterable(start.getAttributes())) {
      buffer.append(' ');
      buffer.append(attribute.getName().getLocalPart());
      buffer.append("=\"");
      buffer.append(attribute.getValue());
      buffer.append("\"");
    }

    XMLEvent next = reader.peek();
    if (next != null && next.isEndElement()) {
      buffer.append("/>");
      // consume end element event
      reader.nextEvent();
    } else {
      buffer.append(">");

      // parse until the start's END_ELEMENT is reached
      parseContents(reader, start, buffer);

      buffer.append("</");
      buffer.append(name.getLocalPart());
      buffer.append('>');

      // the next event should be the start's END_ELEMENT
      assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(next);

      // consume the start's END_ELEMENT
      reader.nextEvent();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("parseStartElement(exit): {}", reader.peek() != null ? XmlEventUtil.toString(reader.peek()) : "");
    }
  }

  private void parseContents(XMLEventReader2 reader, StartElement start, StringBuilder buffer)
      throws XMLStreamException {
    if (logger.isDebugEnabled()) {
      logger.debug("parseContents(enter): {}", XmlEventUtil.toString(reader.peek()));
    }

    XMLEvent event;
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // // skip whitespace before the next list item
      // event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isDebugEnabled()) {
        logger.debug("parseContents(before): {}", XmlEventUtil.toString(event));
      }

      if (event.isStartElement()) {
        StartElement nextStart = event.asStartElement();
        // QName nextName = nextStart.getName();
        parseStartElement(reader, nextStart, buffer);

        if (logger.isDebugEnabled()) {
          logger.debug("parseContents(after): {}", XmlEventUtil.toString(reader.peek()));
        }

        // assert XmlEventUtil.isNextEventEndElement(reader, nextName) :
        // XmlEventUtil.toString(reader.peek());

        // reader.nextEvent();
      } else if (event.isCharacters()) {
        Characters characters = event.asCharacters();
        buffer.append(characters.getData());
        reader.nextEvent();
      }
    }

    assert start == null || XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil
        .toString(reader.peek());

    if (logger.isDebugEnabled()) {
      logger.debug("parseContents(exit): {}", reader.peek() != null ? XmlEventUtil.toString(reader.peek()) : "");
    }
  }

  /**
   * Parses an event stream as a sequence of markup block elements, stopping when the first
   * non-recognized element is encountered.
   * <p>
   * The stream's current element is expected to be the element directly before the first markup block
   * element.
   * <p>
   * The stream's next element is expected to be the first markup block element.
   * 
   * @param reader
   *          the event stream reader
   * @return the parsed sequence of markup block elements as a {@link AbstractMarkupString}
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  protected MarkupMultiline parseMarkupMultilineAsAST(XMLEventReader2 reader) throws XMLStreamException {
    MutableDataSet options = new MutableDataSet();
    Document document = new Document(options, BasedSequence.EMPTY);

    XMLEvent event = reader.peek();
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(event));
    }

    outer: while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // skip whitespace before the next block element
      event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      if (event.isStartElement()) {
        StartElement start = event.asStartElement();
        QName name = start.getName();

        Node node;
        // Note: the next element is not consumed. The called method is expected to
        // consume it
        switch (name.getLocalPart()) {
        case "h1":
          node = processBlockH1(reader, start);
          break;
        case "h2":
          node = processBlockH2(reader, start);
          break;
        case "h3":
          node = processBlockH3(reader, start);
          break;
        case "h4":
          node = processBlockH4(reader, start);
          break;
        case "h5":
          node = processBlockH5(reader, start);
          break;
        case "h6":
          node = processBlockH6(reader, start);
          break;
        case "p":
          node = processBlockParagraph(reader, start);
          break;
        case "ul":
          node = processBlockUnorderedList(reader, start);
          break;
        case "ol":
          node = processBlockOrderedList(reader, start);
          break;
        case "pre":
          node = processBlockPreformattedText(reader, start);
          break;
        case "table":
          node = processBlockTable(reader, start);
          break;
        default:
          // throw new IllegalStateException();
          // stop parsing on first unrecognized event
          break outer;
        }
        document.appendChild(node);
        // the matching END_ELEMENT event should be the next event
        assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());
      }
      reader.nextEvent();

      // skip whitespace before the next block element
      event = XmlEventUtil.skipWhitespace(reader);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", reader.peek() != null ? XmlEventUtil.toString(reader.peek()) : "");
    }

    MarkupMultiline retval = new MarkupMultiline(document);
    // System.out.println(retval.toMarkdown());
    return retval;
  }

  /**
   * Handles a paragraph block.
   * <p>
   * The stream's current element is expected to be the element directly before the handled block.
   * <p>
   * The stream's next element is expected to be the handled block.
   * 
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private Paragraph processBlockParagraph(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    QName name = start.getName();

    Paragraph paragraph = new Paragraph();
    // Note: the following method is expected to consume the current element
    handleBlockContentsWithAnchorsAndInserts(paragraph, reader, start);

    assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());
    return paragraph;
  }

  /**
   * Handles the mixed contents of a block that contains anchors, inserts, and other inline elements.
   * <p>
   * The stream's current element is expected to be the block element.
   * <p>
   * The stream's next element is expected to be the first content child of the block.
   *
   * @param blockNode
   *          the AST node of the calling block
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleBlockContentsWithAnchorsAndInserts(Node blockNode, XMLEventReader2 reader, StartElement start)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }

    // consume the containing block
    XMLEvent event = reader.nextEvent();

    boolean firstEvent = true;
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // skip whitespace before the next list item
      event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      // Note: the following method is expected to consume the current element
      handleInlineAndAnchorsAndInsert(blockNode, reader, firstEvent);
      firstEvent = false;
    }

    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  /**
   * Handles a unordered list block.
   * <p>
   * The stream's current element is expected to be the element directly before the handled block.
   * <p>
   * The stream's next element is expected to be the handled block.
   * 
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private BulletList processBlockUnorderedList(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    BulletList retval = new BulletList();
    // Note: the following method is expected to consume the current element
    handleListContents(retval, reader, start);
    return retval;
  }

  /**
   * Handles a ordered list block.
   * <p>
   * The stream's current element is expected to be the element directly before the handled block.
   * <p>
   * The stream's next element is expected to be the handled block.
   * 
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private OrderedList processBlockOrderedList(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    OrderedList retval = new OrderedList();
    // Note: the following method is expected to consume the current element
    handleListContents(retval, reader, start);
    return retval;
  }

  /**
   * Handles the contents of a list block. This method will read until the END_ELEMENT for the block
   * is encountered.
   * <p>
   * The stream's current element is expected to be the element directly before the handled block.
   * <p>
   * The stream's next element is expected to be the handled block.
   * 
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleListContents(ListBlock list, XMLEventReader2 reader, StartElement start)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }
    // consume the containing list block
    XMLEvent event = reader.nextEvent();

    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // skip whitespace before the next list item
      event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      boolean handled = false;
      if (event.isStartElement()) {
        StartElement nextStart = event.asStartElement();
        QName nextName = nextStart.getName();

        if ("li".equals(nextName.getLocalPart())) {
          ListItem listItem;
          if (list instanceof OrderedList) {
            listItem = new OrderedListItem();
          } else {
            listItem = new BulletListItem();
          }
          // Note: the following method is expected to consume the current element
          handleListItemContents(listItem, reader, nextStart);
          list.appendChild(listItem);
          handled = true;

          assert XmlEventUtil.isNextEventEndElement(reader, nextName) : XmlEventUtil.toString(reader.peek());

          // skip over END_ELEMENT li
          reader.nextEvent();

          // skip whitespace before the next list item
          event = XmlEventUtil.skipWhitespace(reader);
        }
      }

      if (!handled) {
        throw new IllegalStateException(
            String.format("expected START_ELEMENT li, but found '%s'", XmlEventUtil.toString(event)));
      }
    }

    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  /**
   * Handles the contents of a list item. This method will read until the END_ELEMENT for the list
   * item is encountered.
   * <p>
   * The stream's current element is expected to be the list item's element.
   * <p>
   * The stream's next element is expected to be the first contents of the list item.
   * 
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled list item's event
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleListItemContents(ListItem listItem, XMLEventReader2 reader, StartElement start)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }
    QName name = start.getName();

    // consume next START_ELEMENT event as the starting point for parse
    XMLEvent event = reader.nextEvent();

    boolean firstEvent = true;
    // parse until the containing element's END_ELEMENT is reached
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      boolean handled = false;
      if (event.isStartElement()) {
        StartElement nextStart = event.asStartElement();
        QName nextName = nextStart.getName();

        switch (nextName.getLocalPart()) {
        case "ol": {
          // Note: the following method is expected to consume the current element
          Node child = processBlockOrderedList(reader, nextStart);

          assert XmlEventUtil.isNextEventEndElement(reader, nextName) : XmlEventUtil.toString(reader.peek());
          listItem.appendChild(child);
          firstEvent = false;
          handled = true;
          break;
        }
        case "ul": {
          // Note: the following method is expected to consume the current element
          Node child = processBlockUnorderedList(reader, nextStart);

          assert XmlEventUtil.isNextEventEndElement(reader, nextName) : XmlEventUtil.toString(reader.peek());
          listItem.appendChild(child);
          firstEvent = false;
          handled = true;
          break;
        }
        default:
          throw new RuntimeException(String.format("Unrecognized tag '%s'",nextName.getLocalPart()));
        }
      }

      if (!handled) {
        // Note: the following method is expected to consume the current element
        handleInlineAndAnchorsAndInsert(listItem, reader, firstEvent);
        firstEvent = false;
      }
    }

    assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  /**
   * Handles an instance of mixed content for a block element.
   * <p>
   * The stream's current element is expected to be the block's element or the previous child of the
   * block.
   * <p>
   * The stream's next element is expected to be the next content of the block item.
   * 
   * @param node
   *          the parent AST node
   * @param reader
   *          the event stream reader
   * @param firstEvent
   *          {@code true} if this handler is handling the first child of the parent element, or
   *          {@code false} otherwise
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleInlineAndAnchorsAndInsert(Node node, XMLEventReader2 reader, boolean firstEvent)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(reader.peek()));
    }

    // consume next START_ELEMENT event as the starting point for parse
    XMLEvent event = reader.peek();

    if (logger.isTraceEnabled()) {
      logger.trace("event: {}", XmlEventUtil.toString(event));
    }

    if (event.isStartElement()) {
      StartElement nextStart = event.asStartElement();
      QName nextName = nextStart.getName();
      if ("insert".equals(nextName.getLocalPart())) {
        handleInlineInsert(node, reader, nextStart);
        assert XmlEventUtil.isNextEventEndElement(reader, nextName) : XmlEventUtil.toString(reader.peek());
      } else if ("a".equals(nextName.getLocalPart())) {
        processInlineAnchor(node, reader, nextStart);
        assert XmlEventUtil.isNextEventEndElement(reader, nextName) : XmlEventUtil.toString(reader.peek());
      } else {
        handleInlineContent(node, reader, event, firstEvent);
      }
      reader.nextEvent();
    } else if (event.isCharacters()) {
      node.appendChild(
          new Text(processInlineCharacters(event.asCharacters(), firstEvent, reader.peek().isEndElement())));
      reader.nextEvent();
    } else {
      throw new IllegalStateException(String.format("Unknown content '%s' at location '%s'.",
          XmlEventUtil.toString(event), XmlEventUtil.toString(event.getLocation())));
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  private Node processBlockPreformattedText(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    QName name = start.getName();

    FencedCodeBlock retval = new FencedCodeBlock();
    // Note: the following method is expected to consume the current element
    handleBlockContentsWithAnchors(retval, reader, start);

    assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());
    return retval;
  }

  private Node processBlockTable(@SuppressWarnings("unused") XMLEventReader2 reader,
      @SuppressWarnings("unused") StartElement start) {
    TableBlock table = new TableBlock();
    // TODO: handle rows
    return table;
  }

  private Heading processBlockH1(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(1, reader, start);
  }

  private Heading processBlockH2(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(2, reader, start);
  }

  private Heading processBlockH3(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(3, reader, start);
  }

  private Heading processBlockH4(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(4, reader, start);
  }

  private Heading processBlockH5(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(5, reader, start);
  }

  private Heading processBlockH6(XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    return processBlockHeadings(6, reader, start);
  }

  private Heading processBlockHeadings(int level, XMLEventReader2 reader, StartElement start)
      throws XMLStreamException {
    QName name = start.getName();
    Heading node = new Heading();
    node.setLevel(level);
    // Note: the following method is expected to consume the current element
    handleBlockContents(node, reader, start);

    assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());

    return node;
  }

  /**
   * Handles the mixed contents of a block that contains anchors and other inline elements.
   * <p>
   * The stream's current element is expected to be the block element.
   * <p>
   * The stream's next element is expected to be the first content child of the block.
   *
   * @param blockNode
   *          the AST node of the calling block
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled block
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleBlockContentsWithAnchors(Node node, XMLEventReader2 reader, StartElement start)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }

    // consume the containing block
    XMLEvent event = reader.nextEvent();

    boolean firstEvent = true;
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // skip whitespace before the next list item
      event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      boolean handled = false;
      if (event.isStartElement()) {
        StartElement nextStart = event.asStartElement();
        QName nextName = nextStart.getName();

        if ("a".equals(nextName.getLocalPart())) {
          // Note: the following method is expected to consume the current element
          processInlineAnchor(node, reader, nextStart);
          handled = true;
          firstEvent = false;
        }
      }

      if (!handled) {
        // Note: the following method is expected to consume the current element
        handleInlineContent(node, reader, event, firstEvent);
        firstEvent = false;
      }
    }

    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  private String processInlineCharacters(Characters characters, boolean stripLeadingWhitespace,
      boolean stripTrailingWhitespace) {
    String text = characters.getData();

    if (stripLeadingWhitespace && stripTrailingWhitespace) {
      text = text.trim();
    } else if (stripLeadingWhitespace) {
      text = text.replaceFirst(LEADING_WHITESPACE.pattern(), "");
    } else if (stripTrailingWhitespace) {
      text = text.replaceFirst(TRAILING_WHITESPACE.pattern(), "");
    }
    return text;
  }

  // expected to consume the element
  private void processInlineAnchor(Node node, XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    Attribute href = start.getAttributeByName(new QName("href"));
    Link link = new Link();
    if (href != null) {
      link.setUrlChars(CharSubSequence.of(href.getValue()));
    }

    XMLEvent event = reader.peek();
    if (!event.isEndElement()) {
      // Note: the following method is expected to consume the current element
      handleBlockContents(link, reader, start);
    }
    node.appendChild(link);
    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());
  }

  private void handleInlineInsert(Node node, XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    Attribute paramId = start.getAttributeByName(new QName("param-id"));
    if (paramId == null) {
      throw new IllegalStateException("param-id is missing on insert element");
    }
    InsertAnchorNode insert = new InsertAnchorNode(paramId.getValue());
    node.appendChild(insert);

    // consume this element
    reader.nextEvent();
    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());
  }

  private void handleInlineImage(Node node, XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    Attribute alt = start.getAttributeByName(new QName("alt"));
    Attribute src = start.getAttributeByName(new QName("src"));
    Image image = new Image();
    if (alt != null) {
      image.setText(CharSubSequence.of(alt.getValue()));
    }
    if (src != null) {
      image.setUrlChars(CharSubSequence.of(src.getValue()));
    }
    node.appendChild(image);
    // consume this element
    reader.nextEvent();
    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());
  }

  private void handleInlineContent(Node node, XMLEventReader2 reader, XMLEvent start, boolean firstEvent)
      throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }

    XMLEvent event = start;

    if (event.isCharacters()) {
      node.appendChild(
          new Text(processInlineCharacters(event.asCharacters(), firstEvent, reader.peek().isEndElement())));
      reader.nextEvent();
    } else if (event.isStartElement()) {
      StartElement nextStart = event.asStartElement();
      QName name = nextStart.getName();

      // consume the event
      // reader.hasNextEvent();
      switch (name.getLocalPart()) {
      case "q": {
        HtmlEntity quote = new HtmlEntity(CharSubSequence.of("q"));
        node.appendChild(quote);
        handleBlockContents(quote, reader, nextStart);
        break;
      }
      case "code": {
        Code newNode = new Code();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "em": {
        Emphasis newNode = new Emphasis();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "i": {
        Emphasis newNode = new Emphasis();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "strong": {
        StrongEmphasis newNode = new StrongEmphasis();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "b": {
        StrongEmphasis newNode = new StrongEmphasis();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "sub": {
        Subscript newNode = new Subscript();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "sup": {
        Superscript newNode = new Superscript();
        node.appendChild(newNode);
        handleBlockContents(newNode, reader, nextStart);
        break;
      }
      case "img":
        handleInlineImage(node, reader, nextStart);
        break;
      default:
        throw new IllegalStateException(String.format("Unknown content '%s' at location '%s'.",
            XmlEventUtil.toString(event), XmlEventUtil.toString(event.getLocation())));
      }
      assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());
      node.appendChild(node);

      // event = reader.nextEvent();
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

  /**
   * Handles the contents of a block.
   * <p>
   * The stream's current element is expected to be the block element.
   * <p>
   * The stream's next element is expected to be the first content child of the block.
   *
   * @param node
   *          the AST node of the calling element
   * @param reader
   *          the event stream reader
   * @param start
   *          the handled block's event
   * @return the AST for the handled element
   * @throws XMLStreamException
   *           if a parse error occurs
   * @throws IllegalStateException
   *           if unexpected content is encountered
   */
  private void handleBlockContents(Node node, XMLEventReader2 reader, StartElement start) throws XMLStreamException {
    if (logger.isTraceEnabled()) {
      logger.trace("enter: {}", XmlEventUtil.toString(start));
    }

    // consume the containing block
    XMLEvent event = reader.nextEvent();

    boolean firstEvent = true;
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // skip whitespace before the next list item
      event = XmlEventUtil.skipWhitespace(reader);

      if (logger.isTraceEnabled()) {
        logger.trace("event: {}", XmlEventUtil.toString(event));
      }

      // Note: the following method is expected to consume the current element
      handleInlineContent(node, reader, event, firstEvent);
      firstEvent = false;
    }

    assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(reader.peek());

    if (logger.isTraceEnabled()) {
      logger.trace("exit: {}", XmlEventUtil.toString(reader.peek()));
    }
  }

}
