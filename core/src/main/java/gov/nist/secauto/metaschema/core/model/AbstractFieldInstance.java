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

/**
 * A base class for a field that is a member of a containing model.
 *
 * @param <PARENT>
 *          the Java type of the parent model (i.e., assembly, choice,
 *          choiceGroup).
 * @param <DEFINITION>
 *          the Java type of the definition for this member field
 * @param <INSTANCE>
 *          the Java type of the instance for this member field
 * @param <PARENT_DEFINITION>
 *          the Java type of the containing assembly definition
 */
public abstract class AbstractFieldInstance<
    PARENT extends IContainerModel,
    DEFINITION extends IFieldDefinition,
    INSTANCE extends IFieldInstance,
    PARENT_DEFINITION extends IAssemblyDefinition>
    extends AbstractNamedModelInstance<PARENT, PARENT_DEFINITION>
    implements IFieldInstance, IFeatureDefinitionReferenceInstance<DEFINITION, INSTANCE> {

  /**
   * Construct a new field instance.
   *
   * @param parent
   *          the parent model containing this instance
   */
  protected AbstractFieldInstance(@NonNull PARENT parent) {
    super(parent);
  }

  @Override
  public DEFINITION getDefinition() {
    // this should always be not null
    return ObjectUtils.asType(ObjectUtils.requireNonNull(
        getContainingModule()
            .getScopedFieldDefinitionByName(getReferencedDefinitionQName())));
  }

  /**
   * Generates a "coordinate" string for the assembly instance.
   *
   * @return the coordinate
   */
  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    IDefinition definition = getDefinition();
    return String.format("field instance %s -> %s in module %s (@%d(%d)",
        getXmlQName(),
        definition.getDefinitionQName(),
        getContainingDefinition().getContainingModule().getShortName(),
        hashCode(),
        definition.hashCode());
  }
}
