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
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;

import java.lang.reflect.Field;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ClassBindingAssemblyInstance
    extends AbstractBoundModelInstance
    implements IBoundAssemblyInstance, IFeatureComplexItemValueHandler {

  @NonNull
  private final BoundAssembly assembly;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final IAssemblyClassBinding definition;

  public ClassBindingAssemblyInstance(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding definition,
      @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);

    this.definition = definition;
    BoundAssembly boundAssembly = field.getAnnotation(BoundAssembly.class);
    if (boundAssembly == null) {
      throw new IllegalArgumentException(
          String.format("BoundField '%s' on class '%s' is missing the '%s' annotation.",
              field.getName(),
              parentClassBinding.getBoundClass().getName(),
              BoundAssembly.class.getName()));
    }
    this.assembly = boundAssembly;
    this.groupAs = IGroupAs.of(boundAssembly.groupAs(), parentClassBinding);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            field.getName(),
            parentClassBinding.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              field.getName(),
              parentClassBinding.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
  }

  @Override
  public IAssemblyClassBinding getDefinition() {
    return definition;
  }

  @Override
  public IAssemblyClassBinding getClassBinding() {
    return definition;
  }

  @Override
  public IBoundFlagInstance getJsonKey() {
    return JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())
        ? getClassBinding().getJsonKeyFlagInstance()
        : null;
  }

  @Override
  public Object getDefaultValue() {
    // none
    return null;
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @NonNull
  private BoundAssembly getAssemblyAnnotation() {
    return assembly;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getAssemblyAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAssemblyAnnotation().description());
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAssemblyAnnotation().remarks());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAssemblyAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    int value = getAssemblyAnnotation().useIndex();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public String getXmlNamespace() {
    return ObjectUtils
        .notNull(ModelUtil.resolveNamespace(getAssemblyAnnotation().namespace(), getContainingDefinition()));
  }

  @Override
  public final int getMinOccurs() {
    return getAssemblyAnnotation().minOccurs();
  }

  @Override
  public final int getMaxOccurs() {
    return getAssemblyAnnotation().maxOccurs();
  }

  @Override
  public String getGroupAsName() {
    return groupAs.getGroupAsName();
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return groupAs.getGroupAsXmlNamespace();
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return groupAs.getJsonGroupAsBehavior();
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return groupAs.getXmlGroupAsBehavior();
  }

  @Override
  public Object defaultValue() {
    return getCollectionInfo().emptyValue();
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getField().getDeclaringClass().getName(),
        getName());
  }

  // ---------------------------------------
  // - End annotation driven code - CPD-ON -
  // ---------------------------------------
}
