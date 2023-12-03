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
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;

import java.lang.reflect.Field;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class InstanceModelAssemblyComplex
    extends AbstractBoundInstanceModelJavaField<BoundAssembly>
    implements IBoundInstanceModelAssembly {
  @NonNull
  private final IBoundDefinitionAssembly definition;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final BindingInstanceAssembly binding;

  public InstanceModelAssemblyComplex(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionAssembly definition,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(javaField, BoundAssembly.class, containingDefinition);
    this.definition = definition;
    this.groupAs = IGroupAs.of(getAnnotation().groupAs(), containingDefinition);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            getField().getName(),
            containingDefinition.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              getField().getName(),
              containingDefinition.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
    this.binding = new BindingInstanceAssembly();
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public BindingInstanceAssembly getInstanceBinding() {
    return binding;
  }

  @Override
  public IBoundDefinitionAssembly getDefinition() {
    return definition;
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

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getInstanceBinding().getCollectionInfo().getItemsFromValue(value);
  }

  private class BindingInstanceAssembly
      extends AbstractBindingInstanceModel
      implements IBindingInstanceModelAssembly,
      IFeatureComplexItemValueHandler {

    @Override
    public Field getField() {
      return InstanceModelAssemblyComplex.this.getField();
    }

    @Override
    public InstanceModelAssemblyComplex getInstance() {
      return InstanceModelAssemblyComplex.this;
    }

    @Override
    @Nullable
    public String getJsonKeyFlagName() {
      return getInstance().getJsonKeyFlagName();
    }

    @Override
    public IBoundInstanceFlag getItemJsonKey(Object item) {
      return JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())
          ? getDefinition().getJsonKeyFlagInstance()
          : null;
    }

    @Override
    public Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
      return getDefinition().getDefinitionBinding().deepCopyItem(item, parentInstance);
    }

    @Override
    public IBoundDefinitionAssembly getDefinition() {
      return InstanceModelAssemblyComplex.this.getDefinition();
    }

    @Override
    public Class<?> getBoundClass() {
      return getDefinition().getBoundClass();
    }

    @Override
    public void callBeforeDeserialize(Object targetObject, Object parentObject) throws BindingException {
      getDefinition().getDefinitionBinding().callBeforeDeserialize(targetObject, parentObject);
    }

    @Override
    public void callAfterDeserialize(Object targetObject, Object parentObject) throws BindingException {
      getDefinition().getDefinitionBinding().callAfterDeserialize(targetObject, parentObject);
    }
  }
}
