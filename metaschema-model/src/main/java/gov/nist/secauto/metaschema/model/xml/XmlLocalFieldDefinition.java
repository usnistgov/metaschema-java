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

import gov.nist.itl.metaschema.model.m4.xml.FieldDocument;
import gov.nist.itl.metaschema.model.m4.xml.FlagDocument;
import gov.nist.itl.metaschema.model.m4.xml.LocalFieldDefinition;
import gov.nist.itl.metaschema.model.m4.xml.LocalFlagDefinition;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.DataType;
import gov.nist.secauto.metaschema.model.definitions.LocalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.instances.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.instances.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.xml.XmlLocalFieldDefinition.InternalFieldDefinition;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlLocalFieldDefinition
    extends AbstractFieldInstance<InternalFieldDefinition> {
  private final LocalFieldDefinition xmlField;
  private final InternalFieldDefinition fieldDefinition;

  /**
   * Constructs a new Metaschema field definition from an XML representation bound to Java objects.
   * 
   * @param xmlField
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent assembly definition
   */
  public XmlLocalFieldDefinition(LocalFieldDefinition xmlField, AssemblyDefinition parent) {
    super(parent);
    this.xmlField = xmlField;
    this.fieldDefinition = new InternalFieldDefinition();
  }

  /**
   * Get the underlying XML model.
   * 
   * @return the XML model
   */
  protected LocalFieldDefinition getXmlField() {
    return xmlField;
  }

  @Override
  public InternalFieldDefinition getDefinition() {
    return fieldDefinition;
  }

  @Override
  public boolean hasXmlWrapper() {
    boolean retval;
    if (DataType.MARKUP_MULTILINE.equals(getDefinition().getDatatype())) {
      // default value
      retval = Metaschema.DEFAULT_FIELD_XML_WRAPPER;
      if (getXmlField().isSetInXml()) {
        retval = FieldDocument.Field.InXml.WITH_WRAPPER.equals(getXmlField().getInXml());
      }
    } else {
      // All other data types get "wrapped"
      retval = true;
    }
    return retval;
  }

  @Override
  public String getName() {
    return getXmlField().getName();
  }

  @Override
  public String getUseName() {
    return getXmlField().isSetUseName() ? getXmlField().getUseName() : getDefinition().getUseName();
  }

  @Override
  public String getGroupAsName() {
    return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    int retval = Metaschema.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlField().isSetMinOccurs()) {
      retval = getXmlField().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = Metaschema.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (getXmlField().isSetMaxOccurs()) {
      Object value = getXmlField().getMaxOccurs();
      if (value instanceof String) {
        // unbounded
        retval = -1;
      } else if (value instanceof BigInteger) {
        retval = ((BigInteger) value).intValueExact();
      }
    }
    return retval;
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    JsonGroupAsBehavior retval = JsonGroupAsBehavior.SINGLETON_OR_LIST;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInJson()) {
      retval = JsonGroupAsBehavior.lookup(getXmlField().getGroupAs().getInJson());
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = XmlGroupAsBehavior.UNGROUPED;
    if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInXml()) {
      retval = XmlGroupAsBehavior.lookup(getXmlField().getGroupAs().getInXml());
    }
    return retval;
  }

  public class InternalFieldDefinition
      extends AbstractFieldDefinition
      implements LocalInfoElementDefinition<XmlLocalFieldDefinition> {
    private final Map<String, FlagInstance<?>> flagInstances;

    /**
     * Create the corresponding definition for the local flag instance.
     */
    public InternalFieldDefinition() {
      super(XmlLocalFieldDefinition.this.getContainingDefinition().getContainingMetaschema());

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

    @Override
    public String getFormalName() {
      return getXmlField().getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return MarkupStringConverter.toMarkupString(getXmlField().getDescription());
    }

    @Override
    public ModuleScopeEnum getModuleScope() {
      return ModuleScopeEnum.LOCAL;
    }

    @Override
    public String getName() {
      return getXmlField().getName();
    }

    @Override
    public String getUseName() {
      return getName();
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
    public boolean isCollapsible() {
      return getXmlField().isSetCollapsible() ? getXmlField().getCollapsible() : Metaschema.DEFAULT_COLLAPSIBLE;
    }

    @Override
    public XmlLocalFieldDefinition getDefiningInstance() {
      return XmlLocalFieldDefinition.this;
    }

    @Override
    public Map<String, FlagInstance<?>> getFlagInstances() {
      return flagInstances;
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
  }
}
