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

import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an assembly or field instance bound to Java data.
 *
 * @param <ITEM>
 *          the Java type for associated bound objects
 */
public interface IBoundInstanceModel<ITEM>
    extends IBoundInstance<ITEM>, IModelInstanceAbsolute {
  /**
   * Get the collection Java item type for the Java field associated with this
   * instance.
   *
   * @param field
   *          the Java field for the instance
   * @return the Java item type
   */
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

  /**
   * Get the collection Java item type for the Java field associated with this
   * instance.
   *
   * @param <TYPE>
   *          the item's expected Java type
   * @param field
   *          the Java field for the instance
   * @param expectedItemType
   *          the item's expected Java type, which may be a super type of the
   *          item's type
   * @return the Java item type
   */
  @NonNull
  static <TYPE> Class<? extends TYPE> getItemType(@NonNull Field field, @NonNull Class<TYPE> expectedItemType) {
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
    return ObjectUtils.notNull(itemType.asSubclass(expectedItemType));
  }

  @Override
  @NonNull
  IBoundDefinitionModelAssembly getContainingDefinition();

  @Override
  default Object getResolvedDefaultValue() {
    return getMaxOccurs() == 1 ? getEffectiveDefaultValue() : getCollectionInfo().emptyValue();
  }

  /**
   * Get the item values associated with the provided value.
   *
   * @param value
   *          the value which may be a singleton or a collection
   * @return the ordered collection of values
   */
  @Override
  @NonNull
  default Collection<? extends Object> getItemValues(Object value) {
    return getCollectionInfo().getItemsFromValue(value);
  }

  /**
   * Get the Java binding information for the collection type supported by this
   * instance.
   *
   * @return the collection Java binding information
   */
  @NonNull
  IModelInstanceCollectionInfo<ITEM> getCollectionInfo();

  /**
   * Get the JSON key flag for the provided item.
   *
   * @param item
   *          the item to get the JSON key flag for
   * @return the JSON key flag
   */
  @Nullable
  IBoundInstanceFlag getItemJsonKey(@NonNull Object item);
}
