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
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.info.IPropertyCollector;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Represents a property on a Java class that is bound to a Metaschema object (i.e., Assembly,
 * Field, or Flag). A property has a field and associated getter and setter methods.
 * <p>
 * Properties are bound to constructs in XML and JSON/YAML formats.
 * <p>
 * In XML key characteristics of a property binding include:
 * <ul>
 * <li>The name and namespace of the property which will be used for the associated element or
 * attribute.
 * </ul>
 * <p>
 * In JSON/YAML key characteristics of a property binding include:
 * <ul>
 * <li>A string-based key representing the field name of a JSON/YAML object
 * </ul>
 */
public interface IBoundInstance extends IInstance {

  /**
   * Get the {@link IClassBinding} for the Java class within which this property exists.
   * 
   * @return the containing class's binding
   */
  @NotNull
  IClassBinding getParentClassBinding();

  /**
   * Get the associated property's item type.
   * 
   * @return the item type
   */
  @NotNull
  Type getType();

  /**
   * Get the general type of the declared class.
   * 
   * @return the raw type of the property
   */
  @NotNull
  Class<?> getRawType();

  /**
   * Get the type of the bound object.
   * 
   * @return the raw type of the bound object
   */
  @NotNull
  Class<?> getItemType();

  /**
   * Gets the bound Java field associated with this property.
   * 
   * @return the Java field
   */
  @NotNull
  Field getField();

  /**
   * Returns the property name, not the field or method name.
   * 
   * @return the name in the pattern "somePropertyName"
   */
  @NotNull
  String getJavaPropertyName();

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
  void setValue(@NotNull Object parentInstance, Object value);

  /**
   * Get the current value from the provided object. The provided object must be of the item's type
   * associated with this property.
   * 
   * @param parentInstance
   *          the object
   * @return the value if set, or {@code null} otherwise
   */
  Object getValue(@NotNull Object parentInstance);

  @NotNull
  IPropertyCollector newPropertyCollector();

  /**
   * Read JSON data associated with this property and apply it to the provided {@code objectInstance} on which
   * this property exists.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value being parsed.
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
  boolean read(@NotNull Object objectInstance, @NotNull IJsonParsingContext context) throws IOException;

  /**
   * Read JSON data associated with this property and return it.
   * <p>
   * The parser's current token is expected to be the {@link JsonToken#FIELD_NAME} for the field value being parsed.
   * <p>
   * After parsing the parser's current token will be the next token after the field's value.
   * 
   * @param context
   *          the JSON parsing context
   * @return the instance value or {@code null} if no data was available to read
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  Object read(@NotNull IJsonParsingContext context) throws IOException;

  /**
   * Read the XML data associated with this property and apply it to the provided {@code objectInstance} on
   * which this property exists.
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
  boolean read(@NotNull Object objectInstance, @NotNull StartElement parent, @NotNull IXmlParsingContext context)
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

  boolean write(@NotNull Object parentInstance, @NotNull QName parentName, @NotNull IXmlWritingContext context)
      throws XMLStreamException, IOException;

  void write(@NotNull Object parentInstance, @NotNull IJsonWritingContext context) throws IOException;

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
  void copyBoundObject(@NotNull Object fromInstance, @NotNull Object toInstance) throws BindingException;
}
