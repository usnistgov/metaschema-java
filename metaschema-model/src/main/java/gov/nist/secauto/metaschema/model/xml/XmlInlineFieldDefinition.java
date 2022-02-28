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
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.definitions.ILocalDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlAssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlFieldDefinition;
import gov.nist.secauto.metaschema.model.instances.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.instances.IXmlFlagInstance;
import gov.nist.secauto.metaschema.model.xmlbeans.InlineFieldDefinitionType;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class XmlInlineFieldDefinition
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
  public XmlInlineFieldDefinition(@NotNull InlineFieldDefinitionType xmlField, @NotNull IXmlAssemblyDefinition parent) {
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
  public IXmlMetaschema getContainingMetaschema() {
    return getContainingDefinition().getContainingMetaschema();
  }

  @Override
  public boolean isInXmlWrapped() {
    boolean retval;
    if (MetaschemaDataTypeProvider.MARKUP_MULTILINE.equals(getDefinition().getDatatype())) {
      // default value
      retval = ModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED;
      if (getXmlField().isSetInXml()) {
        retval = getXmlField().getInXml();
      }
    } else {
      // All other data types get "wrapped"
      retval = true;
    }
    return retval;
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
  public String getXmlNamespace() {
    return getContainingDefinition().getXmlNamespace();
  }

  @Override
  public String getGroupAsName() {
    return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : null;
  }

  @Override
  public String getGroupAsXmlNamespace() {
    return getContainingDefinition().getXmlNamespace();
  }

  @Override
  public int getMinOccurs() {
    int retval = ModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlField().isSetMinOccurs()) {
      retval = getXmlField().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = ModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS;
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
    JsonGroupAsBehavior retval = ModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInJson()) {
      retval = getXmlField().getGroupAs().getInJson();
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = ModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInXml()) {
      retval = getXmlField().getGroupAs().getInXml();
    }
    return retval;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlField().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlField().getRemarks()) : null;
  }

  public class InternalFieldDefinition implements IXmlFieldDefinition, ILocalDefinition<XmlInlineFieldDefinition> {
    private XmlFlagContainerSupport flagContainer;
    private IValueConstraintSupport constraints;

    /**
     * Create the corresponding definition for the local flag instance.
     */
    public InternalFieldDefinition() {

    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public String getFormalName() {
      return getXmlField().getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return MarkupStringConverter.toMarkupString(getXmlField().getDescription());
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

    @Override
    public String getXmlNamespace() {
      return XmlInlineFieldDefinition.this.getXmlNamespace();
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
      return getXmlField().isSetJsonValueKey() && getXmlField().getJsonValueKey().isSetFlagName();
    }

    @Override
    public IFlagInstance getJsonValueKeyFlagInstance() {
      IFlagInstance retval = null;
      if (getXmlField().isSetJsonValueKey() && getXmlField().getJsonValueKey().isSetFlagName()) {
        retval = getFlagInstanceByName(getXmlField().getJsonValueKey().getFlagName());
      }
      return retval;
    }

    @Override
    public String getJsonValueKeyName() {
      String retval = null;

      if (getXmlField().isSetJsonValueKey()) {
        retval = getXmlField().getJsonValueKey().getStringValue();
      }

      if (retval == null || retval.isEmpty()) {
        retval = getDatatype().getDefaultJsonValueKey();
      }
      return retval;
    }

    @Override
    public boolean isCollapsible() {
      return getXmlField().isSetCollapsible() ? getXmlField().getCollapsible()
          : ModelConstants.DEFAULT_FIELD_COLLAPSIBLE;
    }

    @Override
    public XmlInlineFieldDefinition getDefiningInstance() {
      return XmlInlineFieldDefinition.this;
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

    @Override
    public boolean hasJsonKey() {
      return getXmlField().isSetJsonKey();
    }

    @Override
    public IXmlFlagInstance getJsonKeyFlagInstance() {
      IXmlFlagInstance retval = null;
      if (hasJsonKey()) {
        retval = getFlagInstanceByName(getXmlField().getJsonKey().getFlagName());
      }
      return retval;
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

    /**
     * Get the constraints container in a lazy fashion.
     * 
     * @return the constraints container
     */
    @SuppressWarnings("null")
    @NotNull
    protected IValueConstraintSupport getConstraintSupport() {
      checkModelConstraints();
      return constraints;
    }

    @Override
    public List<@NotNull ? extends IConstraint> getConstraints() {
      return getConstraintSupport().getConstraints();
    }

    @Override
    public List<@NotNull ? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
      return getConstraintSupport().getAllowedValuesContraints();
    }

    @Override
    public List<@NotNull ? extends IMatchesConstraint> getMatchesConstraints() {
      return getConstraintSupport().getMatchesConstraints();
    }

    @Override
    public List<@NotNull ? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
      return getConstraintSupport().getIndexHasKeyConstraints();
    }

    @Override
    public List<@NotNull ? extends IExpectConstraint> getExpectConstraints() {
      return getConstraintSupport().getExpectConstraints();
    }

    @Override
    public MarkupMultiline getRemarks() {
      return XmlInlineFieldDefinition.this.getRemarks();
    }

    @Override
    public @NotNull IXmlMetaschema getContainingMetaschema() {
      return getContainingDefinition().getContainingMetaschema();
    }

  }
}
