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

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public interface JavaTypeAdapter<TYPE> {
  TYPE copy(TYPE obj);

  boolean isParsingStartElement();

  // boolean isParsingEndElement();
  boolean canHandleQName(QName nextQName);

  TYPE parse(String value) throws BindingException;

  /**
   * This method is expected to parse content starting at the next event. Parsing will continue until
   * the next event represents content that is not handled by this method.
   * <p>
   * If {@link #isParsingStartElement()} is {@code true}, then first event to parse will be the
   * {@link XMLEvent#START_ELEMENT} for the containing element. Otherwise, the first event to parse
   * will be the first child of that {@link XMLEvent#START_ELEMENT}.
   * <p>
   * A JavaTypeAdapter is expected to parse until the peeked event is content that is not handled by
   * this method. This also means that if {@link #isParsingStartElement()} is {@code true}, then this
   * method is expected to parse the END_ELEMENT event as well.
   * 
   * @param parsingContext
   *          the XML parser and binding info
   * @return the parsed value
   * @throws BindingException
   *           if a parsing error occurs
   */
  TYPE parse(XmlParsingContext parsingContext) throws BindingException;

  TYPE parse(JsonParsingContext parsingContext) throws BindingException;

  Supplier<?> parseAndSupply(String value) throws BindingException;

  Supplier<TYPE> parseAndSupply(XmlParsingContext parsingContext) throws BindingException;

  Supplier<TYPE> parseAndSupply(JsonParsingContext parsingContext) throws BindingException;

  void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext)
      throws BindingException;

  void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException;

  /**
   * Gets the default value to use as the JSON (or YAML) field name.
   * 
   * @return the default field name
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
