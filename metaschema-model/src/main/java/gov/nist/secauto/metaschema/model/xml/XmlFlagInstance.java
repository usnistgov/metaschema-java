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

import gov.nist.itl.metaschema.model.m4.xml.FlagDocument;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;
import gov.nist.secauto.metaschema.model.instances.AbstractFlagInstance;

public class XmlFlagInstance
    extends AbstractFlagInstance<XmlGlobalFlagDefinition> {
  private final FlagDocument.Flag xmlFlag;

  /**
   * Constructs a new Metaschema flag instance definition from an XML representation bound to Java
   * objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param parent
   *          the field definition this object is an instance of
   */
  public XmlFlagInstance(FlagDocument.Flag xmlFlag, ObjectDefinition parent) {
    super(parent);
    this.xmlFlag = xmlFlag;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  protected FlagDocument.Flag getXmlFlag() {
    return xmlFlag;
  }

  @Override
  public XmlGlobalFlagDefinition getDefinition() {
    return (XmlGlobalFlagDefinition) getContainingDefinition().getContainingMetaschema()
        .getFlagDefinitionByName(getName());
  }

  @Override
  public String getName() {
    return getXmlFlag().getRef();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlFlag().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlFlag().getRemarks()) : null;
  }
  /*
   * TODO: implement
   * 
   * 
   * 
   * @Override public String getAllowedValues() { String retval = null; if (xmlFlag.isSetRemarks()) {
   * retval = xmlFlag.getAllowedValues(); } else if (isReference()) { // TODO: ??? retval =
   * getFlagDefinition().getAllowedValues(); } return retval; }
   */

  @Override
  public boolean isRequired() {
    return getXmlFlag().isSetRequired() ? getXmlFlag().getRequired() : Metaschema.DEFAULT_REQUIRED;
  }

  @Override
  public String getUseName() {
    return getXmlFlag().isSetUseName() ? getXmlFlag().getUseName() : getDefinition().getUseName();
  }
}
