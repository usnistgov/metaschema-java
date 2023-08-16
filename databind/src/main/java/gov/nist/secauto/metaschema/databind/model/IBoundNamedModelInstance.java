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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This marker interface provides common methods for interacting with bound object values.
 */
public interface IBoundNamedModelInstance extends IBoundNamedInstance, INamedModelInstance {

  @NonNull
  static Class<?> getItemType(@NonNull Field field) {
    Type fieldType = field.getGenericType();
    Class<?> rawType = ObjectUtils.notNull(
        (Class<?>) (fieldType instanceof ParameterizedType ? ((ParameterizedType) fieldType).getRawType() : fieldType));

    Class<?> itemType;
    if (Map.class.isAssignableFrom(rawType)) {
      // this is a Map so the second generic type is the value
      itemType = ObjectUtils.notNull((Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[1]);
    } else if (List.class.isAssignableFrom(rawType)) {
      // this is a List so there is only a single generic type
      itemType = ObjectUtils.notNull((Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0]);
    } else {
      // non-collection
      itemType = rawType;
    }
    return itemType;
  }

  @Override
  default Class<?> getItemType() {
    return getPropertyInfo().getItemType();
  }

  @Override
  IAssemblyClassBinding getParentClassBinding();

  @Override
  IBoundModelDefinition getDefinition();

  @NonNull
  default IModelPropertyInfo newPropertyInfo(
      @NonNull Supplier<IDataTypeHandler> dataTypeHandlerSupplier) {
    // create the property info
    Type type = getType();

    IModelPropertyInfo retval;
    if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
      // collection case
      // expect a ParameterizedType
      if (!(type instanceof ParameterizedType)) {
        switch (getJsonGroupAsBehavior()) {
        case KEYED:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  getField().getName(), getParentClassBinding().getBoundClass().getName(),
                  getField().getType().getName(), Map.class.getName()));
        case LIST:
        case SINGLETON_OR_LIST:
          throw new IllegalStateException(
              String.format("The field '%s' on class '%s' has data type of '%s'," + " but should have a type of '%s'.",
                  getField().getName(), getParentClassBinding().getBoundClass().getName(),
                  getField().getType().getName(), List.class.getName()));
        default:
          // this should not occur
          throw new IllegalStateException(getJsonGroupAsBehavior().name());
        }
      }

      Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
      if (JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())) {
        if (!Map.class.isAssignableFrom(rawType)) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              getField().getName(), getParentClassBinding().getBoundClass().getName(),
              getField().getType().getName(), Map.class.getName()));
        }
        retval = new MapPropertyInfo(this, dataTypeHandlerSupplier);
      } else {
        if (!List.class.isAssignableFrom(rawType)) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              getField().getName(), getParentClassBinding().getBoundClass().getName(),
              getField().getType().getName(), List.class.getName()));
        }
        retval = new ListPropertyInfo(this, dataTypeHandlerSupplier);
      }
    } else {
      // single value case
      if (type instanceof ParameterizedType) {
        throw new IllegalStateException(String.format(
            "The field '%s' on class '%s' has a data parmeterized type of '%s',"
                + " but the occurance is not multi-valued.",
            getField().getName(), getParentClassBinding().getBoundClass().getName(), getField().getType().getName()));
      }
      retval = new SingletonPropertyInfo(this, dataTypeHandlerSupplier);
    }
    return retval;
  }

  @NonNull
  IModelPropertyInfo getPropertyInfo();

  /**
   * Get the item values associated with the provided value.
   *
   * @param value
   *          the value which may be a singleton or a collection
   * @return the ordered collection of values
   */
  @Override
  @NonNull
  Collection<? extends Object> getItemValues(Object value);

  @NonNull
  Object copyItem(@NonNull Object fromItem, @NonNull Object toInstance) throws BindingException;

  // void writeItems(List<? extends WritableItem> items, IJsonWritingContext
  // context);

  // Collection<? extends WritableItem> getItemsToWrite(Collection<? extends
  // Object> items);

  // void writeItem(Object instance, IJsonWritingContext context);
}
