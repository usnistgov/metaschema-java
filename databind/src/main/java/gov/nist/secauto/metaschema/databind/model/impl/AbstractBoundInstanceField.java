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
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractBoundInstanceField
    extends AbstractBoundInstanceModelJavaField<BoundField>
    implements IBoundInstanceModelField {
  @NonNull
  private final IGroupAs groupAs;

  protected AbstractBoundInstanceField(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(javaField, BoundField.class, containingDefinition);
    this.groupAs = IGroupAs.of(getAnnotation().groupAs(), containingDefinition);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            javaField.getName(),
            javaField.getDeclaringClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              javaField.getName(),
              javaField.getDeclaringClass().getName(),
              GroupAs.class.getName()));
    }
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public Object getValue(Object parent) {
    return getInstanceBinding().getValue(parent);
  }

  @Override
  public String getName() {
    return ObjectUtils.notNull(getField().getName());
  }

  @Override
  public IGroupAs getGroupAs() {
    return groupAs;
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
  public boolean isInXmlWrapped() {
    return getAnnotation().inXmlWrapped();
  }

  @Override
  public final int getMinOccurs() {
    return getAnnotation().minOccurs();
  }

  @Override
  public final int getMaxOccurs() {
    return getAnnotation().maxOccurs();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
  }
}
