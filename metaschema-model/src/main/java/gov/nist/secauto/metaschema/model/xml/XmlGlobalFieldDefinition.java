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
import gov.nist.itl.metaschema.model.m4.xml.GlobalFieldDefinition;
import gov.nist.itl.metaschema.model.m4.xml.LocalFlagDefinition;
import gov.nist.itl.metaschema.model.m4.xml.ScopeType;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.DataType;
import gov.nist.secauto.metaschema.model.definitions.GlobalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlGlobalFieldDefinition
    extends AbstractFieldDefinition
    implements GlobalInfoElementDefinition {
  private final GlobalFieldDefinition xmlField;
  private final Map<String, FlagInstance<?>> flagInstances;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound to Java objects.
   * 
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param metaschema
   *          the containing Metaschema
   */
  public XmlGlobalFieldDefinition(GlobalFieldDefinition xmlField, XmlMetaschema metaschema) {
    super(metaschema);
    this.xmlField = xmlField;

    // handle flags
    if (getXmlField().getFlagList().size() > 0 || getXmlField().getDefineFlagList().size() > 0) {
      XmlCursor cursor = getXmlField().newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:flag|$this/m:define-flag");

      Map<String, FlagInstance<?>> flagInstances = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        XmlObject obj = cursor.getObject();
        if (obj instanceof FlagDocument.Flag) {
          FlagInstance<?> flagInstance = new XmlFlagInstance((FlagDocument.Flag) obj, this);
          flagInstances.put(flagInstance.getUseName(), flagInstance);
        } else if (obj instanceof LocalFlagDefinition) {
          FlagInstance<?> flagInstance = new XmlLocalFlagDefinition((LocalFlagDefinition) obj, this);
          flagInstances.put(flagInstance.getUseName(), flagInstance);
        }
      }
      this.flagInstances = Collections.unmodifiableMap(flagInstances);
    } else {
      this.flagInstances = Collections.emptyMap();
    }
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  protected GlobalFieldDefinition getXmlField() {
    return xmlField;
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
  public Map<String, FlagInstance<?>> getFlagInstances() {
    return flagInstances;
  }

  @Override
  public DataType getDatatype() {
    DataType retval;
    if (getXmlField().isSetAsType()) {
      retval = DataType.lookup(getXmlField().getAsType());
    } else {
      // the default
      retval = Metaschema.DEFAULT_DATA_TYPE;
    }
    return retval;
  }

  @Override
  public boolean hasJsonValueKey() {
    return getXmlField().isSetJsonValueKey();
  }

  @Override
  public FlagInstance<?> getJsonValueKeyFlagInstance() {
    FlagInstance<?> retval = null;
    if (hasJsonValueKey() && getXmlField().getJsonValueKey().isSetFlagName()) {
      retval = getFlagInstanceByName(getXmlField().getJsonValueKey().getFlagName());
    }
    return retval;
  }

  @Override
  public String getJsonValueKeyName() {
    String retval = null;

    if (hasJsonValueKey()) {
      retval = getXmlField().getJsonValueKey().getStringValue();
    }

    if (retval == null || retval.isEmpty()) {
      retval = getDatatype().getDefaultValueKey();
    }
    return retval;
  }

  @Override
  public boolean hasJsonKey() {
    return getXmlField().isSetJsonKey();
  }

  @Override
  public FlagInstance<?> getJsonKeyFlagInstance() {
    FlagInstance<?> retval = null;
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
      retval = gov.nist.itl.metaschema.model.m4.xml.Boolean.YES.equals(getXmlField().getCollapsible());
    }
    return retval;
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    ModuleScopeEnum retval = Metaschema.DEFAULT_MODEL_SCOPE;
    if (getXmlField().isSetScope()) {
      switch (getXmlField().getScope().intValue()) {
      case ScopeType.INT_GLOBAL:
        retval = ModuleScopeEnum.INHERITED;
        break;
      case ScopeType.INT_LOCAL:
        retval = ModuleScopeEnum.LOCAL;
        break;
      default:
        throw new UnsupportedOperationException(getXmlField().getScope().toString());
      }
    }
    return retval;
  }

  @Override
  public String getUseName() {
    return getXmlField().isSetUseName() ? getXmlField().getUseName() : getName();
  }
}
