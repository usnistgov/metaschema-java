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

import gov.nist.itl.metaschema.model.m4.xml.LocalAssemblyDefinition;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.LocalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.instances.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.instances.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.xml.XmlLocalAssemblyDefinition.InternalAssemblyDefinition;

import java.math.BigInteger;

public class XmlLocalAssemblyDefinition
    extends AbstractAssemblyInstance<InternalAssemblyDefinition> {
  private final LocalAssemblyDefinition xmlAssembly;
  private final InternalAssemblyDefinition assemblyDefinition;

  /**
   * Constructs a new Metaschema assembly definition from an XML representation bound to Java objects.
   * 
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent assembly definition
   */
  public XmlLocalAssemblyDefinition(LocalAssemblyDefinition xmlAssembly, AssemblyDefinition parent) {
    super(parent);
    this.xmlAssembly = xmlAssembly;
    this.assemblyDefinition = new InternalAssemblyDefinition();
  }

  /**
   * Get the underlying XML model.
   * 
   * @return the XML model
   */
  protected LocalAssemblyDefinition getXmlAssembly() {
    return xmlAssembly;
  }

  @Override
  public InternalAssemblyDefinition getDefinition() {
    return assemblyDefinition;
  }

  @Override
  public String getName() {
    return getXmlAssembly().getName();
  }

  @Override
  public String getUseName() {
    return getXmlAssembly().isSetUseName() ? getXmlAssembly().getUseName() : getDefinition().getUseName();
  }

  @Override
  public String getGroupAsName() {
    return getXmlAssembly().isSetGroupAs() ? getXmlAssembly().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    int retval = Metaschema.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlAssembly().isSetMinOccurs()) {
      retval = getXmlAssembly().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = Metaschema.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (getXmlAssembly().isSetMaxOccurs()) {
      Object value = getXmlAssembly().getMaxOccurs();
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
    if (getXmlAssembly().isSetGroupAs() && getXmlAssembly().getGroupAs().isSetInJson()) {
      retval = JsonGroupAsBehavior.lookup(getXmlAssembly().getGroupAs().getInJson());
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = XmlGroupAsBehavior.UNGROUPED;
    if (getXmlAssembly().isSetGroupAs() && getXmlAssembly().getGroupAs().isSetInXml()) {
      retval = XmlGroupAsBehavior.lookup(getXmlAssembly().getGroupAs().getInXml());
    }
    return retval;
  }

  public class InternalAssemblyDefinition
      extends AbstractXmlAssemblyDefinition<InternalAssemblyDefinition, XmlLocalAssemblyDefinition>
      implements LocalInfoElementDefinition<XmlLocalAssemblyDefinition> {

    /**
     * Create the corresponding definition for the local flag instance.
     */
    public InternalAssemblyDefinition() {
      super(XmlLocalAssemblyDefinition.this.getContainingDefinition().getContainingMetaschema());
    }

    @Override
    protected LocalAssemblyDefinition getXmlAssembly() {
      return XmlLocalAssemblyDefinition.this.getXmlAssembly();
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
    public ModuleScopeEnum getModuleScope() {
      return ModuleScopeEnum.LOCAL;
    }

    @Override
    public String getName() {
      return getXmlAssembly().getName();
    }

    @Override
    public String getUseName() {
      return getName();
    }

    @Override
    public boolean isRoot() {
      return false;
    }

    @Override
    public String getRootName() {
      return null;
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
    public XmlLocalAssemblyDefinition getDefiningInstance() {
      return XmlLocalAssemblyDefinition.this;
    }
  }
}
