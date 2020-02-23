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

import gov.nist.itl.metaschema.model.xml.DefineFieldDocument;
import gov.nist.itl.metaschema.model.xml.ExtensionType;
import gov.nist.itl.metaschema.model.xml.FlagDocument;
import gov.nist.itl.metaschema.model.xml.JsonValueKeyDocument.JsonValueKey;
import gov.nist.itl.metaschema.model.xml.binding.DefineFieldBindingDocument;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.configuration.FieldBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.definitions.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.DataType;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlFieldDefinition extends AbstractFieldDefinition<XmlMetaschema> implements FieldDefinition {

  protected static FieldBindingConfiguration getBindingConfiguration(DefineFieldDocument.DefineField xmlField) {
    FieldBindingConfiguration retval = null;
    if (xmlField.isSetExtensions()) {
      DefineFieldDocument.DefineField.Extensions extensions = xmlField.getExtensions();
      for (ExtensionType extensionInstance : extensions.getDefineFieldExtensionList()) {
        System.out.println("Extension Class: " + extensionInstance.getClass().getName());
        if (extensionInstance instanceof DefineFieldBindingDocument.DefineFieldBinding) {
          DefineFieldBindingDocument.DefineFieldBinding modelConfig
              = (DefineFieldBindingDocument.DefineFieldBinding) extensionInstance;
          if (modelConfig.isSetJava()) {
            DefineFieldBindingDocument.DefineFieldBinding.Java modelJava = modelConfig.getJava();

            retval = new FieldBindingConfiguration(modelJava.getClassName(), modelJava.getBaseClassName(),
                modelJava.getInterfaceNameList());
            break;
          }
        }
      }
    }

    if (retval == null) {
      retval = FieldBindingConfiguration.NULL_CONFIG;
    }
    return retval;
  }

  private final DefineFieldDocument.DefineField xmlField;
  private final Map<String, XmlFlagInstance> flagInstances;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound to Java objects.
   * 
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlFieldDefinition(DefineFieldDocument.DefineField xmlField, XmlMetaschema metaschema) {
    super(getBindingConfiguration(xmlField), metaschema);
    this.xmlField = xmlField;

    int numFlags = xmlField.sizeOfFlagArray();
    if (numFlags > 0) {
      Map<String, XmlFlagInstance> flagInstances = new LinkedHashMap<>();
      for (FlagDocument.Flag xmlFlag : xmlField.getFlagList()) {
        XmlFlagInstance flagInstance = new XmlFlagInstance(xmlFlag, this);
        flagInstances.put(flagInstance.getName(), flagInstance);
      }
      this.flagInstances = Collections.unmodifiableMap(flagInstances);
    } else {
      flagInstances = Collections.emptyMap();
    }
  }

  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public String getFormalName() {
    return getXmlField().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return MarkupStringConverter.toMarkupString(getXmlField().getDescription());
  }

  @Override
  public Map<String, XmlFlagInstance> getFlagInstances() {
    return flagInstances;
  }

  @Override
  public DataType getDatatype() {
    DataType retval;
    if (getXmlField().isSetAsType()) {
      retval = DataType.lookup(getXmlField().getAsType());
    } else {
      // the default
      retval = DataType.STRING;
    }
    return retval;
  }

  protected DefineFieldDocument.DefineField getXmlField() {
    return xmlField;
  }

  @Override
  public boolean hasJsonValueKey() {
    return getXmlField().isSetJsonValueKey();
  }

  @Override
  public Object getJsonValueKey() {
    Object retval = null;
    if (getXmlField().isSetJsonValueKey()) {
      JsonValueKey jvk = getXmlField().getJsonValueKey();
      if (jvk.isSetFlagName()) {
        retval = getFlagInstances().get(jvk.getFlagName());
      } else {
        retval = jvk.getStringValue();
      }
    }
    return retval;
  }

  @Override
  public FlagInstance getJsonValueKeyFlagInstance() {
    FlagInstance retval = null;
    if (getXmlField().isSetJsonValueKey()) {
      retval = getFlagInstanceByName(getXmlField().getJsonValueKey().getFlagName());
    }
    return retval;
  }

  @Override
  public String getJsonValueKeyName() {
    String retval = null;

    if (getXmlField().isSetJsonValueKey()) {
      retval = getXmlField().getJsonValueKey().getStringValue();
    }

    if (retval == null || retval.isEmpty()) {
      switch (getDatatype()) {
      case MARKUP_LINE:
        retval = "RICHTEXT";
        break;
      case MARKUP_MULTILINE:
        retval = "PROSE";
        break;
      default:
        retval = "STRVALUE";
      }
    }
    return retval;
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlField().isSetJsonKey();
  }

  @Override
  public FlagInstance getJsonKeyFlagInstance() {
    FlagInstance retval = null;
    if (hasJsonKey()) {
      retval = getFlagInstanceByName(getXmlField().getJsonKey().getFlagName());
    }
    return retval;
  }

  @Override
  public boolean isCollapsible() {
    // default value
    boolean retval = true;
    if (getXmlField().isSetCollapsible()) {
      retval = gov.nist.itl.metaschema.model.xml.Boolean.YES.equals(getXmlField().getCollapsible());
    }
    return retval;
  }
}
