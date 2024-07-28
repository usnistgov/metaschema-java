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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IFieldInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedField;
import gov.nist.secauto.metaschema.databind.model.impl.DefinitionField;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceModelGroupedFieldComplex;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents a field model instance that is a member of a choice group
 * instance.
 */
public interface IBoundInstanceModelGroupedField
    extends IBoundInstanceModelGroupedNamed, IFieldInstanceGrouped {

  /**
   * Create a new field model instance instance that is a member of a choice group
   * instance.
   *
   * @param annotation
   *          the Java annotation the instance is bound to
   * @param container
   *          the choice group instance containing the instance
   * @return the new instance
   */
  @NonNull
  static IBoundInstanceModelGroupedField newInstance(
      @NonNull BoundGroupedField annotation,
      @NonNull IBoundInstanceModelChoiceGroup container) {
    Class<? extends IBoundObject> clazz = annotation.binding();
    IBindingContext bindingContext = container.getContainingDefinition().getBindingContext();
    IBoundDefinitionModel<?> definition = bindingContext.getBoundDefinitionForClass(clazz);

    if (!(definition instanceof DefinitionField)) {
      Field field = container.getField();
      throw new IllegalStateException(String.format(
          "The '%s' annotation, bound to '%s', field '%s' on class '%s' is not bound to a Metaschema field",
          annotation.getClass(),
          annotation.binding().getName(),
          field.toString(),
          field.getDeclaringClass().getName()));
    }
    return new InstanceModelGroupedFieldComplex(annotation, (DefinitionField) definition, container);
  }

  @Override
  IBoundDefinitionModelFieldComplex getDefinition();

  @Override
  default IBoundObject readItem(IBoundObject parent, @NonNull IItemReadHandler handler) throws IOException {
    return handler.readItemField(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default void writeItem(IBoundObject item, IItemWriteHandler handler) throws IOException {
    handler.writeItemField(item, this);
  }
}
