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

import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.definitions.GlobalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.MetaschemaDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.xml.constraint.AssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.xml.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.GlobalAssemblyDefinitionType;

import java.util.List;

public class XmlGlobalAssemblyDefinition
    extends AbstractXmlAssemblyDefinition<XmlGlobalAssemblyDefinition, XmlAssemblyInstance>
    implements GlobalInfoElementDefinition {
  private final GlobalAssemblyDefinitionType xmlAssembly;
  private IAssemblyConstraintSupport constraints;

  /**
   * Constructs a new Metaschema Assembly definition from an XML representation bound to Java objects.
   * 
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalAssemblyDefinition(GlobalAssemblyDefinitionType xmlAssembly, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlAssembly = xmlAssembly;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  @Override
  protected GlobalAssemblyDefinitionType getXmlAssembly() {
    return xmlAssembly;
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected synchronized void checkModelConstraints() {
    if (constraints == null) {
      if (getXmlAssembly().isSetConstraint()) {
        constraints = new AssemblyConstraintSupport(getXmlAssembly().getConstraint());
      } else {
        constraints = IAssemblyConstraintSupport.NULL_CONSTRAINT;
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
  public List<? extends IIndexConstraint> getIndexContraints() {
    checkModelConstraints();
    return constraints.getIndexContraints();
  }

  @Override
  public List<? extends IUniqueConstraint> getUniqueConstraints() {
    checkModelConstraints();
    return constraints.getUniqueConstraints();
  }

  @Override
  public List<? extends ICardinalityConstraint> getHasCardinalityConstraints() {
    checkModelConstraints();
    return constraints.getHasCardinalityConstraints();
  }

  @Override
  public String getName() {
    return getXmlAssembly().getName();
  }

  @Override
  public String getUseName() {
    return getXmlAssembly().isSetUseName() ? getXmlAssembly().getUseName() : getName();
  }

  @Override
  public String getXmlNamespace() {
    return getContainingMetaschema().getXmlNamespace().toString();
  }

  @Override
  public String getFormalName() {
    return getXmlAssembly().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription());
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlAssembly().isSetJsonKey();
  }

  @Override
  public FlagInstance<?> getJsonKeyFlagInstance() {
    FlagInstance<?> retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlAssembly().getJsonKey().getFlagName());
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

  @Override
  public ModuleScopeEnum getModuleScope() {
    ModuleScopeEnum retval = MetaschemaDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
    if (getXmlAssembly().isSetScope()) {
      retval = getXmlAssembly().getScope();
    }
    return retval;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlAssembly().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getRemarks()) : null;
  }
}
