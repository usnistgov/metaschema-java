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

import gov.nist.secauto.metaschema.model.common.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IInlineNamedDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
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
import gov.nist.secauto.metaschema.model.xmlbeans.InlineFieldDefinitionType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class XmlInlineFieldDefinition
    extends AbstractFieldInstance {
  @NotNull
  private final InlineFieldDefinitionType xmlField;
  @NotNull
  private final InternalFieldDefinition fieldDefinition;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound to Java objects.
   * 
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent assembly definition
   */
  public XmlInlineFieldDefinition(@NotNull InlineFieldDefinitionType xmlField, @NotNull IAssemblyDefinition parent) {
    super(parent);
    this.xmlField = xmlField;
    this.fieldDefinition = new InternalFieldDefinition();
  }

  /**
   * Get the underlying XML model.
   * 
   * @return the XML model
   */
  @NotNull
  protected InlineFieldDefinitionType getXmlField() {
    return xmlField;
  }

  @Override
  public InternalFieldDefinition getDefinition() {
    return fieldDefinition;
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return getContainingDefinition().getContainingMetaschema();
  }

  @Override
  public boolean isInXmlWrapped() {
    boolean retval;
    if (MetaschemaDataTypeProvider.MARKUP_MULTILINE.equals(getDefinition().getJavaTypeAdapter())) {
      // default value
      retval = MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED;
      if (getXmlField().isSetInXml()) {
        retval = getXmlField().getInXml();
      }
    } else {
      // All other data types get "wrapped"
      retval = true;
    }
    return retval;
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

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public String getUseName() {
    return getXmlField().isSetUseName() ? getXmlField().getUseName() : getDefinition().getUseName();
  }

  @Override
  public String getGroupAsName() {
    return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlField().isSetMinOccurs()) {
      retval = getXmlField().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (getXmlField().isSetMaxOccurs()) {
      Object value = getXmlField().getMaxOccurs();
      if (value instanceof String) {
        // unbounded
        retval = -1;
      } else if (value instanceof BigInteger) {
        retval = ((BigInteger) value).intValueExact();
      }
    }
    return retval;
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    JsonGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInJson()) {
      retval = getXmlField().getGroupAs().getInJson();
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInXml()) {
      retval = getXmlField().getGroupAs().getInXml();
    }
    return retval;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlField().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlField().getRemarks()) : null;
  }

  @Override
  public Object getValue(@NotNull Object parentValue) {
    // there is no value
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ?> getItemValues(Object instanceValue) {
    // there are no item values
    return Collections.emptyList();
  }

  /**
   * The corresponding definition for the local flag instance.
   */
  public class InternalFieldDefinition implements IFieldDefinition, IInlineNamedDefinition<XmlInlineFieldDefinition> {
    private XmlFlagContainerSupport flagContainer;
    private IValueConstraintSupport constraints;

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public XmlInlineFieldDefinition getInlineInstance() {
      return XmlInlineFieldDefinition.this;
    }

    @Override
    public String getFormalName() {
      return XmlInlineFieldDefinition.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return XmlInlineFieldDefinition.this.getDescription();
    }

    @Override
    public ModuleScopeEnum getModuleScope() {
      return ModuleScopeEnum.LOCAL;
    }

    @Override
    public String getName() {
      return XmlInlineFieldDefinition.this.getName();
    }

    @Override
    public String getUseName() {
      return getName();
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
    public boolean isCollapsible() {
      return getXmlField().isSetCollapsible() ? getXmlField().getCollapsible()
          : MetaschemaModelConstants.DEFAULT_FIELD_COLLAPSIBLE;
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
    public @Nullable IFlagInstance getFlagInstanceByName(String name) {
      return getFlagInstanceMap().get(name);
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull Collection<@NotNull ? extends IFlagInstance> getFlagInstances() {
      return getFlagInstanceMap().values();
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

    /**
     * Used to generate the instances for the constraints in a lazy fashion when the constraints are
     * first accessed.
     * 
     * @return the constraints instance
     */
    @SuppressWarnings("null")
    @NotNull
    protected IValueConstraintSupport initModelConstraints() {
      synchronized (this) {
        if (constraints == null) {
          if (getXmlField().isSetConstraint()) {
            constraints = new ValueConstraintSupport(
                ObjectUtils.notNull(getXmlField().getConstraint()),
                ExternalModelSource.instance(
                    ObjectUtils.requireNonNull(getContainingMetaschema().getLocation())));
          } else {
            constraints = new ValueConstraintSupport();
          }
        }
        return constraints;
      }
    }

    @Override
    public List<@NotNull ? extends IConstraint> getConstraints() {
      return initModelConstraints().getConstraints();
    }

    @Override
    public List<@NotNull ? extends IAllowedValuesConstraint> getAllowedValuesConstraints() {
      return initModelConstraints().getAllowedValuesConstraints();
    }

    @Override
    public List<@NotNull ? extends IMatchesConstraint> getMatchesConstraints() {
      return initModelConstraints().getMatchesConstraints();
    }

    @Override
    public List<@NotNull ? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
      return initModelConstraints().getIndexHasKeyConstraints();
    }

    @Override
    public List<@NotNull ? extends IExpectConstraint> getExpectConstraints() {
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
    public MarkupMultiline getRemarks() {
      return XmlInlineFieldDefinition.this.getRemarks();
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      return XmlInlineFieldDefinition.super.getContainingDefinition().getContainingMetaschema();
    }

    @Override
    public Object getFieldValue(@NotNull Object parentFieldValue) {
      // there is no value
      return null;
    }
  }
}
