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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundGroupedAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BoundGroupedAssemblyInstance
    implements IBoundGroupedAssemblyInstance {
  @NonNull
  private final BoundGroupedAssembly annotation;
  @NonNull
  private final IBoundChoiceGroupInstance container;
  @NonNull
  private final IAssemblyClassBinding definition;

  public BoundGroupedAssemblyInstance(
      @NonNull BoundGroupedAssembly annotation,
      @NonNull IBoundChoiceGroupInstance container) {
    this.annotation = annotation;
    this.container = container;
    IClassBinding classBinding = ObjectUtils.requireNonNull(
        container.getBindingContext().getClassBinding(annotation.binding()),
        String.format("Bound class '%s' is not a '%s' on bound class '%s'",
            annotation.binding().getName(),
            BoundGroupedAssembly.class.getName(),
            container.getOwningDefinition().getBoundClass().getName()));
    if (classBinding instanceof IAssemblyClassBinding) {
      throw new IllegalArgumentException(
          String.format("Bound class '%s' is not a bound assembly on bound class '%s'",
              annotation.binding().getName(),
              IAssemblyClassBinding.class.getName(),
              container.getOwningDefinition().getBoundClass().getName()));
    }
    this.definition = (IAssemblyClassBinding) classBinding;
  }

  @Override
  public IBoundChoiceGroupInstance getParentContainer() {
    return container;
  }

  @Override
  public IAssemblyClassBinding getContainingDefinition() {
    return getParentContainer().getContainingDefinition();
  }

  /**
   * @return the annotation
   */
  protected BoundGroupedAssembly getAnnotation() {
    return annotation;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
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
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    int value = getAnnotation().useIndex();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public String getDiscriminatorValue() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().discriminatorValue());
  }

  @NonNull
  protected Class<?> getBoundClass() {
    return getAnnotation().binding();
  }

  @Override
  public IAssemblyClassBinding getDefinition() {
    return definition;
  }
}
