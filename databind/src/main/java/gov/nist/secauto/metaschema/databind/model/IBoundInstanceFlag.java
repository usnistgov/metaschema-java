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

import gov.nist.secauto.metaschema.core.model.IFeatureDefinitionInstanceInlined;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.impl.InstanceFlagInline;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents a flag instance bound to Java data.
 */
public interface IBoundInstanceFlag
    extends IFlagInstance, IBoundDefinitionFlag,
    IFeatureScalarItemValueHandler,
    IBoundInstance, IFeatureDefinitionInstanceInlined<IBoundDefinitionFlag, IBoundInstanceFlag> {

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

  /**
   * Determines if this flag's value is used as the property name for the JSON
   * object that holds the remaining data based on this flag's containing
   * definition.
   *
   * @return {@code true} if this flag is used as a JSON key, or {@code false}
   *         otherwise
   */
  boolean isJsonKey();

  /**
   * Determines if this flag is used as a JSON "value key". A "value key" is a
   * flag who's value is used as the property name for the containing objects
   * value.
   *
   * @return {@code true} if the flag is used as a JSON "value key", or
   *         {@code false} otherwise
   */
  boolean isJsonValueKey();

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

  /**
   * {@inheritDoc}
   * <p>
   * For an inline instance, this instance is the definition.
   */
  @Override
  @NonNull
  IBoundDefinitionFlag getDefinition();

  @Override
  @NonNull
  default IBoundInstanceFlag getInlineInstance() {
    // always inline
    return this;
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
  default Object readItem(Object parent, @NonNull IItemReadHandler handler) throws IOException {
    return handler.readItemFlag(ObjectUtils.requireNonNull(parent, "parent"), this);
  }

  @Override
  default void writeItem(Object item, IItemWriteHandler handler) throws IOException {
    handler.writeItemFlag(item, this);
  }

  @Override
  default boolean canHandleXmlQName(@NonNull QName qname) {
    return qname.equals(getXmlQName());
  }
}
