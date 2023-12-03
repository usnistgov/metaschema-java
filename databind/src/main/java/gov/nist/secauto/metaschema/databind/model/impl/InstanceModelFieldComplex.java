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

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;

import java.lang.reflect.Field;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class InstanceModelFieldComplex
    extends AbstractBoundInstanceField {
  @NonNull
  private final DefinitionField definition;
  @NonNull
  private final Lazy<Object> defaultValue;
  @NonNull
  private final BindingInstanceField binding;

  public InstanceModelFieldComplex(
      @NonNull Field javaField,
      @NonNull DefinitionField definition,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(javaField, containingDefinition);
    this.definition = definition;

    if (!isInXmlWrapped()) {
      if (!definition.isSimple()) { // NOPMD efficiency
        throw new IllegalStateException(
            String.format("Field '%s' on class '%s' is requested to be unwrapped, but it has flags preventing this.",
                javaField.getName(),
                containingDefinition.getBoundClass().getName()));
      } else if (!getDefinition().getJavaTypeAdapter().isUnrappedValueAllowedInXml()) {
        throw new IllegalStateException(
            String.format(
                "Field '%s' on class '%s' is requested to be unwrapped, but its data type '%s' does not allow this.",
                javaField.getName(),
                containingDefinition.getBoundClass().getName(),
                getDefinition().getJavaTypeAdapter().getPreferredName()));
      }
    }
    this.defaultValue = ObjectUtils.notNull(Lazy.lazy(() -> {
      Object retval = null;
      if (getMaxOccurs() == 1) {
        IBindingFieldValue fieldValue = definition.getFieldValueBinding();

        Object fieldValueDefault = fieldValue.getDefaultValue();
        if (fieldValueDefault != null) {
          retval = getInstanceBinding().newInstance();
          fieldValue.setValue(retval, fieldValueDefault);

          for (IBoundInstanceFlag flag : definition.getFlagInstances()) {
            Object flagDefault = flag.getEffectiveDefaultValue();
            if (flagDefault != null) {
              flag.getInstanceBinding().setValue(retval, flagDefault);
            }
          }
        }
      }
      return retval;
    }));
    this.binding = new BindingInstanceField();
  }

  @Override
  public BindingInstanceField getInstanceBinding() {
    return binding;
  }

  @Override
  public DefinitionField getDefinition() {
    return definition;
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getInstanceBinding().getCollectionInfo().getItemsFromValue(value);
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue.get();
  }

  private class BindingInstanceField
      extends AbstractBindingInstanceModel
      implements IBindingInstanceModelField,
      IFeatureComplexItemValueHandler {

    @Override
    public Field getField() {
      return InstanceModelFieldComplex.this.getField();
    }

    @Override
    public InstanceModelFieldComplex getInstance() {
      return InstanceModelFieldComplex.this;
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
    public DefinitionField getDefinition() {
      return InstanceModelFieldComplex.this.getDefinition();
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
