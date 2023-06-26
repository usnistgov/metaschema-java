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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ExternalModelSource;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalAssemblyDefinitionType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings("PMD.CouplingBetweenObjects")
class XmlGlobalAssemblyDefinition implements IAssemblyDefinition { // NOPMD - intentional

  @NonNull
  private final GlobalAssemblyDefinitionType xmlAssembly;
  @NonNull
  private final XmlMetaschema metaschema;
  private final Lazy<XmlFlagContainerSupport> flagContainer;
  private final Lazy<XmlModelContainerSupport> modelContainer;
  private final Lazy<AssemblyConstraintSupport> constraints;

  /**
   * Constructs a new Metaschema Assembly definition from an XML representation bound to Java objects.
   *
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalAssemblyDefinition(
      @NonNull GlobalAssemblyDefinitionType xmlAssembly,
      @NonNull XmlMetaschema metaschema) {
    this.xmlAssembly = xmlAssembly;
    this.metaschema = metaschema;
    this.flagContainer = Lazy.lazy(() -> new XmlFlagContainerSupport(xmlAssembly, this));
    this.modelContainer = Lazy.lazy(() -> new XmlModelContainerSupport(xmlAssembly, this));
    this.constraints = Lazy.lazy(() -> AssemblyConstraintSupport.newInstance(
        xmlAssembly,
        ExternalModelSource.instance(metaschema.getLocation())));
  }

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
  public XmlMetaschema getContainingMetaschema() {
    return metaschema;
  }

  protected XmlFlagContainerSupport getFlagContainer() {
    return flagContainer.get();
  }

  @NonNull
  private Map<String, ? extends IFlagInstance> getFlagInstanceMap() {
    return getFlagContainer().getFlagInstanceMap();
  }

  @Override
  public IFlagInstance getFlagInstanceByName(String name) {
    return getFlagInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IFlagInstance> getFlagInstances() {
    return getFlagInstanceMap().values();
  }

  protected XmlModelContainerSupport getModelContainer() {
    return modelContainer.get();
  }

  private Map<String, ? extends INamedModelInstance> getNamedModelInstanceMap() {
    return getModelContainer().getNamedModelInstanceMap();
  }

  @Override
  public @Nullable INamedModelInstance getModelInstanceByName(String name) {
    return getNamedModelInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public @NonNull Collection<? extends INamedModelInstance> getNamedModelInstances() {
    return getNamedModelInstanceMap().values();
  }

  private Map<String, ? extends IFieldInstance> getFieldInstanceMap() {
    return getModelContainer().getFieldInstanceMap();
  }

  @Override
  public IFieldInstance getFieldInstanceByName(String name) {
    return getFieldInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IFieldInstance> getFieldInstances() {
    return getFieldInstanceMap().values();
  }

  private Map<String, ? extends IAssemblyInstance> getAssemblyInstanceMap() {
    return getModelContainer().getAssemblyInstanceMap();
  }

  @Override
  public IAssemblyInstance getAssemblyInstanceByName(String name) {
    return getAssemblyInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyInstance> getAssemblyInstances() {
    return getAssemblyInstanceMap().values();
  }

  @Override
  public List<? extends IChoiceInstance> getChoiceInstances() {
    return getModelContainer().getChoiceInstances();
  }

  @Override
  public List<? extends IModelInstance> getModelInstances() {
    return getModelContainer().getModelInstances();
  }

  protected IAssemblyConstraintSupport getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return getConstraintSupport().getConstraints();
  }

  @Override
  public List<? extends IAllowedValuesConstraint> getAllowedValuesConstraints() {
    return getConstraintSupport().getAllowedValuesConstraints();
  }

  @Override
  public List<? extends IMatchesConstraint> getMatchesConstraints() {
    return getConstraintSupport().getMatchesConstraints();
  }

  @Override
  public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return getConstraintSupport().getIndexHasKeyConstraints();
  }

  @Override
  public List<? extends IExpectConstraint> getExpectConstraints() {
    return getConstraintSupport().getExpectConstraints();
  }

  @Override
  public List<? extends IIndexConstraint> getIndexConstraints() {
    return getConstraintSupport().getIndexConstraints();
  }

  @Override
  public List<? extends IUniqueConstraint> getUniqueConstraints() {
    return getConstraintSupport().getUniqueConstraints();
  }

  @Override
  public List<? extends ICardinalityConstraint> getHasCardinalityConstraints() {
    return getConstraintSupport().getHasCardinalityConstraints();
  }

  @Override
  public void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IMatchesConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IExpectConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IUniqueConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull ICardinalityConstraint constraint) {
    getConstraintSupport().addConstraint(constraint);
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IAssemblyInstance getInlineInstance() {
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlAssembly().getName();
  }

  @Override
  public String getUseName() {
    return getXmlAssembly().isSetUseName() ? getXmlAssembly().getUseName() : null;
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
  public boolean hasJsonKey() {
    return getXmlAssembly().isSetJsonKey();
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    IFlagInstance retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlAssembly().getJsonKey().getFlagRef());
    }
    return retval;
  }

  @Override
  public boolean isRoot() {
    return getXmlAssembly().isSetRootName();
  }

  @Override
  public String getRootName() {
    return getXmlAssembly().isSetRootName() ? getXmlAssembly().getRootName() : null;
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
}
