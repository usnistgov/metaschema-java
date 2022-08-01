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
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
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
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFlagDefinitionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

class XmlGlobalFlagDefinition implements IFlagDefinition {
  @NonNull
  private final GlobalFlagDefinitionType xmlFlag;
  @NonNull
  private final IMetaschema metaschema;
  private IValueConstraintSupport constraints;

  /**
   * Constructs a new Metaschema flag definition from an XML representation bound to Java objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalFlagDefinition(@NonNull GlobalFlagDefinitionType xmlFlag, @NonNull IMetaschema metaschema) {
    this.xmlFlag = xmlFlag;
    this.metaschema = metaschema;
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return metaschema;
  }

  /**
   * Get the underlying XML representation.
   * 
   * @return the underlying XML data
   */
  protected GlobalFlagDefinitionType getXmlFlag() {
    return xmlFlag;
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
        if (getXmlFlag().isSetConstraint()) {
          constraints = new ValueConstraintSupport(
              ObjectUtils.notNull(getXmlFlag().getConstraint()),
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
  public void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IMatchesConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IExpectConstraint constraint) {
    initModelConstraints().addConstraint(constraint);
  }

  @SuppressWarnings("null")
  @Override
  public ModuleScopeEnum getModuleScope() {
    return getXmlFlag().isSetScope() ? getXmlFlag().getScope() : IDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IFlagInstance getInlineInstance() {
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlFlag().getName();
  }

  @Override
  public String getUseName() {
    String retval = getXmlFlag().getUseName();
    if (retval == null) {
      retval = getName();
    }
    return retval;
  }

  @Override
  public String getFormalName() {
    return getXmlFlag().isSetFormalName() ? getXmlFlag().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlFlag().isSetDescription() ? MarkupStringConverter.toMarkupString(getXmlFlag().getDescription()) : null;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlFlag().getPropList()));
  }

  @SuppressWarnings("null")
  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getXmlFlag().isSetAsType() ? getXmlFlag().getAsType() : MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlFlag().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlFlag().getRemarks()) : null;
  }
}
