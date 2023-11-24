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

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This marker interface indicates that this object is an instance.
 */
public interface IInstance extends IModelElement {
  /**
   * Retrieve the Metaschema definition on which the instance was declared.
   *
   * @return the Metaschema definition on which the instance was declared
   */
  @NonNull
  IFlagContainer getContainingDefinition();

  /**
   * Get the parent model definition that serves as the container of this
   * instance.
   *
   * @return the container
   */
  @NonNull
  IContainer getParentContainer();

  @Override
  default IModule getContainingModule() {
    return getContainingDefinition().getContainingModule();
  }

  /**
   * Get the current value from the provided {@code parentInstance} object.
   * <p>
   * The provided object must be of the type associated with the definition
   * containing this instance.
   *
   * @param parentInstance
   *          the object associated with the definition containing this property
   * @return the value if available, or {@code null} otherwise
   */
  default Object getValue(@NonNull Object parentInstance) {
    return getDefaultValue();
  }

  default Object getEffectiveDefaultValue() {
    return getDefaultValue();
  }

  /**
   * Generates a "coordinate" string for the provided information element
   * instance.
   *
   * A coordinate consists of the element's:
   * <ul>
   * <li>containing Metaschema module's short name</li>
   * <li>model type</li>
   * <li>name</li>
   * <li>hash code</li>
   * <li>the hash code of the definition</li>
   * </ul>
   *
   * @return the coordinate
   */
  @SuppressWarnings("null")
  @Override
  default String toCoordinates() {
    IModule module = getContainingModule();

    // TODO: revisit this to add more context i.e. the containing definition
    return String.format("%s:%s", module.getShortName(), getModelType());
  }
}
