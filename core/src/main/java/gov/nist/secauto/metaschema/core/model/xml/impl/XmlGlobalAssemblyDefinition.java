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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFeatureStandardModelContainer;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings("PMD.CouplingBetweenObjects")
class XmlGlobalAssemblyDefinition
    implements IAssemblyDefinition,
    IFeatureStandardModelContainer,
    IFeatureFlagContainer<IFlagInstance> {

  @NonNull
  private final GlobalAssemblyDefinitionType xmlAssembly;
  @NonNull
  private final XmlModule metaschema;
  @NonNull
  private final Lazy<XmlFlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IStandardModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> constraints;

  /**
   * Constructs a new Metaschema Assembly definition from an XML representation
   * bound to Java objects.
   *
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalAssemblyDefinition(
      @NonNull GlobalAssemblyDefinitionType xmlAssembly,
      @NonNull XmlModule metaschema) {
    this.xmlAssembly = xmlAssembly;
    this.metaschema = metaschema;
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new XmlFlagContainerSupport(xmlAssembly, this)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> {
      IStandardModelContainerSupport retval = new DefaultModelContainerSupport();
      if (xmlAssembly.isSetModel()) {
        XmlModelParser.parseModel(ObjectUtils.notNull(xmlAssembly.getModel()), this, retval);
      }
      return retval;
    }));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      if (xmlAssembly.isSetConstraint()) {
        ConstraintXmlSupport.parse(retval, ObjectUtils.notNull(xmlAssembly.getConstraint()),
            ISource.modelSource(metaschema.getLocation()));
      }
      return retval;
    }));
  }

  @Override
  public XmlFlagContainerSupport getFlagContainer() {
    return ObjectUtils.notNull(flagContainer.get());
  }

  @Override
  public IStandardModelContainerSupport getModelContainer() {
    return ObjectUtils.notNull(modelContainer.get());
  }

  @Override
  public IModelConstrained getConstraintSupport() {
    return ObjectUtils.notNull(constraints.get());
  }

  @Override
  public XmlModule getContainingModule() {
    return metaschema;
  }

  // ----------------------------------------
  // - Start Annotation driven code - CPD-OFF
  // ----------------------------------------

  /**
   * Get the underlying XML data.
   *
   * @return the underlying XML data
   */
  @NonNull
  protected GlobalAssemblyDefinitionType getXmlAssembly() {
    return xmlAssembly;
  }

  @Override
  public String getName() {
    return ObjectUtils.requireNonNull(getXmlAssembly().getName());
  }

  @Override
  public Integer getIndex() {
    return getXmlAssembly().isSetIndex() ? getXmlAssembly().getIndex().intValue() : null;
  }

  @Override
  public String getUseName() {
    return getXmlAssembly().isSetUseName() ? getXmlAssembly().getUseName().getStringValue() : null;
  }

  @Override
  public Integer getUseIndex() {
    Integer retval = null;
    if (getXmlAssembly().isSetUseName()) {
      GlobalAssemblyDefinitionType.UseName useName = getXmlAssembly().getUseName();
      if (useName.isSetIndex()) {
        retval = useName.getIndex().intValue();
      }
    }
    return retval;
  }

  @Override
  public String getFormalName() {
    return getXmlAssembly().isSetFormalName() ? getXmlAssembly().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlAssembly().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription())
        : null;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlAssembly().getPropList()));
  }

  @Override
  public boolean isRoot() {
    return getXmlAssembly().isSetRootName();
  }

  @Override
  public String getRootName() {
    return getXmlAssembly().isSetRootName() ? getXmlAssembly().getRootName().getStringValue() : null;
  }

  @Override
  public Integer getRootIndex() {
    Integer retval = null;
    if (getXmlAssembly().isSetRootName()) {
      GlobalAssemblyDefinitionType.RootName rootName = getXmlAssembly().getRootName();
      if (rootName.isSetIndex()) {
        retval = rootName.getIndex().intValue();
      }
    }
    return retval;
  }

  @SuppressWarnings("null")
  @Override
  public ModuleScopeEnum getModuleScope() {
    return getXmlAssembly().isSetScope() ? getXmlAssembly().getScope() : IDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlAssembly().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getRemarks()) : null;
  }

  // --------------------------------------
  // - End Annotation driven code - CPD-ON
  // --------------------------------------

  @Override
  public boolean isInline() {
    // global
    return false;
  }

  @Override
  public IAssemblyInstance getInlineInstance() {
    // global
    return null;
  }
}
