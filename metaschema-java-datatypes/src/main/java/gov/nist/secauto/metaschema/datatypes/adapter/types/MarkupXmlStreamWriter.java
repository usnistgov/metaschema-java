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
  protected void handleInsertAnchor(InsertAnchorNode node, XMLStreamWriter writer, QName name, String paramId)
      throws XMLStreamException {
    writer.writeEmptyElement(name.getNamespaceURI(), name.getLocalPart());

    writer.writeAttribute("param-id", paramId);
  }

  @Override
  protected void handleText(XMLStreamWriter writer, String text) throws XMLStreamException {
    writer.writeCharacters(text);
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

}
