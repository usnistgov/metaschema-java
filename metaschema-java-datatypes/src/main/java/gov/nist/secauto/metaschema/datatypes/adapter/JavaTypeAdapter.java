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

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public interface JavaTypeAdapter<TYPE> {
  /**
   * Get the Java class supported by this adapter.
   * 
   * @return the Java class
   */
  Class<TYPE> getJavaClass();

  /**
   * Gets the value as a string suitable for writing as text. This is intended for data types that
   * have a simple string-based structure in XML and JSON, such as for XML attributes or JSON keys. An
   * adapter for a complex data structures that consist of XML elements will throw an
   * {@link UnsupportedOperationException} when this is called.
   * 
   * @param value
   *          the data to formatted as a string
   * @return a string
   * @throws UnsupportedOperationException
   *           if the data type cannot be represented as a string
   */
  String asString(Object value) throws UnsupportedOperationException;

  /**
   * Create a copy of the provided value.
   * 
   * @param obj
   *          the value to copy
   * @return the copy
   */
  TYPE copy(TYPE obj);

  /**
   * Indicates if the adapter will parse the {@link XMLEvents#START_ELEMNT} before parsing the value
   * data.
   * 
   * @return {@code true} if the adapter requires the start element for parsing, or {@code false}
   *         otherwise
   */
  // TODO; implement or remove this
  boolean isParsingStartElement();

  /**
   * Determines if adapter can parse the next element. The next element's {@link QName} is provided
   * for this purpose.
   * <p>
   * This will be called when the parser encounter's an element it does not recognize. This gives the
   * adapter a chance to request parsing of the data.
   * 
   * @param nextElementQName
   *          the next element's namespace-qualified name
   * @return {@code true} if the adapter will parse the element, or {@code false} otherwise
   */
  // TODO: implement this
  boolean canHandleQName(QName nextElementQName);

  /**
   * Parses a provided string. Used to parse XML attributes, simple XML character data, and JSON/YAML
   * property values.
   * 
   * @param value
   *          the string value to parse
   * @return the parsed data as the adapter's type
   * @throws IOException
   *           if an error occurs while parsing
   */
  TYPE parse(String value) throws IOException;

  /**
   * This method is expected to parse content starting at the next event. Parsing will continue until
   * the next event represents content that is not handled by this adapter. This means the event
   * stream should be positioned after any {@link XMLEvents#END_ELEMNT} that corresponds to an
   * {@link XMLEvents#START_ELEMNT} parsed by this adapter.
   * <p>
   * If {@link #isParsingStartElement()} returns {@code true}, then the first event to parse will be
   * the {@link XMLEvent#START_ELEMENT} for the element that contains the value data, then the value
   * data. If this is the case, this method must also parse the corresponding
   * {@link XMLEvents#END_ELEMNT}. Otherwise, the first event to parse will be the value data.
   * <p>
   * The value data is expected to be parsed completely, leaving the event stream on a peeked event
   * corresponding to content that is not handled by this method.
   * 
   * @param eventReader
   *          the XML parser used to read the parsed value
   * @return the parsed value
   * @throws IOException
   *           if a parsing error occurs
   */
  TYPE parse(XMLEventReader2 eventReader) throws IOException;

  /**
   * Parses a JSON property value.
   * 
   * @param parser
   *          the JSON parser used to read the parsed value
   * @return the parsed value
   * @throws IOException
   *           if a parsing error occurs
   */
  TYPE parse(JsonParser parser) throws IOException;

  /**
   * Parses a provided string using {@link #parse(String)}.
   * <p>
   * This method may pre-parse the data and then return copies, since the data can only be parsed
   * once, but the supplier might be called multiple times.
   * 
   * @param value
   *          the string value to parse
   * @return a supplier that will provide new instances of the parsed data
   * @throws IOException
   *           if an error occurs while parsing
   * @see #parse(String)
   */
  default Supplier<TYPE> parseAndSupply(String value) throws IOException {
    TYPE retval = parse(value);
    return () -> copy(retval);
  }

  /**
   * Parses a provided string using {@link #parse(XmlParsingContext)}.
   * <p>
   * This method may pre-parse the data and then return copies, since the data can only be parsed
   * once, but the supplier might be called multiple times.
   * 
   * @param eventReader
   *          the XML parser used to read the parsed value
   * @return a supplier that will provide new instances of the parsed data
   * @throws IOException
   *           if an error occurs while parsing
   * @see #parse(String)
   * @see #parse(XMLEventReader2)
   */
  default Supplier<TYPE> parseAndSupply(XMLEventReader2 eventReader) throws IOException {
    TYPE retval = parse(eventReader);
    return () -> copy(retval);
  }

  /**
   * Parses a provided string using {@link #parse(JsonParsingContext)}.
   * <p>
   * This method may pre-parse the data and then return copies, since the data can only be parsed
   * once, but the supplier might be called multiple times.
   * 
   * @param parser
   *          the JSON parser used to read the parsed value
   * @return a supplier that will provide new instances of the parsed data
   * @throws IOException
   *           if an error occurs while parsing
   * @see #parse(String)
   * @see #parse(JsonParser)
   */
  default Supplier<TYPE> parseAndSupply(JsonParser parser) throws IOException {
    TYPE retval = parse(parser);
    return () -> copy(retval);
  }

  /**
   * Writes the provided Java class instance data as XML. The parent element information is provided
   * as a {@link StartElement} event, which allows namespace information to be obtained from the
   * parent element using the {@link StartElement#getName()} and
   * {@link StartElement#getNamespaceContext()} methods, which can be used when writing the provided
   * instance value.
   * 
   * @param instance
   *          the {@link Field} instance value to write
   * @param parent
   *          the {@link StartElement} XML event that is the parent of the data to write
   * @param eventFactory
   *          the XML event factory used to generate XML writing events
   * @param eventWriter
   *          the XML writer used to output XML as events
   * @throws XMLStreamException
   *           if an unexpected error occurred while processing the XML output
   * @throws IOException
   *           if an unexpected error occurred while writing to the output stream
   */
  void writeXml(Object instance, StartElement parent, XMLEventFactory2 eventFactory, XMLEventWriter eventWriter)
      throws IOException, XMLStreamException;

  /**
   * Writes the provided Java class instance data as an XML character sequence. The parent element
   * information is provided as an XML {@link QName}, which allows namespace information to be
   * obtained from the parent element. Additional namespace information can be gathered using the
   * {@link XMLStreamWriter2#getNamespaceContext()} method, which can be used when writing the
   * provided instance value.
   * 
   * @param instance
   *          the {@link Field} instance value to write
   * @param parentName
   *          the qualified name of the XML data's parent element
   * @param writer
   *          the XML writer used to output the XML data
   * @throws XMLStreamException
   *           if an unexpected error occurred while processing the XML output
   */
  void writeXmlCharacters(Object instance, QName parentName, XMLStreamWriter2 writer) throws XMLStreamException;

  /**
   * Writes the provided Java class instance as a JSON/YAML field value.
   * 
   * @param instance
   *          the {@link Field} instance value to write
   * @param writer
   *          the JSON/YAML writer used to output the JSON/YAML data
   * @throws IOException
   *           if an unexpected error occurred while writing the JSON/YAML output
   */
  void writeJsonValue(Object instance, JsonGenerator writer)
      throws IOException;

  /**
   * Gets the default value to use as the JSON/YAML field name for a Metaschema field value if no JSON
   * value key flag or name is configured.
   * 
   * @return the default field name to use
   */
  String getDefaultJsonFieldName();

  /**
   * Determines if the data type's value is allowed to be unwrapped in XML when the value is a field
   * value.
   * 
   * @return {@code true} if allowed, or {@code false} otherwise.
   */
  boolean isUnrappedValueAllowedInXml();
}
