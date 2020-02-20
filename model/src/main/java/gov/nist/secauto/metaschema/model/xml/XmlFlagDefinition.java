/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.itl.metaschema.model.xml.DefineFlagDocument;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.info.definitions.AbstractFlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.DataType;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

import java.util.Collections;
import java.util.Map;

public class XmlFlagDefinition extends AbstractFlagDefinition<XmlMetaschema> implements FlagDefinition {
  private final DefineFlagDocument.DefineFlag xmlFlag;


  /**
   * Constructs a new Metaschema flag definition from an XML representation bound to Java objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlFlagDefinition(DefineFlagDocument.DefineFlag xmlFlag, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlFlag = xmlFlag;
  }

  @Override
  public String getName() {
    return getXmlFlag().getName();
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
  public DataType getDatatype() {
    DataType retval;
    if (getXmlFlag().isSetAsType()) {
      retval = DataType.lookup(getXmlFlag().getAsType());
    } else {
      // the default
      retval = DataType.STRING;
    }
    return retval;
  }

  protected DefineFlagDocument.DefineFlag getXmlFlag() {
    return xmlFlag;
  }

  @Override
  public FlagInstance getFlagInstanceByName(String name) {
    return null;
  }

  @Override
  public Map<String, ? extends FlagInstance> getFlagInstances() {
    return Collections.emptyMap();
  }
}
