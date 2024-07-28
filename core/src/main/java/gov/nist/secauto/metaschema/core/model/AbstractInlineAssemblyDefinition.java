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
 * A base class for an assembly instance defined inline.
 *
 * @param <PARENT>
 *          the Java type of the parent model container for this instance
 * @param <DEFINITION>
 *          the Java type of the related assembly definition
 * @param <INSTANCE>
 *          the expected Java type of an instance of this definition
 * @param <PARENT_DEFINITION>
 *          the Java type of the containing assembly definition
 * @param <FLAG>
 *          the expected Java type of flag children
 * @param <MODEL>
 *          the expected Java type of model children
 * @param <NAMED_MODEL>
 *          the expected Java type of named model children
 * @param <FIELD>
 *          the expected Java type of field children
 * @param <ASSEMBLY>
 *          the expected Java type of assembly children
 * @param <CHOICE>
 *          the expected Java type of choice children
 * @param <CHOICE_GROUP>
 *          the expected Java type of choice group children
 */
public abstract class AbstractInlineAssemblyDefinition<
    PARENT extends IContainerModel,
    DEFINITION extends IAssemblyDefinition,
    INSTANCE extends IAssemblyInstance,
    PARENT_DEFINITION extends IAssemblyDefinition,
    FLAG extends IFlagInstance,
    MODEL extends IModelInstanceAbsolute,
    NAMED_MODEL extends INamedModelInstanceAbsolute,
    FIELD extends IFieldInstanceAbsolute,
    ASSEMBLY extends IAssemblyInstanceAbsolute,
    CHOICE extends IChoiceInstance,
    CHOICE_GROUP extends IChoiceGroupInstance>
    extends AbstractNamedModelInstance<PARENT, PARENT_DEFINITION>
    implements IAssemblyInstance, IAssemblyDefinition, IFeatureContainerFlag<FLAG>,
    IFeatureContainerModelAssembly<
        MODEL,
        NAMED_MODEL,
        FIELD,
        ASSEMBLY,
        CHOICE,
        CHOICE_GROUP>,
    IFeatureDefinitionInstanceInlined<DEFINITION, INSTANCE> {

  /**
   * Construct a new inline assembly definition.
   *
   * @param parent
   *          the parent model containing this instance
   */
  protected AbstractInlineAssemblyDefinition(@NonNull PARENT parent) {
    super(parent);
  }

  @Override
  public final DEFINITION getDefinition() {
    return ObjectUtils.asType(this);
  }

  @Override
  @NonNull
  public final INSTANCE getInlineInstance() {
    return ObjectUtils.asType(this);
  }

  @Override
  public final FLAG getJsonKey() {
    return IFeatureContainerFlag.super.getJsonKey();
  }
}
