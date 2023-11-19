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

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

// REFACTOR: rename to IModelInstanceInfo
// REFACTOR: make all read and write methods take the value, not the parent instance as an argument
public interface IModelPropertyInfo {

  @NonNull
  static IModelPropertyInfo newPropertyInfo(
      @NonNull IBoundModelInstance instance) {
    // create the property info
    Type type = instance.getType();
    Field field = instance.getField();

    IModelPropertyInfo retval;
    if (instance.getMaxOccurs() == -1 || instance.getMaxOccurs() > 1) {
      // collection case
      JsonGroupAsBehavior jsonGroupAs = instance.getJsonGroupAsBehavior();

      // expect a ParameterizedType
      if (!(type instanceof ParameterizedType)) {
        switch (jsonGroupAs) {
        case KEYED:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  field.getName(),
                  field.getDeclaringClass().getName(),
                  field.getType().getName(), Map.class.getName()));
        case LIST:
        case SINGLETON_OR_LIST:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  field.getName(),
                  field.getDeclaringClass().getName(),
                  field.getType().getName(), List.class.getName()));
        default:
          // this should not occur
          throw new IllegalStateException(jsonGroupAs.name());
        }
      }

      Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
      if (JsonGroupAsBehavior.KEYED.equals(jsonGroupAs)) {
        if (!Map.class.isAssignableFrom(rawType)) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              field.getName(),
              field.getDeclaringClass().getName(),
              field.getType().getName(),
              Map.class.getName()));
        }
        retval = new MapPropertyInfo(instance);
      } else {
        if (!List.class.isAssignableFrom(rawType)) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              field.getName(),
              field.getDeclaringClass().getName(),
              field.getType().getName(),
              List.class.getName()));
        }
        retval = new ListPropertyInfo(instance);
      }
    } else {
      // single value case
      if (type instanceof ParameterizedType) {
        throw new IllegalStateException(String.format(
            "The field '%s' on class '%s' has a data parmeterized type of '%s',"
                + " but the occurance is not multi-valued.",
            field.getName(),
            field.getDeclaringClass().getName(),
            field.getType().getName()));
      }
      retval = new SingletonPropertyInfo(instance);
    }
    return retval;
  }

  /**
   * Get the associated property for which this info is for.
   *
   * @return the property
   */
  // REFACTOR: rename to getInstance
  @NonNull
  IBoundModelInstance getProperty();

  int getItemCount(@Nullable Object value);

  /**
   * Get the type of the bound object.
   *
   * @return the raw type of the bound object
   */
  @NonNull
  Class<?> getItemType();

  default boolean isJsonKeyRequired() {
    return false;
  }

  /**
   * Write a {@code value} that is not {@code null}.
   *
   * @param value
   *          the value to write
   * @param parentName
   *          the XML qualified name of the parent element to write
   * @param context
   *          the XML serialization context
   * @throws XMLStreamException
   *           if an error occurred while generating XML events
   * @throws IOException
   *           if an error occurred while writing to the output stream
   */
  void writeValues(@NonNull Object value, @NonNull QName parentName, @NonNull IXmlWritingContext context)
      throws XMLStreamException, IOException;

  void writeValues(@NonNull Object parentInstance, @NonNull IJsonWritingContext context) throws IOException;

  boolean isValueSet(@NonNull Object parentInstance) throws IOException;

  @NonNull
  default Collection<? extends Object> getItemsFromParentInstance(@NonNull Object parentInstance) {
    Object value = getProperty().getValue(parentInstance);
    return getItemsFromValue(value);
  }

  @NonNull
  Collection<? extends Object> getItemsFromValue(Object value);

  Object emptyValue();

  // REFACTOR: Align this method with deepCopy/deepCopyItem
  Object copy(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException;

  /**
   * Read the value data for the model instance.
   * <p>
   * This method will return a value based on the instance's value type.
   *
   * @param handler
   *          the item parsing handler
   * @param collector
   *          used to hold parsed values
   * @return {@code true} if data was read, or {@code false} otherwise
   * @throws IOException
   *           if there was an error when reading the data
   */
  @Nullable
  Object readItems(@NonNull IReadHandler handler) throws IOException;

  <T> void writeItems(
      @NonNull IWriteHandler handler,
      @NonNull Object value) throws IOException;

  public interface IReadHandler {
    @Nullable
    Object readSingleton() throws IOException;

    @NonNull
    List<?> readList() throws IOException;

    @NonNull
    Map<String, ?> readMap() throws IOException;

    /**
     * Read the next item in the collection of items represented by the property.
     *
     * @return the Java object representing the item, or {@code null} if no items
     *         remain to be read
     * @throws IOException
     *           if an error occurred while parsing the input
     */
    Object readItem() throws IOException;
  }

  public interface IWriteHandler {
    void writeSingleton(@NonNull Object item);

    void writeList(@NonNull List<?> items);

    void writeMap(@NonNull Map<String, ?> items);

    /**
     * Write the next item in the collection of items represented by the property.
     *
     * @param item
     *          the item Java object to write
     * @throws IOException
     *           if an error occurred while parsing the input
     */
    void writeItem(@NonNull Object item) throws IOException;
  }
}
