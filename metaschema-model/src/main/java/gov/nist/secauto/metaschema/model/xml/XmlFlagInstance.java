/**
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

import gov.nist.itl.metaschema.model.xml.Boolean;
import gov.nist.itl.metaschema.model.xml.FlagDocument;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.DataType;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;
import gov.nist.secauto.metaschema.model.info.instances.AbstractFlagInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

import java.util.Collections;
import java.util.Map;

public class XmlFlagInstance extends AbstractFlagInstance {
  private final FlagDocument.Flag xmlFlag;
  private final LocalFlagDefinition localFlagDefinition;

  /**
   * Constructs a new Metaschema flag instance definition from an XML representation bound to Java
   * objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param parent
   *          the field definition this object is an instance of
   */
  public XmlFlagInstance(FlagDocument.Flag xmlFlag, ManagedObject parent) {
    super(parent);
    this.xmlFlag = xmlFlag;

    if (xmlFlag.isSetName()) {
      localFlagDefinition = new LocalFlagDefinition();
    } else {
      localFlagDefinition = null;
    }
  }

  @Override
  protected FlagDefinition getLocalFlagDefinition() {
    return localFlagDefinition;
  }

  @Override
  public String getName() {
    FlagDocument.Flag xmlFlag = getXmlFlag();
    return xmlFlag.isSetRef() ? xmlFlag.getRef() : xmlFlag.getName();
  }

  @Override
  public String getFormalName() {
    String retval = null;
    if (getXmlFlag().isSetFormalName()) {
      retval = getXmlFlag().getFormalName();
    } else if (isReference()) {
      retval = getDefinition().getFormalName();
    }
    return retval;
  }

  @Override
  public MarkupLine getDescription() {
    MarkupLine retval = null;
    if (getXmlFlag().isSetDescription()) {
      retval = MarkupStringConverter.toMarkupString(getXmlFlag().getDescription());
    } else if (isReference()) {
      retval = getDefinition().getDescription();
    }
    return retval;
  }

  /*
   * TODO: implement
   * 
   * @Override public String getRemarks() { String retval = null; if (xmlFlag.isSetRemarks()) { retval
   * = xmlFlag.getRemarks(); } else if (isReference()) { // TODO: append? retval =
   * getFlagDefinition().getRemarks(); } return retval; }
   * 
   * 
   * @Override public String getAllowedValues() { String retval = null; if (xmlFlag.isSetRemarks()) {
   * retval = xmlFlag.getAllowedValues(); } else if (isReference()) { // TODO: ??? retval =
   * getFlagDefinition().getAllowedValues(); } return retval; }
   */
  @Override
  public DataType getDatatype() {
    DataType retval;
    if (getXmlFlag().isSetAsType()) {
      retval = DataType.lookup(getXmlFlag().getAsType());
    } else if (isReference()) {
      retval = getDefinition().getDatatype();
    } else {
      // the default
      retval = DataType.STRING;
    }
    return retval;
  }

  @Override
  public boolean isRequired() {
    boolean retval = false;
    if (getXmlFlag().isSetRequired()) {
      Boolean.Enum required = getXmlFlag().getRequired();
      if (Boolean.INT_YES == required.intValue()) {
        retval = true;
      }
    }
    return retval;
  }

  protected FlagDocument.Flag getXmlFlag() {
    return xmlFlag;
  }

  /**
   * A factory class with static methods for creating instances of this type.
   */
  private class LocalFlagDefinition implements FlagDefinition {

    public LocalFlagDefinition() {
    }

    @Override
    public String getName() {
      return XmlFlagInstance.this.getName();
    }

    @Override
    public Type getType() {
      return Type.FLAG;
    }

    @Override
    public Metaschema getContainingMetaschema() {
      return XmlFlagInstance.this.getContainingMetaschema();
    }

    @Override
    public String getFormalName() {
      return XmlFlagInstance.this.getFormalName();
    }

    @Override
    public DataType getDatatype() {
      return XmlFlagInstance.this.getDatatype();
    }

    @Override
    public MarkupLine getDescription() {
      return XmlFlagInstance.this.getDescription();
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
}
