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
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a choice group instance bound to Java field.
 */
public interface IBoundInstanceModelChoiceGroup
    extends IBoundInstanceModel<IBoundObject>, IBoundContainerModelChoiceGroup, IChoiceGroupInstance {

  /**
   * Create a new bound choice group instance.
   *
   * @param field
   *          the Java field the instance is bound to
   * @param containingDefinition
   *          the definition containing the instance
   * @return the new instance
   */
  @NonNull
  static IBoundInstanceModelChoiceGroup newInstance(
      @NonNull Field field,
      @NonNull IBoundDefinitionModelAssembly containingDefinition) {
    return InstanceModelChoiceGroup.newInstance(field, containingDefinition);
  }

  @Override
  default String getJsonName() {
    // always the group-as name
    return ObjectUtils.requireNonNull(getGroupAsName());
  }

  @Override
  @NonNull
  IBoundDefinitionModelAssembly getOwningDefinition();

  @Override
  default IBoundDefinitionModelAssembly getContainingDefinition() {
    return getOwningDefinition();
  }

  /**
   * Get the bound grouped model instance associated with the provided Java class.
   *
   * @param clazz
   *          the Java class which should be bound to a grouped model instance
   * @return the grouped model instance or {code null} if no instance was bound to
   *         the requested class
   */
  @Nullable
  IBoundInstanceModelGroupedNamed getGroupedModelInstance(@NonNull Class<?> clazz);

  /**
   * Get the bound grouped model instance associated with the provided XML
   * qualified name.
   *
   * @param name
   *          the XML qualified name which should be bound to a grouped model
   *          instance
   * @return the grouped model instance or {code null} if no instance was bound to
   *         the requested XML qualified name
   */
  @Nullable
  IBoundInstanceModelGroupedNamed getGroupedModelInstance(@NonNull QName name);

  /**
   * Get the bound grouped model instance associated with the provided JSON
   * discriminator value.
   *
   * @param discriminator
   *          the JSON discriminator value which should be bound to a grouped
   *          model instance
   * @return the grouped model instance or {code null} if no instance was bound to
   *         the requested JSON discriminator value
   */
  @Nullable
  IBoundInstanceModelGroupedNamed getGroupedModelInstance(@NonNull String discriminator);

  /**
   * Get the bound grouped model instance associated with the provided item.
   *
   * @param item
   *          the item which should be bound to a grouped model instance
   * @return the grouped model instance or {code null} if no instance was bound to
   *         the requested item
   */
  @Override
  @NonNull
  default IBoundInstanceModelGroupedNamed getItemInstance(Object item) {
    return ObjectUtils.requireNonNull(getGroupedModelInstance(item.getClass()));
  }

  @Override
  default IBoundObject readItem(IBoundObject parent, IItemReadHandler handler) throws IOException {
    return handler.readChoiceGroupItem(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default void writeItem(IBoundObject item, IItemWriteHandler handler) throws IOException {
    handler.writeChoiceGroupItem(item, this);
  }

  @Override
  default IBoundObject deepCopyItem(IBoundObject item, IBoundObject parentInstance) throws BindingException {
    IBoundInstanceModelGroupedNamed itemInstance = getItemInstance(item);
    return itemInstance.deepCopyItem(item, parentInstance);
  }

  @Override
  default boolean canHandleXmlQName(@NonNull QName qname) {
    return getGroupedModelInstance(qname) != null;
  }
}
