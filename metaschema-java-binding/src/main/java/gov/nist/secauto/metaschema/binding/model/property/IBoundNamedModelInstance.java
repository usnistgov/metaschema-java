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

package gov.nist.secauto.metaschema.binding.model.property;

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.IBoundNamedModelDefinition;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.info.IDataTypeHandler;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This marker interface provides common methods for interacting with bound object values.
 */
public interface IBoundNamedModelInstance extends IBoundNamedInstance, INamedModelInstance {
  /**
   * Retrieve the {@link IClassBinding} associated with the instance.
   * 
   * @return the class binding or {@code null} if the instance is not a bound class
   */
  IClassBinding getClassBinding();

  @Override
  IBoundNamedModelDefinition getDefinition();

  @NotNull
  IDataTypeHandler getDataTypeHandler();

  /**
   * Get the item values associated with the provided value.
   * 
   * @param value
   *          the value which may be a singleton or a collection
   * @return the ordered collection of values
   */
  @NotNull
  Collection<@NotNull ? extends Object> getItemValues(Object value);

  /**
   * Reads an individual XML item from the XML stream.
   * 
   * @param parentInstance
   *          the object the data is parsed into
   * @param start
   *          the current containing XML element
   * @param context
   *          the XML parsing context
   * @return the item read, or {@code null} if no item was read
   * @throws XMLStreamException
   *           if an error occurred while generating an {@link XMLEvent}
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  Object readItem(@Nullable Object parentInstance, @NotNull StartElement start, @NotNull IXmlParsingContext context)
      throws XMLStreamException, IOException;

  /**
   * Reads a set of JSON items from the JSON stream.
   * <p>
   * An item is a complete value, which can be a {@link JsonToken#START_OBJECT}, or a value token.
   * 
   * @param parentInstance
   *          the object the data is parsed into
   * @param context
   *          the JSON/YAML parsing context
   * @return the items read, or {@code null} if no item was read
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  @NotNull
  List<@NotNull Object> readItem(@Nullable Object parentInstance, boolean requiresJsonKey, @NotNull IJsonParsingContext context) throws IOException;

  boolean writeItem(@NotNull Object item, @NotNull QName parentName, @NotNull IXmlWritingContext context) throws XMLStreamException, IOException;

  @NotNull
  Object copyItem(@NotNull Object fromItem, @NotNull Object toInstance) throws BindingException;

  // void writeItems(List<? extends WritableItem> items, IJsonWritingContext context);

  // Collection<? extends WritableItem> getItemsToWrite(Collection<? extends Object> items);

  // void writeItem(Object instance, IJsonWritingContext context);

}
