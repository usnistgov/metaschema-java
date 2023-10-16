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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.impl.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings({ "PMD.GodClass", "PMD.CouplingBetweenObjects" })
class XmlGlobalFieldDefinition
    implements IFieldDefinition,
    IFeatureFlagContainer<IFlagInstance> {
  @NonNull
  private final GlobalFieldDefinitionType xmlField;
  @NonNull
  private final IModule module;
  @Nullable
  private final Object defaultValue;
  private final Lazy<XmlFlagContainerSupport> flagContainer;
  private final Lazy<IValueConstrained> constraints;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound
   * to Java objects.
   *
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param module
   *          the containing Metaschema module
   */
  public XmlGlobalFieldDefinition(@NonNull GlobalFieldDefinitionType xmlField, @NonNull IModule module) {
    this.xmlField = xmlField;
    this.module = module;
    Object defaultValue = null;
    if (xmlField.isSetDefault()) {
      defaultValue = getJavaTypeAdapter().parse(ObjectUtils.requireNonNull(xmlField.getDefault()));
    }
    this.defaultValue = defaultValue;
    this.flagContainer = Lazy.lazy(() -> new XmlFlagContainerSupport(xmlField, this));
    this.constraints = Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      if (getXmlField().isSetConstraint()) {
        ConstraintXmlSupport.parse(retval, ObjectUtils.notNull(getXmlField().getConstraint()),
            ISource.modelSource(ObjectUtils.requireNonNull(getContainingModule().getLocation())));
      }
      return retval;
    });
  }

  /**
   * Lazy initialize the flag instances associated with this definition.
   *
   * @return the flag container
   */
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

  // ----------------------------------------
  // - Start Annotation driven code - CPD-OFF
  // ----------------------------------------

  /**
   * Get the underlying XML data.
   *
   * @return the underlying XML data
   */
  @NonNull
  protected final GlobalFieldDefinitionType getXmlField() {
    return xmlField;
  }

  @SuppressWarnings({ "null" })
  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public Integer getIndex() {
    return getXmlField().isSetIndex() ? getXmlField().getIndex().intValue() : null;
  }

  @Override
  public String getUseName() {
    return getXmlField().isSetUseName() ? getXmlField().getUseName().getStringValue() : null;
  }

  @Override
  public Integer getUseIndex() {
    Integer retval = null;
    if (getXmlField().isSetUseName()) {
      GlobalFieldDefinitionType.UseName useName = getXmlField().getUseName();
      if (useName.isSetIndex()) {
        retval = useName.getIndex().intValue();
      }
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
  public final IDataTypeAdapter<?> getJavaTypeAdapter() {
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

  // --------------------------------------
  // - End Annotation driven code - CPD-ON
  // --------------------------------------

  @Override
  public Object getFieldValue(@NonNull Object parentFieldValue) {
    // there is no value
    return null;
  }

  @Override
  public IModule getContainingModule() {
    return module;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public boolean isInline() {
    // global
    return false;
  }

  @Override
  public IFieldInstance getInlineInstance() {
    // global
    return null;
  }
}
