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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;

import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IChoiceGroupInstance
    extends IModelInstanceAbsolute, IContainerModelGrouped {

  int DEFAULT_CHOICE_GROUP_GROUP_AS_MAX_OCCURS = -1;
  @NonNull
  String DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME = "object-type";

  /**
   * {@inheritDoc}
   *
   * @see #DEFAULT_CHOICE_GROUP_GROUP_AS_MAX_OCCURS
   */
  @Override
  default int getMaxOccurs() {
    return DEFAULT_CHOICE_GROUP_GROUP_AS_MAX_OCCURS;
  }

  /**
   * Retrieve the Metaschema assembly definition on which this instance is
   * declared.
   *
   * @return the parent Metaschema assembly definition
   */
  @Override
  default IAssemblyDefinition getContainingDefinition() {
    return getOwningDefinition();
  }

  /**
   * Provides the Metaschema model type of "CHOICE".
   *
   * @return the model type
   */
  @Override
  default ModelType getModelType() {
    return ModelType.CHOICE_GROUP;
  }

  /**
   * Get the JSON property to use to discriminate between JSON objects.
   *
   * @return the discriminator property
   * @see #DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME
   */
  @NonNull
  String getJsonDiscriminatorProperty();

  @Override
  default boolean isEffectiveValueWrappedInXml() {
    return true;
  }

  /**
   * Get the effective name of the JSON key flag, if a JSON key is configured.
   * <p>
   * This name is expected to be in the same namespace as the containing model
   * element (i.e. choice group, assembly, field).
   *
   * @return the name of the JSON key flag if configured, or {@code null}
   *         otherwise
   */
  @Nullable
  String getJsonKeyFlagInstanceName();

  /**
   * Get the named model instance for the provided choice group item.
   *
   * @param item
   *          the item to get the instance for
   * @return the named model instance for the provided choice group item
   */
  @NonNull
  default INamedModelInstanceGrouped getItemInstance(@NonNull Object item) {
    throw new UnsupportedOperationException("no value");
  }

  @Override
  default MarkupMultiline getRemarks() {
    // no remarks
    return null;
  }

  @SuppressWarnings("null")
  @Override
  default String toCoordinates() {
    return String.format("%s-instance:%s:%s/%s@%d",
        getModelType().toString().toLowerCase(Locale.ROOT),
        getContainingDefinition().getContainingModule().getShortName(),
        getContainingDefinition().getName(),
        getGroupAsName(),
        hashCode());
  }
}
