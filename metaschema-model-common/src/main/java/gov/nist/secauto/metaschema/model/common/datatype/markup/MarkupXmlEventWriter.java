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

package gov.nist.secauto.metaschema.model.common.datatype.markup;

import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;

import org.codehaus.stax2.evt.XMLEventFactory2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public class MarkupXmlEventWriter
    extends AbstractMarkupXmlWriter<XMLEventWriter> {
  protected final XMLEventFactory2 eventFactory;

  public MarkupXmlEventWriter(String namespace, boolean handleBlockElements, XMLEventFactory2 eventFactory) {
    super(namespace, handleBlockElements);
    Objects.requireNonNull(eventFactory, "eventFactory");
    this.eventFactory = eventFactory;
  }

  protected XMLEventFactory2 getEventFactory() {
    return eventFactory;
  }

  @Override
  protected void handleImage(Image node, XMLEventWriter writer, QName name, String href, String alt)
      throws XMLStreamException {
    List<Attribute> attributes = new LinkedList<>();
    if (node.getUrl() != null) {
      attributes.add(eventFactory.createAttribute("src", href));
    }

    if (node.getUrl() != null) {
      attributes.add(eventFactory.createAttribute("alt", alt));
    }

    StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
    writer.add(start);

    EndElement end = eventFactory.createEndElement(name, null);
    writer.add(end);
  }

  @Override
  protected void handleInsertAnchor(InsertAnchorNode node, XMLEventWriter writer, QName name)
      throws XMLStreamException {
    List<Attribute> attributes = new ArrayList<>(2);
    attributes.add(eventFactory.createAttribute("type", node.getType().toString()));
    attributes.add(eventFactory.createAttribute("id-ref", node.getIdReference().toString()));

    StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
    writer.add(start);

    // there are no children
    // visitChildren(node);

    EndElement end = eventFactory.createEndElement(name, null);
    writer.add(end);
  }

  @Override
  protected void writeText(XMLEventWriter writer, String text) throws XMLStreamException {
    writer.add(eventFactory.createCharacters(text));
  }

  @Override
  protected void writeHtmlEntity(XMLEventWriter writer, String entityText) throws XMLStreamException {
    writer.add(eventFactory.createEntityReference(entityText, null));
  }

  @Override
  protected void handleBasicElementStart(Node node, XMLEventWriter writer, QName name) throws XMLStreamException {
    StartElement start = eventFactory.createStartElement(name, null, null);
    writer.add(start);
  }

  @Override
  protected void handleBasicElementEnd(Node node, XMLEventWriter writer, QName name) throws XMLStreamException {
    EndElement end = eventFactory.createEndElement(name, null);
    writer.add(end);
  }

  @Override
  protected void handleLinkStart(LinkNode node, XMLEventWriter writer, QName name, String string)
      throws XMLStreamException {
    List<Attribute> attributes = new LinkedList<>();
    if (node.getUrl() != null) {
      attributes.add(eventFactory.createAttribute("href", node.getUrl().toString()));
    }

    StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
    writer.add(start);
  }

  @Override
  protected void handleLinkEnd(LinkNode node, XMLEventWriter writer, QName name) throws XMLStreamException {
    EndElement end = eventFactory.createEndElement(name, null);
    writer.add(end);
  }

  @Override
  protected void handleHtmlBlock(HtmlBlock node, XMLEventWriter writer) throws XMLStreamException {
    Document doc = Jsoup.parse(node.getChars().toString());
    doc.getElementsByTag("html").first().getElementsByTag("body").first().traverse(new StreamNodeVisitor(writer));
  }

  @Override
  protected void handleHtmlInline(HtmlInline node, XMLEventWriter writer) throws XMLStreamException {
    Document doc = Jsoup.parse(node.getChars().toString());
    doc.getElementsByTag("html").first().getElementsByTag("body").first().traverse(new StreamNodeVisitor(writer));
  }

  private class StreamNodeVisitor implements NodeVisitor {
    private final XMLEventWriter writer;

    public StreamNodeVisitor(XMLEventWriter writer) {
      this.writer = writer;
    }

    @Override
    public void head(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0) {
        try {
          if (node instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
            if (element.childNodes().isEmpty()) {
              writer.add(eventFactory.createStartElement(new QName(getNamespace(), element.tagName()), null, null));
            } else {
              List<Attribute> attributes = new LinkedList<>();
              for (org.jsoup.nodes.Attribute attr : element.attributes()) {
                attributes.add(eventFactory.createAttribute(attr.getKey(), attr.getValue()));
              }

              writer.add(eventFactory.createStartElement(new QName(getNamespace(), element.tagName()),
                  attributes.iterator(), null));
            }
          } else if (node instanceof org.jsoup.nodes.TextNode) {
            org.jsoup.nodes.TextNode text = (org.jsoup.nodes.TextNode) node;
            writer.add(eventFactory.createCharacters(text.text()));
          }
        } catch (XMLStreamException ex) {
          throw new RuntimeException(ex);
        }
      }
    }

    @Override
    public void tail(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0) {
        if (node instanceof org.jsoup.nodes.Element) {
          org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
          try {
            writer.add(eventFactory.createEndElement(new QName(getNamespace(), element.tagName()), null));
          } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
          }
        }
      }
    }
  }
}
