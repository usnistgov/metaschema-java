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

package gov.nist.secauto.metaschema.core.model.xml;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.util.StreamWriter2Delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

public class IndentingXmlStreamWriter2
    extends StreamWriter2Delegate {
  private String indentText = DEFAULT_INDENT_TEXT;
  private String lineEndText = DEFAULT_LINE_END_TEXT;
  private int depth; // = 0;
  private final Map<Integer, Boolean> depthWithChildMap = new HashMap<>(); // NOPMD - synchronization not needed
  private static final String DEFAULT_INDENT_TEXT = "  ";
  private static final String DEFAULT_LINE_END_TEXT = "\n";

  public IndentingXmlStreamWriter2(XMLStreamWriter2 parent) {
    super(parent);
  }

  protected String getIndentText() {
    return indentText;
  }

  protected void setIndentText(String indentText) {
    Objects.requireNonNull(indentText, "indentText");
    this.indentText = indentText;
  }

  protected String getLineEndText() {
    return lineEndText;
  }

  protected void setLineEndText(String lineEndText) {
    Objects.requireNonNull(lineEndText, "lineEndText");
    this.lineEndText = lineEndText;
  }

  protected void handleStartElement() throws XMLStreamException {
    // update state of parent node
    if (depth > 0) {
      depthWithChildMap.put(depth - 1, true);
    }
    // reset state of current node
    depthWithChildMap.put(depth, false);
    // indent for current depth
    getParent().writeCharacters(getLineEndText());
    getParent().writeCharacters(getIndentText().repeat(depth));
    depth++;
  }

  @Override
  public void writeStartElement(String localName) throws XMLStreamException {
    handleStartElement();
    super.writeStartElement(localName);
  }

  @Override
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    handleStartElement();
    super.writeStartElement(namespaceURI, localName);
  }

  @Override
  public void writeStartElement(String prefix,
      String localName,
      String namespaceURI) throws XMLStreamException {
    handleStartElement();
    super.writeStartElement(prefix, localName, namespaceURI);
  }

  protected void handleEndElement() throws XMLStreamException {
    depth--;
    if (depthWithChildMap.get(depth)) {
      getParent().writeCharacters(getLineEndText());
      getParent().writeCharacters(getIndentText().repeat(depth));
    }
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    handleEndElement();
    super.writeEndElement();
  }

  @Override
  public void writeFullEndElement() throws XMLStreamException {
    handleEndElement();
    super.writeFullEndElement();
  }

  protected void handleEmptyElement() throws XMLStreamException {
    // update state of parent node
    if (depth > 0) {
      depthWithChildMap.put(depth - 1, true);
    }
    // indent for current depth
    getParent().writeCharacters(getLineEndText());
    getParent().writeCharacters(getIndentText().repeat(depth));
  }

  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException {
    handleEmptyElement();
    super.writeEmptyElement(localName);
  }

  @Override
  public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
    handleEmptyElement();
    super.writeEmptyElement(namespaceURI, localName);
  }

  @Override
  public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    handleEmptyElement();
    super.writeEmptyElement(prefix, localName, namespaceURI);
  }
}
