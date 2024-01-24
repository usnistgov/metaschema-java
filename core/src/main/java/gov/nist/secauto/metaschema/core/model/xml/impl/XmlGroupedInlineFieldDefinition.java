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
import gov.nist.secauto.metaschema.core.model.AbstractFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFeatureGroupedModelInstance;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class XmlGroupedInlineFieldDefinition
    extends AbstractFieldInstance
    implements IFieldDefinition,
    IFeatureInlinedDefinition<IFieldDefinition, IFieldInstance>,
    IFeatureFlagContainer<IFlagInstance>, IFeatureGroupedModelInstance {

  @NonNull
  private final GroupedInlineFieldDefinitionType xmlObject;
  @NonNull
  private final Lazy<XmlFlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<IValueConstrained> constraints;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound
   * to Java objects.
   *
   * @param xmlObject
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  @SuppressWarnings("PMD.NullAssignment")
  public XmlGroupedInlineFieldDefinition(
      @NonNull GroupedInlineFieldDefinitionType xmlObject,
      @NonNull IModelContainer parent) {
    super(parent);
    this.xmlObject = xmlObject;
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new XmlFlagContainerSupport(xmlObject, this)));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IValueConstrained retval = new ValueConstraintSet();
      if (getXmlObject().isSetConstraint()) {
        ConstraintXmlSupport.parse(retval, ObjectUtils.notNull(getXmlObject().getConstraint()),
            ISource.modelSource(ObjectUtils.requireNonNull(getContainingModule().getLocation())));
      }
      return retval;
    }));
  }

  @Override
  public IFieldDefinition getDefinition() {
    return this;
  }

  @Override
  public IFieldInstance getInlineInstance() {
    return this;
  }

  @Override
  public IFlagContainerSupport<IFlagInstance> getFlagContainer() {
    return ObjectUtils.notNull(flagContainer.get());
  }

  @Override
  public IValueConstrained getConstraintSupport() {
    return ObjectUtils.notNull(constraints.get());
  }

  // ----------------------------------------
  // - Start XmlBeans driven code - CPD-OFF -
  // ----------------------------------------

  /**
   * Get the underlying XML model.
   *
   * @return the XML model
   */
  protected GroupedInlineFieldDefinitionType getXmlObject() {
    return xmlObject;
  }

  @Override
  public String getFormalName() {
    return getXmlObject().isSetFormalName() ? getXmlObject().getFormalName() : null;
  }

  @Override
  public MarkupLine getDescription() {
    return getXmlObject().isSetDescription()
        ? MarkupStringConverter.toMarkupString(ObjectUtils.notNull(getXmlObject().getDescription()))
        : null;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlObject().getPropList()));
  }

  @Override
  public String getName() {
    return ObjectUtils.notNull(getXmlObject().getName());
  }

  @Override
  public Integer getIndex() {
    return getXmlObject().isSetIndex() ? getXmlObject().getIndex().intValue() : null;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getXmlObject().isSetAsType() ? ObjectUtils.notNull(getXmlObject().getAsType())
        : MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlObject().isSetRemarks()
        ? MarkupStringConverter.toMarkupString(ObjectUtils.notNull(getXmlObject().getRemarks()))
        : null;
  }

  @Override
  public String getDiscriminatorValue() {
    return getXmlObject().getDiscriminatorValue();
  }

  @Override
  public boolean hasJsonValueKeyFlagInstance() {
    return getXmlObject().isSetJsonValueKeyFlag() && getXmlObject().getJsonValueKeyFlag().isSetFlagRef();
  }

  @Override
  public IFlagInstance getJsonValueKeyFlagInstance() {
    IFlagInstance retval = null;
    if (getXmlObject().isSetJsonValueKeyFlag() && getXmlObject().getJsonValueKeyFlag().isSetFlagRef()) {
      retval = getFlagInstanceByName(ObjectUtils.notNull(getXmlObject().getJsonValueKeyFlag().getFlagRef()));
    }
    return retval;
  }

  @Override
  public String getJsonValueKeyName() {
    return getXmlObject().getJsonValueKey();
  }

  // -------------------------------------
  // - End XmlBeans driven code - CPD-ON -
  // -------------------------------------
}
