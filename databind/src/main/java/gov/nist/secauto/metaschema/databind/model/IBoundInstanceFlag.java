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

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceFlagInline;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a flag instance bound to Java data.
 */
public interface IBoundInstanceFlag
    extends IFlagInstance, IBoundDefinitionFlag, IBoundModuleInstance,
    IFeatureScalarItemValueHandler,
    IFeatureJavaField {

  /**
   * Create a new bound flag instance.
   *
   * @param field
   *          the Java field the instance is bound to
   * @param containingDefinition
   *          the definition containing the instance
   * @return the new instance
   */
  @NonNull
  static IBoundInstanceFlag newInstance(
      @NonNull Field field,
      @NonNull IBoundDefinitionModel containingDefinition) {
    return new InstanceFlagInline(field, containingDefinition);
  }

  // Flag Instance Features
  // ======================

  @Override
  @NonNull
  IBoundDefinitionModel getContainingDefinition();

  @Override
  @NonNull
  default IBoundDefinitionModel getParentContainer() {
    return getContainingDefinition();
  }

  @Override
  @NonNull
  default IBoundInstanceFlag getInstance() {
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * For an inline instance, this instance is the definition.
   */
  @Override
  @NonNull
  default IBoundInstanceFlag getDefinition() {
    return this;
  }

  @Override
  default boolean isInline() {
    return true;
  }

  @Override
  @NonNull
  default IBoundInstanceFlag getInlineInstance() {
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This is an inline instance that is both a definition and an instance. Don't
   * delegate to the definition, since this would be redundant.
   */
  @Override
  @NonNull
  default String getEffectiveName() {
    String useName = getUseName();
    return useName == null ? getName() : useName;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Use the effective name of the instance.
   */
  @Override
  @NonNull
  default String getJsonName() {
    return IFlagInstance.super.getJsonName();
  }

  @Override
  @NonNull
  default IBoundModule getContainingModule() {
    return IBoundModuleInstance.super.getContainingModule();
  }

  /**
   * {@inheritDoc}
   * <p>
   * This is an inline instance that is both a definition and an instance. Don't
   * delegate to the definition, since this would be redundant.
   */
  @Override
  @Nullable
  default Object getEffectiveDefaultValue() {
    return getDefaultValue();
  }

  /**
   * Generates a "coordinate" string for the inline flag instance.
   *
   * The coordinates consist of the:
   * <ul>
   * <li>containing Metaschema module's short name</li>
   * <li>name</li>
   * <li>hash code</li>
   * </ul>
   *
   * @return the coordinate
   */
  @Override
  @NonNull
  default String toCoordinates() {
    IDefinition definition = getDefinition();
    return ObjectUtils.notNull(String.format("Inline Flag(%s:%s:%d)",
        getContainingDefinition().getContainingModule().getShortName(),
        definition.getName(),
        hashCode()));
  }

  /**
   * {@inheritDoc}
   * <p>
   * Always bound to a field.
   */
  @Override
  @Nullable
  default Object getValue(@NonNull Object parent) {
    return IFeatureJavaField.super.getValue(parent);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Always bound to a field.
   */
  @Override
  default void setValue(@NonNull Object parentObject, @Nullable Object value) {
    IFeatureJavaField.super.setValue(parentObject, value);
  }

  @Override
  default void deepCopy(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException {
    Object value = getValue(fromInstance);
    if (value != null) {
      setValue(toInstance, deepCopyItem(value, toInstance));
    }
  }

  @Override
  @NonNull
  default Object readItem(@Nullable Object parent, @NonNull IItemReadHandler handler) throws IOException {
    return handler.readItemFlag(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default boolean canHandleJsonPropertyName(@NonNull String name) {
    return name.equals(getJsonName());
  }

  @Override
  default boolean canHandleXmlQName(@NonNull QName qname) {
    return qname.equals(getXmlQName());
  }
}
