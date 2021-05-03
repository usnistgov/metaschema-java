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

package gov.nist.secauto.metaschema.binding.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedProperty;
import gov.nist.secauto.metaschema.binding.model.property.Property;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;

import org.codehaus.stax2.XMLStreamReader2;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public interface ClassBinding {
  BindingContext getBindingContext();

  /**
   * The class this binding is for.
   * 
   * @return the bound class
   */
  Class<?> getBoundClass();

  /**
   * Get the JSON key flag, if there is one.
   * 
   * @return the JSON key flag, or {@code null} if there isn't one
   */
  FlagProperty getJsonKey();

  /**
   * Get the class's flag properties.
   * 
   * @return a mapping of field name to property
   */
  Map<String, FlagProperty> getFlagProperties();

  /**
   * Get the class's properties.
   * 
   * @return a mapping of field name to property
   */
  Map<String, ? extends Property> getProperties();

  /**
   * Get the class's properties that match the filter.
   * 
   * @param flagFilter
   *          a filter to apply or {@code null} if no filtering is needed
   * @return a collection of properties
   */
  Map<String, ? extends NamedProperty> getProperties(Predicate<FlagProperty> flagFilter);

  /**
   * Reads a JSON/YAML object storing the associated data in a Java class instance and adds the
   * resulting instance to the provided collector.
   * <p>
   * When called the current {@link JsonToken} of the {@link JsonParser} is expected to be a
   * {@link JsonToken#FIELD_NAME} that is the first field of the object.
   * <p>
   * After returning the current {@link JsonToken} of the {@link JsonParser} is expected to be a
   * {@link JsonToken#END_OBJECT} representing the end of the object for this class.
   * 
   * @param collector
   *          used to gather Java instances
   * @param parentInstance
   *          the Java instance for the object containing this object
   * @param context
   *          the parsing context
   * @return {@code true} if data was parsed, {@code false} otherwise
   * @throws IOException
   * @throws BindingException
   */
  // TODO: check if a boolean return value is needed
  boolean readItem(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException;

  /**
   * Reads a XML element storing the associated data in a Java class instance and adds the resulting
   * instance to the provided collector.
   * <p>
   * When called the next {@link XMLEvent} of the {@link XMLStreamReader2} is expected to be a
   * {@link XMLStreamConstants#START_ELEMENT} that is the XML element associated with the Java class.
   * <p>
   * After returning the next {@link XMLEvent} of the {@link XMLStreamReader2} is expected to be a the
   * next event after the {@link XMLStreamConstants#END_ELEMENT} for the XML
   * {@link XMLStreamConstants#START_ELEMENT} element associated with the Java class.
   * 
   * @param collector
   *          used to gather Java instances
   * @param parentInstance
   *          the Java instance for the object containing this object
   * @param start
   *          the containing start element
   * @param context
   *          the parsing context
   * @return {@code true} if data was parsed, {@code false} otherwise
   * @throws IOException
   * @throws BindingException
   * @throws XMLStreamException
   */
  boolean readItem(PropertyCollector collector, Object parentInstance, StartElement start, XmlParsingContext context)
      throws BindingException, IOException, XMLStreamException;

  void writeItem(Object item, QName parentName, XmlWritingContext context) throws IOException, XMLStreamException;

  default void writeItem(Object item, boolean writeObjectWrapper, JsonWritingContext context) throws IOException {
    writeItems(Collections.singleton(item), writeObjectWrapper, context);
  }

  // for JSON, the entire value needs to be processed to deal with collapsable fields
  void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, JsonWritingContext context)
      throws IOException;
}
