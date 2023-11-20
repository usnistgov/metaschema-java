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

package gov.nist.secauto.metaschema.databind.model.info;

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IItemValueHandler {
  /**
   * Indicate if the item supported by this handler allows values without an XML
   * element wrapper.
   * <p>
   * Implementations may proxy this request to the JavaTypeAdapter if it is used
   * or return {@code false} otherwise.
   *
   * @return {@code true} if the underlying data type is allowed to be unwrapped,
   *         or {@code false} otherwise
   */
  boolean isUnwrappedValueAllowedInXml();

  /**
   * Parse and return the set of items from the JSON stream.
   * <p>
   * An item is a complete value, which can be a {@link JsonToken#START_OBJECT},
   * or a value token.
   *
   * @param parent
   *          the parent Java object to use for serialization callbacks, or
   *          {@code null} if there is no parent
   * @param context
   *          the JSON/YAML parser
   * @return the Java object representing the parsed item(s)
   * @throws IOException
   *           if an error occurred while parsing
   */
  @NonNull
  Object readItem(
      @Nullable Object parent,
      @NonNull IJsonParsingContext context) throws IOException;

  /**
   * Parse and return the set of items from the XML stream.
   *
   * @param parent
   *          the parent Java object to use for serialization callbacks
   * @param parentName
   *          the name of the parent (containing) element
   * @param context
   *          the XML writing context
   * @return the Java object representing the parsed item(s)
   * @throws IOException
   *           if an error occurred while writing
   * @throws XMLStreamException
   *           if an error occurred while generating the XML
   */
  @NonNull
  Object readItem(
      @NonNull Object parent,
      @NonNull StartElement parentName,
      @NonNull IXmlParsingContext context) throws IOException, XMLStreamException;

  /**
   * Write the provided {@code targetObject} as JSON.
   *
   * @param item
   *          the data to write
   * @param context
   *          the JSON writing context
   * @param jsonKey
   *          the JSON key to use or {@code null} if no JSON key is configured
   * @throws IOException
   *           if an error occurred while writing
   */
  void writeItem(
      @NonNull Object item,
      @NonNull IJsonWritingContext context,
      @Nullable IBoundFlagInstance jsonKey) throws IOException;

  /**
   * Write the provided value as XML.
   *
   * @param item
   *          the item to write
   * @param parentName
   *          the name of the parent (containing) element
   * @param context
   *          the JSON serializer
   * @throws IOException
   *           if an error occurred while writing
   * @throws XMLStreamException
   *           if an error occurred while generating the XML
   */
  void writeItem(
      @NonNull Object item,
      @NonNull QName parentName,
      @NonNull IXmlWritingContext context) throws IOException, XMLStreamException;

  /**
   * Create and return a deep copy of the provided item.
   *
   * @param item
   *          the item to copy
   * @param parentInstance
   *          an optional parent object to use for serialization callbacks
   * @return the new deep copy
   * @throws BindingException
   *           if an error occurred while analyzing the bound objects
   */
  @NonNull
  Object deepCopyItem(@NonNull Object item, @Nullable Object parentInstance) throws BindingException;
}
