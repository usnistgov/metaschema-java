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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

// REFACTOR: parameterize the item type?
public interface IModelInstanceCollectionInfo<ITEM> {

  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <T> IModelInstanceCollectionInfo<T> of(
      @NonNull IBoundInstanceModel<T> instance) {

    // create the collection info
    Type type = instance.getType();
    Field field = instance.getField();

    IModelInstanceCollectionInfo<T> retval;
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
        retval = new MapCollectionInfo<>(instance);
      } else {
        if (!List.class.isAssignableFrom(rawType)) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              field.getName(),
              field.getDeclaringClass().getName(),
              field.getType().getName(),
              List.class.getName()));
        }
        retval = new ListCollectionInfo<>(instance);
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
      retval = new SingletonCollectionInfo<>(instance);
    }
    return retval;
  }

  /**
   * Get the associated instance binding for which this info is for.
   *
   * @return the instance binding
   */
  @NonNull
  IBoundInstanceModel<ITEM> getInstance();

  /**
   * Get the number of items associated with the value.
   *
   * @param value
   *          the value to identify items for
   * @return the number of items, which will be {@code 0} if value is {@code null}
   */
  int size(@Nullable Object value);

  /**
   * Determine if the value is empty.
   *
   * @param value
   *          the value representing a collection
   * @return {@code true} if the value represents a collection with no items or
   *         {@code false} otherwise
   */
  boolean isEmpty(@Nullable Object value);

  /**
   * Get the type of the bound object.
   *
   * @return the raw type of the bound object
   */
  @NonNull
  Class<? extends ITEM> getItemType();

  @NonNull
  default Collection<? extends ITEM> getItemsFromParentInstance(@NonNull Object parentInstance) {
    Object value = getInstance().getValue(parentInstance);
    return getItemsFromValue(value);
  }

  @NonNull
  Collection<? extends ITEM> getItemsFromValue(Object value);

  Object emptyValue();

  Object deepCopyItems(@NonNull IBoundObject fromObject, @NonNull IBoundObject toObject) throws BindingException;

  /**
   * Read the value data for the model instance.
   * <p>
   * This method will return a value based on the instance's value type.
   *
   * @param handler
   *          the item parsing handler
   * @return the item collection object
   * @throws IOException
   *           if there was an error when reading the data
   */
  @NonNull
  Object readItems(@NonNull IModelInstanceReadHandler<ITEM> handler) throws IOException;

  void writeItems(
      @NonNull IModelInstanceWriteHandler<ITEM> handler,
      @NonNull Object value) throws IOException;
}
