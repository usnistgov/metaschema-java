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

package gov.nist.secauto.metaschema.core.model.xml.impl; // NOPMD - excessive public methods and coupling is unavoidable

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Represents a Metaschema assembly definition declared locally as an instance.
 */
class XmlInlineAssemblyDefinition
    extends AbstractAssemblyInstance
    implements IAssemblyDefinition,
    IFeatureStandardModelContainer,
    IFeatureFlagContainer<IFlagInstance>,
    IFeatureInlinedDefinition<IAssemblyDefinition, IAssemblyInstance> {
  @NonNull
  private final InlineAssemblyDefinitionType xmlObject;

  @NonNull
  private final Lazy<XmlFlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IStandardModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> constraints;

  /**
   * Constructs a new Metaschema assembly definition from an XML representation
   * bound to Java objects.
   *
   * @param xmlObject
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  public XmlInlineAssemblyDefinition(
      @NonNull InlineAssemblyDefinitionType xmlObject,
      @NonNull IModelContainer parent) {
    super(parent);
    this.xmlObject = xmlObject;
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new XmlFlagContainerSupport(xmlObject, this)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      IStandardModelContainerSupport retval = new AssemblyModelContainerSupportImpl();
      if (xmlObject.isSetModel()) {
        XmlModelParser.parseModel(ObjectUtils.notNull(xmlObject.getModel()), this, retval);
      }
      return retval;
    }));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      if (getXmlObject().isSetConstraint()) {
        ConstraintXmlSupport.parse(retval, ObjectUtils.notNull(getXmlObject().getConstraint()),
            ISource.modelSource(ObjectUtils.requireNonNull(getContainingModule().getLocation())));
      }
      return retval;
    }));
  }

  @Override
  public IAssemblyDefinition getDefinition() {
    return this;
  }

  @Override
  @NonNull
  public IAssemblyInstance getInlineInstance() {
    return this;
  }

  @SuppressWarnings("null")
  @Override
  public XmlFlagContainerSupport getFlagContainer() {
    return flagContainer.get();
  }

  @SuppressWarnings("null")
  @Override
  public IStandardModelContainerSupport getModelContainer() {
    return modelContainer.get();
  }

  @SuppressWarnings("null")
  @Override
  public IModelConstrained getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public boolean isRoot() {
    // a local assembly is never a root
    return false;
  }

  @Override
  public String getRootName() {
    // a local assembly is never a root
    return null;
  }

  @Override
  public Integer getRootIndex() {
    // a local assembly is never a root
    return null;
  }

  // ----------------------------------------
  // - Start XmlBeans driven code - CPD-OFF -
  // ----------------------------------------

  /**
   * Get the underlying XML model.
   *
   * @return the XML model
   */
  protected InlineAssemblyDefinitionType getXmlObject() {
    return xmlObject;
  }

  @Override
  public String getFormalName() {
    return getXmlObject().isSetFormalName() ? getXmlObject().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlObject().isSetDescription()
        ? MarkupStringConverter.toMarkupString(getXmlObject().getDescription())
        : null;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlObject().getPropList()));
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlObject().getName();
  }

  @Override
  public Integer getIndex() {
    return getXmlObject().isSetIndex() ? getXmlObject().getIndex().intValue() : null;
  }

  @Override
  public String getGroupAsName() {
    return getXmlObject().isSetGroupAs() ? getXmlObject().getGroupAs().getName() : null;
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

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlObject().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlObject().getRemarks()) : null;
  }

  // -------------------------------------
  // - End XmlBeans driven code - CPD-ON -
  // -------------------------------------
}
