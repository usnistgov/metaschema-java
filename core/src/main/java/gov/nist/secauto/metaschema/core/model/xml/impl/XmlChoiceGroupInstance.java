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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractModelInstance;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedFieldInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedNamedModelInstance;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedChoiceType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

class XmlChoiceGroupInstance
    extends AbstractModelInstance<IAssemblyDefinition>
    implements IChoiceGroupInstance,
    IFeatureGroupedModelContainer<
        IGroupedNamedModelInstance,
        IGroupedFieldInstance,
        IGroupedAssemblyInstance> {
  @NonNull
  private final GroupedChoiceType xmlObject;
  @NonNull
  private final Lazy<ModelContainerSupport> modelContainer;

  /**
   * Constructs a mutually exclusive choice between two possible objects.
   *
   * @param xmlObject
   *          the XML for the choice definition bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  public XmlChoiceGroupInstance(
      @NonNull GroupedChoiceType xmlObject,
      @NonNull IAssemblyDefinition parent) {
    super(parent);
    this.xmlObject = xmlObject;
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      ModelContainerSupport retval = new ModelContainerSupport();
      XmlModelParser.parseChoiceGroup(xmlObject, this, retval);
      return retval;
    }));
  }

  @Override
  public ModelContainerSupport getModelContainer() {
    return ObjectUtils.notNull(modelContainer.get());
  }

  @Override
  public IAssemblyDefinition getOwningDefinition() {
    return getParentContainer();
  }

  // ----------------------------------------
  // - Start XmlBeans driven code - CPD-OFF -
  // ----------------------------------------

  /**
   * Get the underlying XML data.
   *
   * @return the underlying XML data
   */
  @NonNull
  protected GroupedChoiceType getXmlObject() {
    return xmlObject;
  }

  @Override
  public String getJsonDiscriminatorProperty() {
    return getXmlObject().isSetDiscriminator()
        ? ObjectUtils.requireNonNull(getXmlObject().getDiscriminator())
        : MetaschemaModelConstants.DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME;
  }

  @Override
  public String getJsonKeyFlagName() {
    return getXmlObject().isSetJsonKey() ? getXmlObject().getJsonKey().getFlagRef() : null;
  }

  @Override
  public String getGroupAsName() {
    return getXmlObject().getGroupAs().getName();
  }

  @Override
  public int getMinOccurs() {
    return XmlModelParser.getMinOccurs(getXmlObject().getMinOccurs());
  }

  @Override
  public int getMaxOccurs() {
    return XmlModelParser.getMaxOccurs(getXmlObject().getMaxOccurs());
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return XmlModelParser.getJsonGroupAsBehavior(getXmlObject().getGroupAs());
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return XmlModelParser.getXmlGroupAsBehavior(getXmlObject().getGroupAs());
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return getContainingModule().getXmlNamespace().toASCIIString();
  }

  @Override
  public MarkupMultiline getRemarks() {
    // remarks not supported
    return null;
  }

  // -------------------------------------
  // - End XmlBeans driven code - CPD-ON -
  // -------------------------------------
  private static class ModelContainerSupport
      implements IStandardGroupedModelContainerSupport {

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    @NonNull
    private final Map<String, IGroupedNamedModelInstance> namedModelInstances = new LinkedHashMap<>();
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    @NonNull
    private final Map<String, IGroupedFieldInstance> fieldInstances = new LinkedHashMap<>();
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    @NonNull
    private final Map<String, IGroupedAssemblyInstance> assemblyInstances = new LinkedHashMap<>();

    /**
     * Get a mapping of all named model instances, mapped from their effective name
     * to the instance.
     *
     * @return the mapping
     */
    @Override
    public Map<String, IGroupedNamedModelInstance> getNamedModelInstanceMap() {
      return namedModelInstances;
    }

    /**
     * Get a mapping of all field instances, mapped from their effective name to the
     * instance.
     *
     * @return the mapping
     */
    @Override
    public Map<String, IGroupedFieldInstance> getFieldInstanceMap() {
      return fieldInstances;
    }

    /**
     * Get a mapping of all assembly instances, mapped from their effective name to
     * the instance.
     *
     * @return the mapping
     */
    @Override
    public Map<String, IGroupedAssemblyInstance> getAssemblyInstanceMap() {
      return assemblyInstances;
    }
  }
}
