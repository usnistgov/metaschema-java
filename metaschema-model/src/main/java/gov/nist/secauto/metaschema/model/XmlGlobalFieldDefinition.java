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

import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.ExternalModelSource;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFieldDefinitionType;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class XmlGlobalFieldDefinition implements IFieldDefinition {
  @NotNull
  private final GlobalFieldDefinitionType xmlField;
  @NotNull
  private final IMetaschema metaschema;
  private XmlFlagContainerSupport flagContainer;

  private IValueConstraintSupport constraints;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound to Java objects.
   * 
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalFieldDefinition(@NotNull GlobalFieldDefinitionType xmlField, @NotNull IMetaschema metaschema) {
    this.xmlField = xmlField;
    this.metaschema = metaschema;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  @NotNull
  protected GlobalFieldDefinitionType getXmlField() {
    return xmlField;
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return metaschema;
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   * 
   * @return the constraints instance
   */
  @SuppressWarnings("null")
  protected IValueConstraintSupport initModelConstraints() {
    synchronized (this) {
      if (constraints == null) {
        if (getXmlField().isSetConstraint()) {
          constraints = new ValueConstraintSupport(
              ObjectUtils.notNull(getXmlField().getConstraint()),
              ExternalModelSource.instance(getContainingMetaschema().getLocation()));
        } else {
          constraints = new ValueConstraintSupport();
        }
      }
      return constraints;
    }
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return initModelConstraints().getConstraints();
  }

  @Override
  public List<? extends IAllowedValuesConstraint> getAllowedValuesConstraints() {
    return initModelConstraints().getAllowedValuesConstraints();
  }

  @Override
  public List<? extends IMatchesConstraint> getMatchesConstraints() {
    return initModelConstraints().getMatchesConstraints();
  }

  @Override
  public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return initModelConstraints().getIndexHasKeyConstraints();
  }

  @Override
  public List<? extends IExpectConstraint> getExpectConstraints() {
    return initModelConstraints().getExpectConstraints();
  }

  @Override
  public void addConstraint(@NotNull IAllowedValuesConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NotNull IMatchesConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NotNull IIndexHasKeyConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NotNull IExpectConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IFieldInstance getInlineInstance() {
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public String getUseName() {
    return getXmlField().isSetUseName() ? getXmlField().getUseName() : getName();
  }

  @Override
  public String getFormalName() {
    return getXmlField().isSetFormalName() ? getXmlField().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlField().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlField().getDescription())
        : null;
  }

  /**
   * Lazy initialize the flag instances associated with this definition.
   * 
   * @return the flag container
   */
  protected XmlFlagContainerSupport initFlagContainer() {
    synchronized (this) {
      if (flagContainer == null) {
        flagContainer = new XmlFlagContainerSupport(getXmlField(), this);
      }
      return flagContainer;
    }
  }

  @NotNull
  private Map<@NotNull String, ? extends IFlagInstance> getFlagInstanceMap() {
    return initFlagContainer().getFlagInstanceMap();
  }

  @Override
  public IFlagInstance getFlagInstanceByName(String name) {
    return getFlagInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFlagInstance> getFlagInstances() {
    return getFlagInstanceMap().values();
  }

  @SuppressWarnings("null")
  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getXmlField().isSetAsType() ? getXmlField().getAsType() : MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
  }

  @Override
  public boolean hasJsonValueKeyFlagInstance() {
    return getXmlField().isSetJsonValueKeyFlag() && getXmlField().getJsonValueKeyFlag().isSetFlagRef();
  }

  @Override
  public IFlagInstance getJsonValueKeyFlagInstance() {
    IFlagInstance retval = null;
    if (getXmlField().isSetJsonValueKeyFlag() && getXmlField().getJsonValueKeyFlag().isSetFlagRef()) {
      retval = getFlagInstanceByName(getXmlField().getJsonValueKeyFlag().getFlagRef());
    }
    return retval;
  }

  @Override
  public String getJsonValueKeyName() {
    String retval = null;

    if (getXmlField().isSetJsonValueKey()) {
      retval = getXmlField().getJsonValueKey();
    }

    if (retval == null || retval.isEmpty()) {
      retval = getJavaTypeAdapter().getDefaultJsonValueKey();
    }
    return retval;
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlField().isSetJsonKey();
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    IFlagInstance retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlField().getJsonKey().getFlagRef());
    }
    return retval;
  }

  @Override
  public boolean isCollapsible() {
    return getXmlField().isSetCollapsible() ? getXmlField().getCollapsible()
        : MetaschemaModelConstants.DEFAULT_FIELD_COLLAPSIBLE;
  }

  @SuppressWarnings("null")
  @Override
  public ModuleScopeEnum getModuleScope() {
    return getXmlField().isSetScope() ? getXmlField().getScope() : IDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlField().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlField().getRemarks()) : null;
  }

  @Override
  public Object getFieldValue(@NotNull Object parentFieldValue) {
    // there is no value
    return null;
  }
}
