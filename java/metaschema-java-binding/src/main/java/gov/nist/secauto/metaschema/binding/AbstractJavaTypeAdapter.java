/**
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

package gov.nist.secauto.metaschema.binding;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import java.io.IOException;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractJavaTypeAdapter<TYPE> implements JavaTypeAdapter<TYPE> {
  public static final String DEFAULT_JSON_FIELD_NAME = "STRVALUE";

  @Override
  public boolean isParsingStartElement() {
    return false;
  }

  @Override
  public boolean canHandleQName(QName nextQName) {
    return false;
  }

  @Override
  public Supplier<?> parseAndSupply(String value) throws BindingException {
    TYPE retval = parse(value);
    return () -> copy(retval);
  }

  @Override
  public Supplier<TYPE> parseAndSupply(XmlParsingContext parsingContext) throws BindingException {
    TYPE retval = parse(parsingContext);
    return () -> copy(retval);
  }

  @Override
  public Supplier<TYPE> parseAndSupply(JsonParsingContext parsingContext) throws BindingException {
    TYPE retval = parse(parsingContext);
    return () -> copy(retval);
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
  public TYPE parse(XmlParsingContext parsingContext) throws BindingException {
    XMLEventReader2 reader = parsingContext.getEventReader();
    StringBuilder builder = new StringBuilder();
    try {
      XMLEvent nextEvent;
      while (!(nextEvent = reader.peek()).isEndElement()) {
        if (nextEvent.isCharacters()) {
          Characters characters = nextEvent.asCharacters();
          builder.append(characters.getData());
          // advance past current event
          reader.nextEvent();
        } else {
          throw new BindingException(String.format("Invalid content '%s' at %s", XmlEventUtil.toString(nextEvent),
              XmlEventUtil.toString(nextEvent.getLocation())));
        }
      }
      // trim leading and trailing whitespace
      return parse(builder.toString().trim());
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  /**
   * This default implementation will parse the value as a string and delegate to the string-based
   * parsing method.
   */
  @Override
  public TYPE parse(JsonParsingContext parsingContext) throws BindingException {
    JsonParser parser = parsingContext.getEventReader();
    String value;
    try {
      value = parser.getValueAsString();
      // skip over value
      parser.nextToken();
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
    if (value == null) {
      throw new BindingException("Unable to parse field value as text");
    }
    return parse(value);
  }

  @Override
  public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext)
      throws BindingException {
    XMLEventFactory2 eventFactory = writingContext.getXMLEventFactory();
    XMLEventWriter writer = writingContext.getEventWriter();
    try {
      if (valueQName != null) {
        StartElement start = eventFactory.createStartElement(valueQName, null, null);
        writer.add(start);
      }

      String content = value.toString();
      Characters characters = eventFactory.createCharacters(content);
      try {
        writer.add(characters);
      } catch (XMLStreamException ex) {
        throw new BindingException(ex);
      }

      if (valueQName != null) {
        EndElement end = eventFactory.createEndElement(valueQName, null);
        writer.add(end);
      }
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException {
    try {
      writingContext.getEventWriter().writeString(value.toString());
    } catch (IOException ex) {
      throw new BindingException(ex);
    }

  }

}
