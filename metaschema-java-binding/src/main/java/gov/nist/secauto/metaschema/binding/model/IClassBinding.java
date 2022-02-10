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

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedInstance;

import org.codehaus.stax2.XMLStreamReader2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public interface IClassBinding extends IBoundNamedModelDefinition {
  @Override
  IBindingContext getBindingContext();

  /**
   * The class this binding is for.
   * 
   * @return the bound class
   */
  Class<?> getBoundClass();

  // Provides a compatible return value
  @Override
  IBoundFlagInstance getJsonKeyFlagInstance();

  /**
   * Get the class's properties that match the filter.
   * 
   * @param flagFilter
   *          a filter to apply or {@code null} if no filtering is needed
   * @return a collection of properties
   */
  Map<String, ? extends IBoundNamedInstance> getNamedInstances(Predicate<IBoundFlagInstance> flagFilter);

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
   * @param parentInstance
   *          the Java instance for the object containing this object
   * @param context
   *          the parsing context
   * @return the instance or {@code null} if no data was parsed
   * @throws IOException
   *           if an error occurred while reading the parsed content
   * @throws BindingException
   *           if an error occurred parsing content into java instances
   */
  // TODO: check if a boolean return value is needed
  List<Object> readItem(Object parentInstance, IJsonParsingContext context) throws IOException, BindingException;

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
   * @param parentInstance
   *          the Java instance for the object containing this object
   * @param start
   *          the containing start element
   * @param context
   *          the parsing context
   * @return the instance or {@code null} if no data was parsed
   * @throws IOException
   *           if an error occurred while reading the parsed content
   * @throws BindingException
   *           if an error occurred parsing content into java instances
   * @throws XMLStreamException
   *           if an error occurred while parsing the content as XML
   */
  Object readItem(Object parentInstance, StartElement start, IXmlParsingContext context)
      throws BindingException, IOException, XMLStreamException;

  void writeItem(Object item, QName parentName, IXmlWritingContext context) throws IOException, XMLStreamException;

  default void writeItem(Object item, boolean writeObjectWrapper, IJsonWritingContext context) throws IOException {
    writeItems(Collections.singleton(item), writeObjectWrapper, context);
  }

  // for JSON, the entire value needs to be processed to deal with collapsable fields
  void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, IJsonWritingContext context)
      throws IOException;

  /**
   * Create a deep copy of the provided bound object.
   * 
   * @param item
   *          the bound object to copy
   * @param parentInstance
   *          the new object's parent instance or {@code null}
   * @return the copy
   * @throws BindingException
   *           if an error occurred copying content between java instances
   */
  @NotNull
  Object copyBoundObject(@NotNull Object item, Object parentInstance) throws BindingException;
}