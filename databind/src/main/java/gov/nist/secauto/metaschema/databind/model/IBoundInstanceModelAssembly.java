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

import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceModelAssemblyComplex;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents an assembly instance bound to Java field.
 */
public interface IBoundInstanceModelAssembly
    extends IBoundInstanceModelNamed<IBoundObject>, IAssemblyInstanceAbsolute, IFeatureComplexItemValueHandler {
  /**
   * Create a new bound assembly instance.
   *
   * @param field
   *          the Java field the instance is bound to
   * @param containingDefinition
   *          the definition containing the instance
   * @return the new instance
   */
  @NonNull
  static IBoundInstanceModelAssembly newInstance(
      @NonNull Field field,
      @NonNull IBoundDefinitionModelAssembly containingDefinition) {
    Class<? extends IBoundObject> itemType = IBoundInstanceModel.getItemType(field, IBoundObject.class);
    IBindingContext bindingContext = containingDefinition.getBindingContext();
    IBoundDefinitionModel<?> definition = bindingContext.getBoundDefinitionForClass(itemType);
    if (definition instanceof IBoundDefinitionModelAssembly) {
      return InstanceModelAssemblyComplex.newInstance(
          field,
          (IBoundDefinitionModelAssembly) definition,
          containingDefinition);
    }

    throw new IllegalStateException(String.format(
        "The field '%s' on class '%s' is not bound to a Metaschema assembly",
        field.toString(),
        field.getDeclaringClass().getName()));
  }

  @Override
  @NonNull
  IBoundDefinitionModelAssembly getDefinition();
  // @Override
  // default Object getValue(Object parent) {
  // return IBoundInstanceModelNamed.super.getValue(parent);
  // }

  // @Override
  // default void setValue(Object parentObject, Object value) {
  // IBoundInstanceModelNamed.super.setValue(parentObject, value);
  // }

  @Override
  default IBoundObject readItem(IBoundObject parent, IItemReadHandler handler) throws IOException {
    return handler.readItemAssembly(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default void writeItem(IBoundObject item, IItemWriteHandler handler) throws IOException {
    handler.writeItemAssembly(item, this);
  }

  @Override
  default IBoundObject deepCopyItem(IBoundObject item, IBoundObject parentInstance) throws BindingException {
    return getDefinition().deepCopyItem(item, parentInstance);
  }

  @Override
  default Class<? extends IBoundObject> getBoundClass() {
    return getDefinition().getBoundClass();
  }

  @Override
  default void callBeforeDeserialize(IBoundObject targetObject, IBoundObject parentObject) throws BindingException {
    getDefinition().callBeforeDeserialize(targetObject, parentObject);
  }

  @Override
  default void callAfterDeserialize(IBoundObject targetObject, IBoundObject parentObject) throws BindingException {
    getDefinition().callAfterDeserialize(targetObject, parentObject);
  }
}
