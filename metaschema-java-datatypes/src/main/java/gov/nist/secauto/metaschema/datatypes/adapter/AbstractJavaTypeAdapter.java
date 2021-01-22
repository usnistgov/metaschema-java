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

package gov.nist.secauto.metaschema.datatypes.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Provides a basic Java type adapter implementation. This implementation should be the parent class
 * of all Java type adapter implementations.
 * 
 * @param <TYPE>
 *          the Java type this adapter supports
 */
public abstract class AbstractJavaTypeAdapter<TYPE> implements JavaTypeAdapter<TYPE> {
  public static final String DEFAULT_JSON_FIELD_NAME = "STRVALUE";

  private final Class<TYPE> clazz;

  /**
   * Construct a new Java type adapter for a provided class.
   * 
   * @param clazz
   *          the Java type this adapter supports
   */
  protected AbstractJavaTypeAdapter(Class<TYPE> clazz) {
    this.clazz = clazz;
  }

  @Override
  public Class<TYPE> getJavaClass() {
    return clazz;
  }

  @Override
  public boolean isParsingStartElement() {
    return false;
  }

  @Override
  public boolean canHandleQName(QName nextQName) {
    return false;
  }

  @Override
  public String getDefaultJsonFieldName() {
    return DEFAULT_JSON_FIELD_NAME;
  }

  @Override
  public boolean isUnrappedValueAllowedInXml() {
    return false;
  }

  @Override
  public TYPE parse(XMLEventReader2 eventReader) throws IOException {
    StringBuilder builder = new StringBuilder();
    try {
      XMLEvent nextEvent;
      while (!(nextEvent = eventReader.peek()).isEndElement()) {
        if (nextEvent.isCharacters()) {
          Characters characters = nextEvent.asCharacters();
          builder.append(characters.getData());
          // advance past current event
          eventReader.nextEvent();
        } else {
          throw new IOException(String.format("Invalid content '%s' at %s", XmlEventUtil.toString(nextEvent),
              XmlEventUtil.toString(nextEvent.getLocation())));
        }
      }
      // trim leading and trailing whitespace
      return parse(builder.toString().trim());
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * This default implementation will parse the value as a string and delegate to the string-based
   * parsing method.
   */
  @Override
  public TYPE parse(JsonParser parser) throws IOException {
    String value = parser.getValueAsString();
    if (value == null) {
      throw new IOException("Unable to parse field value as text");
    }
    // skip over value
    parser.nextToken();
    return parse(value);
  }

  @Override
  public String asString(Object value) {
    return value.toString();
  }

  @Override
  public void writeXml(Object value, StartElement parent, XMLEventFactory2 eventFactory, XMLEventWriter eventWriter)
      throws IOException, XMLStreamException {
    try {
      String content = asString(value);
      Characters characters = eventFactory.createCharacters(content);
      eventWriter.add(characters);
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void writeXmlCharacters(Object value, QName parentName, XMLStreamWriter2 writer) throws XMLStreamException {
    String content = asString(value);
    writer.writeCharacters(content);
  }

  @Override
  public void writeJsonValue(Object value, JsonGenerator generator)
      throws IOException {
    generator.writeString(value.toString());
  }
}
