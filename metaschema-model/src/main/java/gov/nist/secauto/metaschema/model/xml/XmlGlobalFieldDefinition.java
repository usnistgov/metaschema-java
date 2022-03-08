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

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.secauto.metaschema.model.IXmlMetaschema;
import gov.nist.secauto.metaschema.model.common.ModelConstants;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.definitions.IXmlFieldDefinition;
import gov.nist.secauto.metaschema.model.instances.IXmlFlagInstance;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFieldDefinitionType;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class XmlGlobalFieldDefinition implements IXmlFieldDefinition {
  @NotNull
  private final GlobalFieldDefinitionType xmlField;
  @NotNull
  private final IXmlMetaschema metaschema;
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
  public XmlGlobalFieldDefinition(@NotNull GlobalFieldDefinitionType xmlField, @NotNull IXmlMetaschema metaschema) {
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
  public @NotNull IXmlMetaschema getContainingMetaschema() {
    return metaschema;
  }

  @Override
  public boolean isGlobal() {
    return true;
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected synchronized void checkModelConstraints() {
    if (constraints == null) {
      if (getXmlField().isSetConstraint()) {
        constraints = new ValueConstraintSupport(getXmlField().getConstraint());
      } else {
        constraints = IValueConstraintSupport.NULL_CONSTRAINT;
      }
    }
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    checkModelConstraints();
    return constraints.getConstraints();
  }

  @Override
  public List<? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
    checkModelConstraints();
    return constraints.getAllowedValuesContraints();
  }

  @Override
  public List<? extends IMatchesConstraint> getMatchesConstraints() {
    checkModelConstraints();
    return constraints.getMatchesConstraints();
  }

  @Override
  public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    checkModelConstraints();
    return constraints.getIndexHasKeyConstraints();
  }

  @Override
  public List<? extends IExpectConstraint> getExpectConstraints() {
    checkModelConstraints();
    return constraints.getExpectConstraints();
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IInstance getInlineInstance() {
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
  public String getXmlNamespace() {
    return getContainingMetaschema().getXmlNamespace().toString();
  }

  @Override
  public String getFormalName() {
    return getXmlField().isSetFormalName() ? getXmlField().getFormalName() : null;
  }

  @Override
  public MarkupLine getDescription() {
    return getXmlField().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlField().getDescription()) : null;
  }

  protected synchronized void initFlagContainer() {
    if (flagContainer == null) {
      flagContainer = new XmlFlagContainerSupport(getXmlField(), this);
    }
  }

  @Override
  public Map<@NotNull String, ? extends IXmlFlagInstance> getFlagInstanceMap() {
    initFlagContainer();
    return flagContainer.getFlagInstanceMap();
  }

  @SuppressWarnings("null")
  @Override
  public IJavaTypeAdapter<?> getDatatype() {
    IJavaTypeAdapter<?> retval;
    if (getXmlField().isSetAsType()) {
      retval = getXmlField().getAsType();
    } else {
      // the default
      retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    }
    return retval;
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
      retval = getDatatype().getDefaultJsonValueKey();
    }
    return retval;
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlField().isSetJsonKey();
  }

  @Override
  public IXmlFlagInstance getJsonKeyFlagInstance() {
    IXmlFlagInstance retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlField().getJsonKey().getFlagRef());
    }
    return retval;
  }

  @Override
  public boolean isCollapsible() {
    return getXmlField().isSetCollapsible() ? getXmlField().getCollapsible() : ModelConstants.DEFAULT_FIELD_COLLAPSIBLE;
  }

  @SuppressWarnings("null")
  @Override
  public ModuleScopeEnum getModuleScope() {
    return getXmlField().isSetScope() ? getXmlField().getScope() : IDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlField().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlField().getRemarks()) : null;
  }
}
