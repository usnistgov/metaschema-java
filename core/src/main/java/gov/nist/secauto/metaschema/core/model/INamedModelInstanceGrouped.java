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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an arbitrary grouping of Metaschema model instances.
 */
public interface INamedModelInstanceGrouped extends INamedModelInstance {
  @Override
  IChoiceGroupInstance getParentContainer();

  @Override
  default IAssemblyDefinition getContainingDefinition() {
    return getParentContainer().getContainingDefinition();
  }

  /**
   * Get the discriminator JSON property name to use to identify the type of a
   * given instance object.
   *
   * @return the discriminator property name or {@code null} if the effective name
   *         should be used instead
   */
  @Nullable
  String getDiscriminatorValue();

  /**
   * Get the effective discriminator JSON property name to use to identify the
   * type of a given instance object.
   *
   * @return the discriminator property name
   */
  @NonNull
  default String getEffectiveDisciminatorValue() {
    String retval = getDiscriminatorValue();
    if (retval == null) {
      retval = getEffectiveName();
    }
    return retval;
  }

  @Override
  @Nullable
  default IFlagInstance getEffectiveJsonKey() {
    return JsonGroupAsBehavior.KEYED.equals(getParentContainer().getJsonGroupAsBehavior())
        ? ObjectUtils.requireNonNull(getJsonKey())
        : null;
  }

  @Override
  @Nullable
  default IFlagInstance getJsonKey() {
    String name = getParentContainer().getJsonKeyFlagInstanceName();
    return name == null
        ? null
        : ObjectUtils.requireNonNull(getDefinition().getFlagInstanceByName(getContainingModule().toFlagQName(name)));
  }

  @Override
  default int getMinOccurs() {
    return getParentContainer().getMinOccurs();
  }

  @Override
  default int getMaxOccurs() {
    return getParentContainer().getMaxOccurs();
  }

}
