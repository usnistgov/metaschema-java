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

import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

interface IBoundJavaCollectionField extends IBoundJavaField, IBoundNamedModelInstance {
  @Override
  @NonNull
  default IModelPropertyInfo newPropertyInfo() { // NOPMD - cyclomatic complexity is unavoidable
    // create the property info
    Type type = getField().getGenericType();

    IModelPropertyInfo retval;
    if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
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

      // collection case
      if (JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())) {
        if (!Map.class.isAssignableFrom(getRawType())) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              getField().getName(), getParentClassBinding().getBoundClass().getName(),
              getField().getType().getName(), Map.class.getName()));
        }
        retval = new MapPropertyInfo(this);
      } else {
        if (!List.class.isAssignableFrom(getRawType())) {
          throw new IllegalArgumentException(String.format(
              "The field '%s' on class '%s' has data type '%s', which is not the expected '%s' derived data type.",
              getField().getName(), getParentClassBinding().getBoundClass().getName(),
              getField().getType().getName(), List.class.getName()));
        }
        retval = new ListPropertyInfo(this);
      }
    } else {
      // single value case
      if (type instanceof ParameterizedType) {
        throw new IllegalStateException(String.format(
            "The field '%s' on class '%s' has a data parmeterized type of '%s',"
                + " but the occurance is not multi-valued.",
            getField().getName(), getParentClassBinding().getBoundClass().getName(), getField().getType().getName()));
      }
      retval = new SingletonPropertyInfo(this);
    }
    return retval;
  }

  @Override
  @NonNull
  IModelPropertyInfo getPropertyInfo();

  @Override
  default @NonNull Class<?> getItemType() {
    return getPropertyInfo().getItemType();
  }
}
