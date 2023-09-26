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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

// TODO: get rid of functional interfaces
public interface IDataTypeHandler {
  @NonNull
  static IDataTypeHandler newDataTypeHandler(
      @NonNull IBoundNamedModelInstance targetInstance,
      @NonNull IClassBinding classBinding) {
    return new ClassDataTypeHandler(targetInstance, classBinding);
  }

  @NonNull
  static IDataTypeHandler newDataTypeHandler(
      @NonNull IClassBinding classBinding) {
    return new ClassDataTypeHandler(null, classBinding);
  }

  @NonNull
  static IDataTypeHandler newDataTypeHandler(
      @NonNull IBoundFieldInstance property) {
    return new JavaTypeAdapterDataTypeHandler(property);
  }

  /**
   * Get the class binding associated with this handler.
   *
   * @return the class binding or {@code null} if the property's item type is not
   *         a bound class
   */
  IClassBinding getClassBinding();

  /**
   * Get the associated {@link IDataTypeAdapter}, if the data type is not a
   * complex bound object.
   *
   * @return the adpater, or {@code null} otherwise
   */
  IDataTypeAdapter<?> getJavaTypeAdapter();

  /**
   * Indicate if the value supported by this handler allows values without an XML
   * element wrapper.
   * <p>
   * Implementations may proxy this request to the JavaTypeAdapter if it is used
   * or return {@code false} otherwise.
   *
   * @return {@code true} if the underlying data type is allowed to be unwrapped,
   *         or {@code false} otherwise
   */
  boolean isUnwrappedValueAllowedInXml();

  boolean isJsonKeyRequired();

  /**
   * Parse and return the set of items from the JSON stream.
   * <p>
   * An item is a complete value, which can be a {@link JsonToken#START_OBJECT},
   * or a value token.
   *
   * @param <T>
   *          the Java type of the bound object described by this class
   * @param parentObject
   *          the parent Java object to use for serialization callbacks, or
   *          {@code null} if there is no parent
   * @param context
   *          the JSON/YAML parser
   * @return the Java object representing the set of parsed items
   * @throws IOException
   *           if an error occurred while parsing
   */
  @NonNull
  <T> T readItem(
      @Nullable Object parentObject,
      @NonNull IJsonParsingContext context) throws IOException;

  /**
   * Parse and return the set of items from the XML stream.
   *
   * @param parentObject
   *          the parent Java object to use for serialization callbacks
   * @param parentName
   *          the name of the parent (containing) element
   * @param context
   *          the XML writing context
   * @return the Java object representing the set of parsed items
   * @throws IOException
   *           if an error occurred while writing
   * @throws XMLStreamException
   *           if an error occurred while generating the XML
   */
  @NonNull
  Object readItem(
      @NonNull Object parentObject,
      @NonNull StartElement parentName,
      @NonNull IXmlParsingContext context) throws IOException, XMLStreamException;

  /**
   * Write the provided {@code targetObject} as JSON.
   *
   * @param targetObject
   *          the data to write
   * @param context
   *          the JSON writing context
   * @throws IOException
   *           if an error occurred while writing
   */
  void writeItem(
      @NonNull Object targetObject,
      @NonNull IJsonWritingContext context) throws IOException;

  /**
   * Write the provided value as XML.
   *
   * @param value
   *          the item to write
   * @param currentParentName
   *          the name of the parent (containing) element
   * @param context
   *          the JSON serializer
   * @throws IOException
   *           if an error occurred while writing
   * @throws XMLStreamException
   *           if an error occurred while generating the XML
   */
  void writeItem(
      @NonNull Object value,
      @NonNull QName currentParentName,
      @NonNull IXmlWritingContext context) throws IOException, XMLStreamException;

  /**
   * Build and return a deep copy of the provided item.
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
  Object copyItem(@NonNull Object item, @Nullable Object parentInstance) throws BindingException;

}
