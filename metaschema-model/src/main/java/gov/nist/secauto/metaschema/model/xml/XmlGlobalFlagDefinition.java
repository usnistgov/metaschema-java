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

import gov.nist.itl.metaschema.model.m4.xml.AllowedValuesType;
import gov.nist.itl.metaschema.model.m4.xml.GlobalFlagDefinitionType;
import gov.nist.itl.metaschema.model.m4.xml.IndexConstraintType;
import gov.nist.itl.metaschema.model.m4.xml.MatchesConstraintType;
import gov.nist.secauto.metaschema.datatypes.DataTypes;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.definitions.AbstractInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.GlobalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.MetaschemaDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class XmlGlobalFlagDefinition extends AbstractInfoElementDefinition
    implements FlagDefinition, GlobalInfoElementDefinition {
  private final GlobalFlagDefinitionType xmlFlag;
  private List<IConstraint> constraints;

  /**
   * Constructs a new Metaschema flag definition from an XML representation bound to Java objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalFlagDefinition(GlobalFlagDefinitionType xmlFlag, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlFlag = xmlFlag;
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
   * Used to generate the instances for the assembly's model in a lazy fashion when the model is first
   * accessed.
   */
  protected void generateModelConstraints() {
    // handle constraints
    if (getXmlFlag().isSetConstraint()) {
      XmlCursor cursor = getXmlFlag().getConstraint().newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:allowed-values|$this/m:matches|$this/m:index-has-key");

      List<IConstraint> constraints = new LinkedList<>();
      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof AllowedValuesType) {
          XmlAllowedValuesConstraint constraint
              = new XmlAllowedValuesConstraint((AllowedValuesType) obj, MetapathExpression.CONTEXT_NODE);
          constraints.add(constraint);
        } else if (obj instanceof MatchesConstraintType) {
          XmlMatchesConstraint constraint
              = new XmlMatchesConstraint((MatchesConstraintType) obj, MetapathExpression.CONTEXT_NODE);
          constraints.add(constraint);
        } else if (obj instanceof IndexConstraintType) {
          XmlIndexHasKeyConstraint constraint = new XmlIndexHasKeyConstraint((IndexConstraintType) obj);
          constraints.add(constraint);
        }
      }
      this.constraints = constraints.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(constraints);
    } else {
      this.constraints = Collections.emptyList();
    }
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    synchronized (this) {
      if (constraints == null) {
        generateModelConstraints();
      }
    }
    return constraints;
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    ModuleScopeEnum retval = MetaschemaDefinition.DEFAULT_DEFINITION_MODEL_SCOPE;
    if (getXmlFlag().isSetScope()) {
      retval = getXmlFlag().getScope();
    }
    return retval;
  }

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
  public String getXmlNamespace() {
    return null;
    // return getContainingMetaschema().getXmlNamespace().toString();
  }

  @Override
  public String getFormalName() {
    return getXmlFlag().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return MarkupStringConverter.toMarkupString(getXmlFlag().getDescription());
  }

  @Override
  public DataTypes getDatatype() {
    DataTypes retval;
    if (getXmlFlag().isSetAsType()) {
      retval = getXmlFlag().getAsType();
    } else {
      // the default
      retval = DataTypes.DEFAULT_DATA_TYPE;
    }
    return retval;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlFlag().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlFlag().getRemarks()) : null;
  }
}
