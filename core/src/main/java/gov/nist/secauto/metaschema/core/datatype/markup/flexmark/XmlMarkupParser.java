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

package gov.nist.secauto.metaschema.core.datatype.markup.flexmark;

import com.vladsch.flexmark.util.sequence.Escaping;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class XmlMarkupParser {
  private static final Logger LOGGER = LogManager.getLogger(XmlMarkupParser.class);

  @NonNull
  public static final Set<String> BLOCK_ELEMENTS = ObjectUtils.notNull(
      Set.of(
          "h1",
          "h2",
          "h3",
          "h4",
          "h5",
          "h6",
          "ul",
          "ol",
          "pre",
          "hr",
          "blockquote",
          "p",
          "table",
          "img"));

  @NonNull
  private static final XmlMarkupParser SINGLETON = new XmlMarkupParser();

  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @NonNull
  public static synchronized XmlMarkupParser instance() {
    return SINGLETON;
  }

  private XmlMarkupParser() {
    // disable construction
  }

  public MarkupLine parseMarkupline(XMLEventReader2 reader) throws XMLStreamException { // NOPMD - acceptable
    StringBuilder buffer = new StringBuilder();
    parseContents(reader, null, buffer);
    String html = buffer.toString().trim();
    return html.isEmpty() ? null : MarkupLine.fromHtml(html);
  }

  public MarkupMultiline parseMarkupMultiline(XMLEventReader2 reader) throws XMLStreamException {
    StringBuilder buffer = new StringBuilder();
    parseToString(reader, buffer);
    String html = buffer.toString().trim();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("XML->HTML: {}", html);
    }
    return html.isEmpty() ? null : MarkupMultiline.fromHtml(html);
  }

  private void parseToString(XMLEventReader2 reader, StringBuilder buffer) // NOPMD - acceptable
      throws XMLStreamException {
    // if (LOGGER.isDebugEnabled()) {
    // LOGGER.debug("parseToString(enter): {}",
    // XmlEventUtil.toString(reader.peek()));
    // }

    outer: while (reader.hasNextEvent() && !reader.peek().isEndElement()) {
      // skip whitespace before the next block element
      XMLEvent nextEvent = XmlEventUtil.skipWhitespace(reader);

      // if (LOGGER.isDebugEnabled()) {
      // LOGGER.debug("parseToString: {}", XmlEventUtil.toString(nextEvent));
      // }

      if (nextEvent.isStartElement()) {
        StartElement start = nextEvent.asStartElement();
        QName name = start.getName();

        // Note: the next element is not consumed. The called method is expected to
        // consume it
        if (BLOCK_ELEMENTS.contains(name.getLocalPart())) {
          parseStartElement(reader, start, buffer);

          // the next event should be the event after the start's END_ELEMENT
          // assert XmlEventUtil.isNextEventEndElement(reader, name) :
          // XmlEventUtil.toString(reader.peek());
        } else {
          // throw new IllegalStateException();
          // stop parsing on first unrecognized event
          break outer;
        }
      }
      // reader.nextEvent();

      // skip whitespace before the next block element
      XmlEventUtil.skipWhitespace(reader);
    }

    // if (LOGGER.isDebugEnabled()) {
    // LOGGER.debug("parseToString(exit): {}", reader.peek() != null ?
    // XmlEventUtil.toString(reader.peek()) : "");
    // }
  }

  private void parseStartElement(XMLEventReader2 reader, StartElement start, StringBuilder buffer)
      throws XMLStreamException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("parseStartElement(enter): {}", XmlEventUtil.toString(start));
    }

    // consume the start event
    reader.nextEvent();

    QName name = start.getName();
    buffer.append('<')
        .append(name.getLocalPart());
    for (Attribute attribute : CollectionUtil.toIterable(
        ObjectUtils.notNull(start.getAttributes()))) {
      buffer
          .append(' ')
          .append(attribute.getName().getLocalPart())
          .append("=\"")
          .append(attribute.getValue())
          .append('"');
    }

    XMLEvent next = reader.peek();
    if (next != null && next.isEndElement()) {
      buffer.append("/>");
      // consume end element event
      reader.nextEvent();
    } else {
      buffer.append('>');

      // parse until the start's END_ELEMENT is reached
      parseContents(reader, start, buffer);

      buffer
          .append("</")
          .append(name.getLocalPart())
          .append('>');

      // the next event should be the start's END_ELEMENT
      XmlEventUtil.assertNext(reader, XMLStreamConstants.END_ELEMENT, name);

      // consume the start's END_ELEMENT
      reader.nextEvent();
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("parseStartElement(exit): {}", reader.peek() != null ? XmlEventUtil.toString(reader.peek()) : "");
    }
  }

  private void parseContents(XMLEventReader2 reader, StartElement start, StringBuilder buffer)
      throws XMLStreamException {
    // if (LOGGER.isDebugEnabled()) {
    // LOGGER.debug("parseContents(enter): {}",
    // XmlEventUtil.toString(reader.peek()));
    // }

    XMLEvent event;
    while (reader.hasNextEvent() && !(event = reader.peek()).isEndElement()) {
      // // skip whitespace before the next list item
      // event = XmlEventUtil.skipWhitespace(reader);

      // if (LOGGER.isDebugEnabled()) {
      // LOGGER.debug("parseContents(before): {}", XmlEventUtil.toString(event));
      // }

      if (event.isStartElement()) {
        StartElement nextStart = event.asStartElement();
        // QName nextName = nextStart.getName();
        parseStartElement(reader, nextStart, buffer);

        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug("parseContents(after): {}",
        // XmlEventUtil.toString(reader.peek()));
        // }

        // assert XmlEventUtil.isNextEventEndElement(reader, nextName) :
        // XmlEventUtil.toString(reader.peek());

        // reader.nextEvent();
      } else if (event.isCharacters()) {
        Characters characters = event.asCharacters();
        buffer.append(Escaping.escapeHtml(characters.getData(), true));
        reader.nextEvent();
      }
    }

    assert start == null
        || XmlEventUtil.isEventEndElement(reader.peek(), ObjectUtils.notNull(start.getName())) : XmlEventUtil
            .generateExpectedMessage(reader.peek(), XMLStreamConstants.END_ELEMENT, start.getName());

    // if (LOGGER.isDebugEnabled()) {
    // LOGGER.debug("parseContents(exit): {}", reader.peek() != null ?
    // XmlEventUtil.toString(reader.peek()) : "");
    // }
  }

}
