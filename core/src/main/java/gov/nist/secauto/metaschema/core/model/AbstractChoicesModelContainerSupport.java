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

import java.util.List;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractChoicesModelContainerSupport<
    MI extends IModelInstance,
    NMI extends INamedModelInstance,
    FI extends IFieldInstance,
    AI extends IAssemblyInstance,
    CI extends IChoiceInstance,
    CG extends IChoiceGroupInstance>
    extends AbstractModelContainerSupport<MI, NMI, FI, AI, CI, CG> {

  @NonNull
  private final Class<CI> choiceClass;
  @NonNull
  private final Class<CG> choiceGroupClass;

  /**
   * Construct a new model container with support for choice and choice group
   * members.
   *
   * @param choiceClass
   *          the Java base class for choice members
   * @param choiceGroupClass
   *          the Java base class for choice group members
   */
  protected AbstractChoicesModelContainerSupport(
      @NonNull Class<CI> choiceClass,
      @NonNull Class<CG> choiceGroupClass) {
    this.choiceClass = choiceClass;
    this.choiceGroupClass = choiceGroupClass;
  }

  /**
   * Get a listing of all choice instances.
   *
   * @return the listing
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<CI> getChoiceInstances() {
    // this shouldn't get called all that often, so this is better than allocating
    // memory
    return ObjectUtils.notNull(getModelInstances().stream()
        .filter(obj -> choiceClass.isInstance(obj))
        .map(obj -> (CI) obj)
        .collect(Collectors.toUnmodifiableList()));
  }

  /**
   * Get a listing of all choice group instances.
   *
   * @return the listing
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<CG> getChoiceGroupInstances() {
    // this shouldn't get called all that often, so this is better than allocating
    // memory
    return ObjectUtils.notNull(getModelInstances().stream()
        .filter(obj -> choiceGroupClass.isInstance(obj))
        .map(obj -> (CG) obj)
        .collect(Collectors.toUnmodifiableList()));
  }
}
