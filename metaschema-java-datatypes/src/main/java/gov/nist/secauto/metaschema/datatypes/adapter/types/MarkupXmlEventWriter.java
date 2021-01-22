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

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.datatypes.types.markup.flexmark.insertanchor.InsertAnchorNode;

import org.codehaus.stax2.evt.XMLEventFactory2;

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
  protected void handleInsertAnchor(InsertAnchorNode node, XMLEventWriter writer, QName name, String paramId)
      throws XMLStreamException {
    List<Attribute> attributes = new LinkedList<>();
    if (node.getName() != null) {
      attributes.add(eventFactory.createAttribute("param-id", paramId));
    }

    StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
    writer.add(start);

    // there are no children
    // visitChildren(node);

    EndElement end = eventFactory.createEndElement(name, null);
    writer.add(end);
  }

  @Override
  protected void handleText(XMLEventWriter writer, String text) throws XMLStreamException {
    writer.add(eventFactory.createCharacters(text));
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
}
