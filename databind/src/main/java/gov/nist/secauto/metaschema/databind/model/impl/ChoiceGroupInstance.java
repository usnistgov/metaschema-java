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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundGroupedNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class ChoiceGroupInstance
    extends AbstractBoundModelInstance
    implements IBoundChoiceGroupInstance {

  @NonNull
  private final BoundChoiceGroup annotation;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final Lazy<ChoiceGroupModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<Map<Class<?>, IBoundGroupedNamedModelInstance>> classToInstanceMap;
  @NonNull
  private final Lazy<Map<QName, IBoundGroupedNamedModelInstance>> qnameToInstanceMap;

  public ChoiceGroupInstance(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding containingDefinition) {
    super(field, containingDefinition);

    BoundChoiceGroup annotation = field.getAnnotation(BoundChoiceGroup.class);
    if (annotation == null) {
      throw new IllegalArgumentException(
          String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
              field.getName(),
              containingDefinition.getBoundClass().getName(),
              BoundAssembly.class.getName()));
    }
    this.annotation = annotation;

    this.groupAs = IGroupAs.of(annotation.groupAs(), containingDefinition);
    if ((getMaxOccurs() == -1 || getMaxOccurs() > 1)) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            field.getName(),
            containingDefinition.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(this.groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              field.getName(),
              containingDefinition.getBoundClass().getName(),
              GroupAs.class.getName()));
    }

    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> new ChoiceGroupModelContainerSupport(
        annotation.assemblies(),
        annotation.fields(),
        this)));
    this.classToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getModelInstances().stream()
            .collect(Collectors.toMap(
                item -> (Class<?>) item.getDefinition().getBoundClass(),
                CustomCollectors.identity())))));
    this.qnameToInstanceMap = ObjectUtils.notNull(Lazy.lazy(() -> Collections.unmodifiableMap(
        getModelInstances().stream()
            .collect(Collectors.toMap(
                item -> item.getXmlQName(),
                CustomCollectors.identity())))));

  }

  @SuppressWarnings("null")
  @Override
  public Map<QName, IBoundGroupedNamedModelInstance> getQNameToInstanceMap() {
    return qnameToInstanceMap.get();
  }

  @SuppressWarnings("null")
  @Override
  public Map<Class<?>, IBoundGroupedNamedModelInstance> getClassToInstanceMap() {
    return classToInstanceMap.get();
  }

  @SuppressWarnings("null")
  @Override
  public ChoiceGroupModelContainerSupport getModelContainer() {
    return modelContainer.get();
  }

  @Override
  public IAssemblyClassBinding getOwningDefinition() {
    return getContainingDefinition();
  }

  @Override
  public Object getEffectiveDefaultValue() {
    return super.getCollectionInfo().emptyValue();
  }

  @Override
  public MarkupMultiline getRemarks() {
    // no remarks
    return null;
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @NonNull
  private BoundChoiceGroup getAnnotation() {
    return annotation;
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
  public String getJsonKeyFlagName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().jsonKey());
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    return getAnnotation().discriminator();
  }

  // ---------------------------------------
  // - End annotation driven code - CPD-ON -
  // ---------------------------------------

  @Override
  public boolean canHandleJsonPropertyName(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean canHandleXmlQName(QName qname) {
    return qnameToInstanceMap.get().containsKey(qname);
  }

  @Override
  public String getJsonName() {
    return getGroupAsName();
  }

  @Override
  public IBoundFlagInstance getItemJsonKey(Object item) {
    String jsonKeyFlagName = getJsonKeyFlagName();
    IBoundFlagInstance retval = null;

    if (jsonKeyFlagName != null) {
      Class<?> clazz = item.getClass();

      IClassBinding classBinding = ObjectUtils.requireNonNull(getBindingContext().getClassBinding(clazz));
      retval = classBinding.getFlagInstanceByName(jsonKeyFlagName);
    }
    return retval;
  }

  @Override
  public Object readItem(Object parent, IItemReadHandler handler) throws IOException {
    return handler.readChoiceGroupItem(parent, this);
  }

  @Override
  public void writeItem(Object item, IItemWriteHandler handler) throws IOException {
    IBoundGroupedNamedModelInstance itemInstance = getItemInstance(item);
    handler.writeChoiceGroupItem(item, this, itemInstance);
  }

  @Override
  public Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    // TODO Auto-generated method stub
    return this;
  }
}
