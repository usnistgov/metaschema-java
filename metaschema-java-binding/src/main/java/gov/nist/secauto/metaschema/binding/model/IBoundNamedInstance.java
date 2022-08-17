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

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IBoundNamedInstance extends INamedInstance {

  @Override
  default String getName() {
    // delegate to the definition
    return getDefinition().getEffectiveName();
  }

  @Override
  default @NonNull Map<QName, Set<String>> getProperties() {
    // TODO: implement
    throw new UnsupportedOperationException();
  }

  /**
   * Get the {@link IClassBinding} for the Java class within which this property exists.
   * 
   * @return the containing class's binding
   */
  @NonNull
  IClassBinding getParentClassBinding();

  /**
   * Gets the bound Java field associated with this property.
   * 
   * @return the Java field
   */
  @NonNull
  Field getField();

  @NonNull
  default String getJavaFieldName() {
    return ObjectUtils.notNull(getField().getName());
  }

  /**
   * Get the actual Java type of the underlying bound object.
   * <p>
   * This may be the same as the what is returned by {@link #getItemType()}, or may be a Java
   * collection class.
   * 
   * @return the raw type of the bound object
   */
  @SuppressWarnings("null")
  @NonNull
  default Type getType() {
    return getField().getGenericType();
  }

  /**
   * Get the item type of the bound object. An item type is the primitive or specialized type that
   * represents that data associated with this binding.
   * 
   * @return the item type of the bound object
   */
  @NonNull
  default Class<?> getItemType() {
    return (Class<?>) getType();
  }

  /**
   * Get the current value from the provided {@code parentInstance} object. The provided object must
   * be of the type associated with the definition containing this property.
   * 
   * @param parentInstance
   *          the object associated with the definition containing this property
   * @return the value if available, or {@code null} otherwise
   */
  @Override
  default Object getValue(@NonNull Object parentInstance) {
    Field field = getField();
    boolean accessable = field.canAccess(parentInstance);
    field.setAccessible(true); // NOPMD - intentional
    Object retval;
    try {
      Object result = field.get(parentInstance);
      retval = result;
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new IllegalArgumentException(
          String.format("Unable to get the value of field '%s' in class '%s'.", field.getName(),
              field.getDeclaringClass().getName()),
          ex);
    } finally {
      field.setAccessible(accessable); // NOPMD - intentional
    }
    return retval;
  }

  /**
   * Set the provided value on the provided object. The provided object must be of the item's type
   * associated with this property.
   * 
   * @param parentInstance
   *          the object
   * @param value
   *          a value, which may be a simple {@link Type} or a {@link ParameterizedType} for a
   *          collection
   */
  default void setValue(@NonNull Object parentInstance, Object value) {
    Field field = getField();
    boolean accessable = field.canAccess(parentInstance);
    field.setAccessible(true); // NOPMD - intentional
    try {
      field.set(parentInstance, value);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new IllegalArgumentException(
          String.format("Unable to set the value of field '%s' in class '%s'.", field.getName(),
              field.getDeclaringClass().getName()),
          ex);
    } finally {
      field.setAccessible(accessable); // NOPMD - intentional
    }
  }

  @NonNull
  IPropertyCollector newPropertyCollector();

  /**
   * Read JSON data associated with this property and apply it to the provided {@code objectInstance}
   * on which this property exists.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value
   * being parsed.
   * <p>
   * After parsing the parser's current token will be the next token after the field's value.
   * 
   * @param objectInstance
   *          an instance of the class on which this property exists
   * @param context
   *          the JSON parsing context
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  boolean read(@NonNull Object objectInstance, @NonNull IJsonParsingContext context) throws IOException;

  /**
   * Read JSON data associated with this property and return it.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value
   * being parsed.
   * <p>
   * After parsing the parser's current token will be the next token after the field's value.
   * 
   * @param context
   *          the JSON parsing context
   * @return the instance value or {@code null} if no data was available to read
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  Object read(@NonNull IJsonParsingContext context) throws IOException;

  /**
   * Read the XML data associated with this property and apply it to the provided
   * {@code objectInstance} on which this property exists.
   * 
   * @param objectInstance
   *          an instance of the class on which this property exists
   * @param parent
   *          the containing XML element that was previously parsed
   * @param context
   *          the XML parsing context
   * @return {@code true} if the property was parsed, or {@code false} if the data did not contain
   *         information for this property
   * @throws IOException
   *           if there was an error when reading XML data
   * @throws XMLStreamException
   *           if there was an error generating an {@link XMLEvent} from the XML
   */
  boolean read(@NonNull Object objectInstance, @NonNull StartElement parent, @NonNull IXmlParsingContext context)
      throws IOException, XMLStreamException;

  // /**
  // * Get a supplier that can continually parse the underlying stream loading multiple values.
  // *
  // * @param context
  // * @return
  // * @throws BindingException
  // */
  // IJsonBindingSupplier getJsonItemSupplier(IBindingContext context)
  // throws BindingException;
  //
  // /**
  // * Get a supplier that can continually parse the underlying stream loading multiple values.
  // *
  // * @param context
  // * @return
  // * @throws BindingException
  // */
  // IXmlBindingSupplier getXmlItemSupplier(IBindingContext context)
  // throws BindingException;

  boolean write(@NonNull Object parentInstance, @NonNull QName parentName, @NonNull IXmlWritingContext context)
      throws XMLStreamException, IOException;

  void write(@NonNull Object parentInstance, @NonNull IJsonWritingContext context) throws IOException;

  /**
   * Copy this instance from one parent object to another.
   * 
   * @param fromInstance
   *          the object to copy from
   * @param toInstance
   *          the object to copy to
   * @throws BindingException
   *           if an error occurred while processing the object bindings
   */
  void copyBoundObject(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException;
}
