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

import gov.nist.itl.metaschema.model.m4.xml.GlobalFlagDefinition;
import gov.nist.itl.metaschema.model.m4.xml.ScopeType;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.AbstractFlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.DataType;
import gov.nist.secauto.metaschema.model.definitions.GlobalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;

public class XmlGlobalFlagDefinition
    extends AbstractFlagDefinition
    implements GlobalInfoElementDefinition {
  private final GlobalFlagDefinition xmlFlag;

  /**
   * Constructs a new Metaschema flag definition from an XML representation bound to Java objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalFlagDefinition(GlobalFlagDefinition xmlFlag, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlFlag = xmlFlag;
  }

  /**
   * Get the underlying XML representation.
   * 
   * @return the underlying XML data
   */
  protected GlobalFlagDefinition getXmlFlag() {
    return xmlFlag;
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    ModuleScopeEnum retval = Metaschema.DEFAULT_MODEL_SCOPE;
    if (getXmlFlag().isSetScope()) {
      switch (getXmlFlag().getScope().intValue()) {
      case ScopeType.INT_GLOBAL:
        retval = ModuleScopeEnum.INHERITED;
        break;
      case ScopeType.INT_LOCAL:
        retval = ModuleScopeEnum.LOCAL;
        break;
      default:
        throw new UnsupportedOperationException(getXmlFlag().getScope().toString());
      }
    }
    return retval;
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
    return getXmlFlag().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return MarkupStringConverter.toMarkupString(getXmlFlag().getDescription());
  }

  @Override
  public String getName() {
    return getXmlFlag().getName();
  }

  @Override
  public DataType getDatatype() {
    DataType retval;
    if (getXmlFlag().isSetAsType()) {
      retval = DataType.lookup(getXmlFlag().getAsType());
    } else {
      // the default
      retval = Metaschema.DEFAULT_DATA_TYPE;
    }
    return retval;
  }
}
