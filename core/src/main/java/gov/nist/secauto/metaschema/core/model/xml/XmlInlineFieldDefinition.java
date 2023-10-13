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

package gov.nist.secauto.metaschema.core.model.xml; // NOPMD - intentional

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFeatureInlinedDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.ExternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

class XmlInlineFieldDefinition
    extends AbstractFieldInstance {
  @NonNull
  private final InlineFieldDefinitionType xmlField;
  @NonNull
  private final InternalFieldDefinition fieldDefinition;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound
   * to Java objects.
   *
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  public XmlInlineFieldDefinition(
      @NonNull InlineFieldDefinitionType xmlField,
      @NonNull IModelContainer parent) {
    super(parent);
    this.xmlField = xmlField;
    this.fieldDefinition = new InternalFieldDefinition(xmlField);
  }

  @Override
  public InternalFieldDefinition getDefinition() {
    return fieldDefinition;
  }

  @Override
  public IModule getContainingModule() {
    return getContainingDefinition().getContainingModule();
  }

  // ----------------------------------------
  // - Start annotation driven code - CPD-OFF
  // ----------------------------------------

  /**
   * Get the underlying XML model.
   *
   * @return the XML model
   */
  @NonNull
  protected final InlineFieldDefinitionType getXmlField() {
    return xmlField;
  }

  @Override
  public boolean isInXmlWrapped() {
    boolean retval;
    if (MarkupDataTypeProvider.MARKUP_MULTILINE.equals(getDefinition().getJavaTypeAdapter())) {
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

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlField().getPropList()));
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public Integer getIndex() {
    return getXmlField().isSetIndex() ? getXmlField().getIndex().intValue() : null;
  }

  @Override
  public String getGroupAsName() {
    return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    return XmlModelParser.getMinOccurs(getXmlField().getMinOccurs());
  }

  @Override
  public int getMaxOccurs() {
    return XmlModelParser.getMaxOccurs(getXmlField().getMaxOccurs());
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return XmlModelParser.getJsonGroupAsBehavior(getXmlField().getGroupAs());
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return XmlModelParser.getXmlGroupAsBehavior(getXmlField().getGroupAs());
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlField().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlField().getRemarks()) : null;
  }

  // --------------------------------------
  // - End annotation driven code - CPD-ON
  // --------------------------------------

  @Override
  public String getUseName() {
    // an inline definition doesn't have a use name
    return null;
  }

  @Override
  public Integer getUseIndex() {
    // an inline definition doesn't have a use name index
    return null;
  }

  @Override
  public Object getValue(@NonNull Object parentValue) {
    // there is no value
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<?> getItemValues(Object instanceValue) {
    // there are no item values
    return Collections.emptyList();
  }

  /**
   * The corresponding definition for the local flag instance.
   */
  private final class InternalFieldDefinition
      implements IFieldDefinition,
      IFeatureInlinedDefinition<IFieldInstance>,
      IFeatureFlagContainer<IFlagInstance> {
    @Nullable
    private final Object defaultValue;
    private final Lazy<XmlFlagContainerSupport> flagContainer;
    private final Lazy<IValueConstrained> constraints;

    private InternalFieldDefinition(
        @NonNull InlineFieldDefinitionType xmlField) {
      Object defaultValue = null;
      if (xmlField.isSetDefault()) {
        defaultValue = getJavaTypeAdapter().parse(ObjectUtils.requireNonNull(xmlField.getDefault()));
      }
      this.defaultValue = defaultValue;
      this.flagContainer = Lazy.lazy(() -> new XmlFlagContainerSupport(xmlField, this));
      this.constraints = Lazy.lazy(() -> {
        IValueConstrained retval;
        if (xmlField.isSetConstraint()) {
          retval = new ValueConstraintSupport(
              ObjectUtils.notNull(xmlField.getConstraint()),
              ExternalModelSource.instance(
                  ObjectUtils.requireNonNull(getContainingModule().getLocation())));
        } else {
          retval = new ValueConstraintSupport();
        }
        return retval;
      });
    }

    // ----------------------------------------
    // - Start annotation driven code - CPD-OFF
    // ----------------------------------------

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
        retval = getFlagInstanceByName(ObjectUtils.notNull(getXmlField().getJsonValueKeyFlag().getFlagRef()));
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

    // --------------------------------------
    // - End annotation driven code - CPD-ON
    // --------------------------------------

    @Override
    public Object getDefaultValue() {
      return defaultValue;
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    @NonNull
    public IFieldInstance getInlineInstance() {
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
    public @NonNull Map<QName, Set<String>> getProperties() {
      return XmlInlineFieldDefinition.this.getProperties();
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
    public Integer getIndex() {
      return XmlInlineFieldDefinition.this.getIndex();
    }

    @Override
    public String getUseName() {
      // always use the name instead
      return null;
    }

    @Override
    public Integer getUseIndex() {
      // always use the name index instead
      return null;
    }

    @SuppressWarnings("null")
    @Override
    public XmlFlagContainerSupport getFlagContainer() {
      return flagContainer.get();
    }

    /**
     * Used to generate the instances for the constraints in a lazy fashion when the
     * constraints are first accessed.
     *
     * @return the constraints instance
     */
    @SuppressWarnings("null")
    @Override
    public IValueConstrained getConstraintSupport() {
      return constraints.get();
    }

    @Override
    public MarkupMultiline getRemarks() {
      return XmlInlineFieldDefinition.this.getRemarks();
    }

    @Override
    public IModule getContainingModule() {
      return XmlInlineFieldDefinition.super.getContainingDefinition().getContainingModule();
    }

    @Override
    public Object getFieldValue(@NonNull Object parentFieldValue) {
      // there is no value
      return null;
    }
  }
}
