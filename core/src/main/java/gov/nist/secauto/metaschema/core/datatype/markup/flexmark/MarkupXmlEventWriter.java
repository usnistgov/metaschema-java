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

import com.vladsch.flexmark.parser.ListOptions;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.impl.AbstractMarkupWriter;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.codehaus.stax2.evt.XMLEventFactory2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MarkupXmlEventWriter
    extends AbstractMarkupWriter<XMLEventWriter, XMLStreamException> {

  @NonNull
  protected final XMLEventFactory2 eventFactory;

  /**
   * Construct a new event writer.
   *
   * @param namespace
   *          the XML namespace to use for XMHTML content
   * @param listOptions
   *          list production options
   * @param writer
   *          the XML event stream to write to
   * @param eventFactory
   *          the XML event factory used to generate XML events
   */
  public MarkupXmlEventWriter(
      @NonNull String namespace,
      @NonNull ListOptions listOptions,
      @NonNull XMLEventWriter writer,
      @NonNull XMLEventFactory2 eventFactory) {
    super(namespace, listOptions, writer);
    this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
  }

  /**
   * Get the XML event factory used to generate XML events.
   *
   * @return the XML event factory
   */
  @NonNull
  protected XMLEventFactory2 getEventFactory() {
    return eventFactory;
  }

  /**
   * Get XML events for the provided attributes.
   *
   * @param attributes
   *          the mapping of attribute name to value
   * @return the list of attribute events
   */
  @NonNull
  protected List<Attribute> handleAttributes(@NonNull Map<String, String> attributes) {
    List<Attribute> attrs;
    if (attributes.isEmpty()) {
      attrs = CollectionUtil.emptyList();
    } else {
      attrs = ObjectUtils.notNull(attributes.entrySet().stream()
          .map((entry) -> eventFactory.createAttribute(entry.getKey(), entry.getValue()))
          .collect(Collectors.toList()));
    }
    return attrs;
  }

  @Override
  public void writeEmptyElement(QName qname, Map<String, String> attributes) throws XMLStreamException {
    List<Attribute> attrs = handleAttributes(attributes);
    StartElement start = eventFactory.createStartElement(qname, attrs.isEmpty() ? null : attrs.iterator(), null);

    XMLEventWriter stream = getStream();
    stream.add(start);

    EndElement end = eventFactory.createEndElement(qname, null);
    stream.add(end);
  }

  @Override
  public void writeElementStart(QName qname, Map<String, String> attributes) throws XMLStreamException {
    List<Attribute> attrs = handleAttributes(attributes);
    StartElement start = eventFactory.createStartElement(qname, attrs.isEmpty() ? null : attrs.iterator(), null);
    getStream().add(start);
  }

  @Override
  public void writeElementEnd(QName qname) throws XMLStreamException {
    EndElement end = eventFactory.createEndElement(qname, null);
    getStream().add(end);
  }

  @Override
  public void writeText(CharSequence text) throws XMLStreamException {
    getStream().add(eventFactory.createCharacters(text.toString()));
  }

  @Override
  protected void writeHtmlEntityInternal(String entityText) throws XMLStreamException {
    getStream().add(eventFactory.createEntityReference(entityText, null));
  }

  @Override
  protected void writeComment(CharSequence text) throws XMLStreamException {
    getStream().add(eventFactory.createComment(text.toString()));
  }
}
