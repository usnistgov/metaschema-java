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

package gov.nist.secauto.metaschema.databind.model.oldmodel.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.oldmodel.IBoundGroupedNamedModelInstance;

import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractBoundGroupedNamedModelInstance<T extends IClassBinding>
    implements IBoundGroupedNamedModelInstance {
  @NonNull
  private final ChoiceGroupProperty containingChoice;
  @NonNull
  private final BoundChoiceGroup.ModelInstance annotation;

  @NonNull
  private final T boundClass;

  public AbstractBoundGroupedNamedModelInstance(
      @NonNull ChoiceGroupProperty containingChoice,
      @NonNull BoundChoiceGroup.ModelInstance annotation,
      @NonNull Class<T> expectedClassBindingType) {
    this.containingChoice = containingChoice;
    this.annotation = annotation;
    Class<?> type = annotation.type();

    IAssemblyClassBinding containingDefinition = containingChoice.getContainingDefinition();
    IClassBinding classBinding = containingDefinition.getBindingContext().getClassBindingStrategy(type);
    if (classBinding == null) {
      throw new IllegalStateException(
          String.format("Unable to get class binding for '%s' the choice-group in the assemnbly named '%s'.",
              type.getName(),
              containingDefinition.getName()));
    }
    if (!expectedClassBindingType.isInstance(classBinding)) {
      throw new IllegalStateException(
          String.format(
              "The class binding '%s' is not the expected '%s' in the choice-group in the assemnbly named '%s'.",
              classBinding.getClass().getName(),
              expectedClassBindingType.getName(),
              containingDefinition.getName()));
    }
    this.boundClass = ObjectUtils.notNull(expectedClassBindingType.cast(classBinding));
  }

  @Override
  public IAssemblyClassBinding getContainingDefinition() {
    return getParentContainer().getContainingDefinition();
  }

  @Override
  public IBoundChoiceGroupInstance getParentContainer() {
    return containingChoice;
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMinOccurs() {
    return 1;
  }

  @Override
  public int getMaxOccurs() {
    return 1;
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @NonNull
  private BoundChoiceGroup.ModelInstance getAnnotation() {
    return annotation;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAnnotation().description());
  }

  @Override
  public T getDefinition() {
    return boundClass;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    int value = getAnnotation().useIndex();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public String getXmlNamespace() {
    return ObjectUtils
        .notNull(ModelUtil.resolveNamespace(getAnnotation().namespace(), getContainingDefinition()));
  }

  @Override
  public String getDiscriminatorValue() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().discriminatorValue());
  }
}
