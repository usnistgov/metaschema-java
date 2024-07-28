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
import gov.nist.secauto.metaschema.core.model.AbstractGlobalFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IAttributable;
import gov.nist.secauto.metaschema.core.model.IContainerFlagSupport;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.UseNameType;
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
    extends AbstractGlobalFieldDefinition<XmlModule, IFieldInstance, IFlagInstance> {
  @NonNull
  private final GlobalFieldDefinitionType xmlObject;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<IContainerFlagSupport<IFlagInstance>> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound
   * to Java objects.
   *
   * @param xmlObject
   *          the XML representation bound to Java objects
   * @param module
   *          the containing Metaschema module
   */
  public XmlGlobalFieldDefinition(@NonNull GlobalFieldDefinitionType xmlObject, @NonNull XmlModule module) {
    super(module);
    this.xmlObject = xmlObject;
    Object defaultValue = null;
    if (xmlObject.isSetDefault()) {
      defaultValue = getJavaTypeAdapter().parse(ObjectUtils.requireNonNull(xmlObject.getDefault()));
    }
    this.defaultValue = defaultValue;
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> XmlFlagContainerSupport.newInstance(xmlObject, this)));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      if (getXmlObject().isSetConstraint()) {
        ConstraintXmlSupport.parse(retval, ObjectUtils.notNull(getXmlObject().getConstraint()),
            ISource.modelSource(ObjectUtils.requireNonNull(getContainingModule().getLocation())));
      }
      return retval;
    }));
  }

  /**
   * Lazy initialize the flag instances associated with this definition.
   *
   * @return the flag container
   */
  @SuppressWarnings("null")
  @Override
  public IContainerFlagSupport<IFlagInstance> getFlagContainer() {
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
  public Object getDefaultValue() {
    return defaultValue;
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
  protected final GlobalFieldDefinitionType getXmlObject() {
    return xmlObject;
  }

  @SuppressWarnings({ "null" })
  @Override
  public String getName() {
    return getXmlObject().getName();
  }

  @Override
  public Integer getIndex() {
    return getXmlObject().isSetIndex() ? getXmlObject().getIndex().intValue() : null;
  }

  @Override
  public String getUseName() {
    return getXmlObject().isSetUseName() ? getXmlObject().getUseName().getStringValue() : null;
  }

  @Override
  public Integer getUseIndex() {
    Integer retval = null;
    if (getXmlObject().isSetUseName()) {
      UseNameType useName = getXmlObject().getUseName();
      if (useName.isSetIndex()) {
        retval = useName.getIndex().intValue();
      }
    }
    return retval;
  }

  @Override
  public String getFormalName() {
    return getXmlObject().isSetFormalName() ? getXmlObject().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlObject().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlObject().getDescription())
        : null;
  }

  @Override
  public Map<IAttributable.Key, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlObject().getPropList()));
  }

  @SuppressWarnings("null")
  @Override
  public final IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getXmlObject().isSetAsType() ? getXmlObject().getAsType() : MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
  }

  @Override
  public boolean hasJsonValueKeyFlagInstance() {
    return getXmlObject().isSetJsonValueKeyFlag() && getXmlObject().getJsonValueKeyFlag().isSetFlagRef();
  }

  @Override
  public IFlagInstance getJsonValueKeyFlagInstance() {
    IFlagInstance retval = null;
    if (getXmlObject().isSetJsonValueKeyFlag() && getXmlObject().getJsonValueKeyFlag().isSetFlagRef()) {
      retval = getFlagInstanceByName(
          new QName(
              getXmlNamespace(),
              ObjectUtils.notNull(getXmlObject().getJsonValueKeyFlag().getFlagRef())));
    }
    return retval;
  }

  @Override
  public String getJsonValueKeyName() {
    return getXmlObject().getJsonValueKey();
  }

  @SuppressWarnings("null")
  @Override
  public ModuleScopeEnum getModuleScope() {
    return getXmlObject().isSetScope() ? getXmlObject().getScope() : DEFAULT_DEFINITION_MODEL_SCOPE;
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
