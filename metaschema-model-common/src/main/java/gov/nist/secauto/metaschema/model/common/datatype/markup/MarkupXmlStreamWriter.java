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
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.insertanchor.InsertAnchorNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeVisitor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MarkupXmlStreamWriter
    extends AbstractMarkupXmlWriter<XMLStreamWriter> {

  public MarkupXmlStreamWriter(String namespace, boolean handleBlockElements) {
    super(namespace, handleBlockElements);
  }

  @Override
  protected void handleImage(Image node, XMLStreamWriter writer, QName name, String href, String alt)
      throws XMLStreamException {
    writer.writeEmptyElement(name.getNamespaceURI(), name.getLocalPart());

    writer.writeAttribute("src", href);
    writer.writeAttribute("alt", alt);
  }

  @Override
  protected void handleInsertAnchor(InsertAnchorNode node, XMLStreamWriter writer, QName name)
      throws XMLStreamException {
    writer.writeEmptyElement(name.getNamespaceURI(), name.getLocalPart());

    if (node.getType() != null) {
      writer.writeAttribute("type", node.getType().toString());
    }

    if (node.getIdReference() != null) {
      writer.writeAttribute("id-ref", node.getIdReference().toString());
    }
  }

  @Override
  protected void writeText(XMLStreamWriter writer, String text) throws XMLStreamException {
    writer.writeCharacters(text);
  }

  @Override
  protected void writeHtmlEntity(XMLStreamWriter writer, String entityText) throws XMLStreamException {
    writer.writeEntityRef(entityText);
  }

  @Override
  protected void handleBasicElementStart(Node node, XMLStreamWriter writer, QName name) throws XMLStreamException {
    writer.writeStartElement(name.getNamespaceURI(), name.getLocalPart());
  }

  @Override
  protected void handleBasicElementEnd(Node node, XMLStreamWriter writer, QName name) throws XMLStreamException {
    writer.writeEndElement();
  }

  @Override
  protected void handleLinkStart(LinkNode node, XMLStreamWriter writer, QName name, String href)
      throws XMLStreamException {
    writer.writeStartElement(name.getNamespaceURI(), name.getLocalPart());

    writer.writeAttribute("href", href);
  }

  @Override
  protected void handleLinkEnd(LinkNode node, XMLStreamWriter writer, QName name) throws XMLStreamException {
    writer.writeEndElement();
  }

  @Override
  protected void handleHtmlBlock(HtmlBlock node, XMLStreamWriter writer) throws XMLStreamException {
    Document doc = Jsoup.parse(node.getChars().toString());
    doc.getElementsByTag("html").first().getElementsByTag("body").first().traverse(new StreamNodeVisitor(writer));
  }

  @Override
  protected void handleHtmlInline(HtmlInline node, XMLStreamWriter writer) throws XMLStreamException {
    Document doc = Jsoup.parse(node.getChars().toString());
    doc.getElementsByTag("html").first().getElementsByTag("body").first().traverse(new StreamNodeVisitor(writer));
  }

  private class StreamNodeVisitor implements NodeVisitor {
    private final XMLStreamWriter writer;

    public StreamNodeVisitor(XMLStreamWriter writer) {
      this.writer = writer;
    }

    @Override
    public void head(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0) {
        try {
          if (node instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element element = (org.jsoup.nodes.Element)node;
            if (element.childNodes().isEmpty()) {
              writer.writeEmptyElement(getNamespace(), element.tagName());
            } else {
              writer.writeStartElement(getNamespace(), element.tagName());
            }
            
            for (org.jsoup.nodes.Attribute attr : element.attributes()) {
              writer.writeAttribute(attr.getKey(), attr.getValue());
            }
          } else if (node instanceof org.jsoup.nodes.TextNode) {
            org.jsoup.nodes.TextNode text = (org.jsoup.nodes.TextNode)node;
            writer.writeCharacters(text.text());
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
          org.jsoup.nodes.Element element = (org.jsoup.nodes.Element)node;
          if (!element.childNodes().isEmpty()) {
            try {
              writer.writeEndElement();
            } catch (XMLStreamException ex) {
              throw new RuntimeException(ex);
            }
          }
        }
      }
    }
  }
}
