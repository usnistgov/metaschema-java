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
import gov.nist.secauto.metaschema.model.common.Defaults;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.LocalInfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.instances.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.xml.XmlLocalAssemblyDefinition.InternalAssemblyDefinition;
import gov.nist.secauto.metaschema.model.xml.constraint.AssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.LocalAssemblyDefinitionType;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents a Metaschema assembly definition declared locally as an instance.
 */
public class XmlLocalAssemblyDefinition extends AbstractAssemblyInstance<InternalAssemblyDefinition> {
  private final LocalAssemblyDefinitionType xmlAssembly;
  private final InternalAssemblyDefinition assemblyDefinition;
  private IAssemblyConstraintSupport constraints;

  /**
   * Constructs a new Metaschema assembly definition from an XML representation bound to Java objects.
   * 
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent assembly definition
   */
  public XmlLocalAssemblyDefinition(LocalAssemblyDefinitionType xmlAssembly, AssemblyDefinition parent) {
    super(parent);
    this.xmlAssembly = xmlAssembly;
    this.assemblyDefinition = new InternalAssemblyDefinition();
  }

  /**
   * Get the underlying XML model.
   * 
   * @return the XML model
   */
  protected LocalAssemblyDefinitionType getXmlAssembly() {
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
    int retval = Defaults.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (getXmlAssembly().isSetMinOccurs()) {
      retval = getXmlAssembly().getMinOccurs().intValueExact();
    }
    return retval;
  }

  @Override
  public int getMaxOccurs() {
    int retval = Defaults.DEFAULT_GROUP_AS_MAX_OCCURS;
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
      retval = getXmlAssembly().getGroupAs().getInJson();
    }
    return retval;
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    XmlGroupAsBehavior retval = XmlGroupAsBehavior.UNGROUPED;
    if (getXmlAssembly().isSetGroupAs() && getXmlAssembly().getGroupAs().isSetInXml()) {
      retval = getXmlAssembly().getGroupAs().getInXml();
    }
    return retval;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlAssembly().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getRemarks()) : null;
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
    protected LocalAssemblyDefinitionType getXmlAssembly() {
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
      return XmlLocalAssemblyDefinition.this.getName();
    }

    @Override
    public String getUseName() {
      return getName();
    }

    @Override
    public String getXmlNamespace() {
      return XmlLocalAssemblyDefinition.this.getXmlNamespace();
    }

    @Override
    public boolean isRoot() {
      // a local assembly is never a root
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
    public List<? extends IIndexConstraint> getIndexConstraints() {
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
    public MarkupMultiline getRemarks() {
      return XmlLocalAssemblyDefinition.this.getRemarks();
    }
  }
}
