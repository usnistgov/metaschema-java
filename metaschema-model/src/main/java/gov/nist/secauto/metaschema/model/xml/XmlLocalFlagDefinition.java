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

import gov.nist.secauto.metaschema.model.IXmlMetaschema;
import gov.nist.secauto.metaschema.model.common.Defaults;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.definitions.ILocalDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlFlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlNamedModelDefinition;
import gov.nist.secauto.metaschema.model.instances.AbstractFlagInstance;
import gov.nist.secauto.metaschema.model.xml.constraint.ValueConstraintSupport;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.LocalFlagDefinitionType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XmlLocalFlagDefinition extends AbstractFlagInstance {
  @NotNull
  private final LocalFlagDefinitionType xmlFlag;
  @NotNull
  private final InternalFlagDefinition flagDefinition;
  private IValueConstraintSupport constraints;

  /**
   * Constructs a new Metaschema flag definition from an XML representation bound to Java objects.
   * 
   * @param xmlFlag
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent definition, which must be a definition type that can contain flags.
   */
  public XmlLocalFlagDefinition(@NotNull LocalFlagDefinitionType xmlFlag, @NotNull IXmlNamedModelDefinition parent) {
    super(parent);
    this.xmlFlag = xmlFlag;
    this.flagDefinition = new InternalFlagDefinition();
  }

  /**
   * Used to generate the instances for the constraints in a lazy fashion when the constraints are
   * first accessed.
   */
  protected synchronized void checkModelConstraints() {
    if (constraints == null) {
      if (getXmlFlag().isSetConstraint()) {
        constraints = new ValueConstraintSupport(getXmlFlag().getConstraint());
      } else {
        constraints = IValueConstraintSupport.NULL_CONSTRAINT;
      }
    }
  }

  /**
   * Get the underlying XML model.
   * 
   * @return the XML model
   */
  protected LocalFlagDefinitionType getXmlFlag() {
    return xmlFlag;
  }

  @Override
  public InternalFlagDefinition getDefinition() {
    return flagDefinition;
  }

  @Override
  public IXmlMetaschema getContainingMetaschema() {
    return getContainingDefinition().getContainingMetaschema();
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlFlag().getName();
  }

  @Override
  public String getUseName() {
    return getDefinition().getUseName();
  }

  @Override
  public String getXmlNamespace() {
    return null;
  }

  @Override
  public boolean isRequired() {
    return getXmlFlag().isSetRequired() ? getXmlFlag().getRequired() : Defaults.DEFAULT_FLAG_REQUIRED;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getXmlFlag().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlFlag().getRemarks()) : null;
  }

  public class InternalFlagDefinition implements IXmlFlagDefinition, ILocalDefinition<XmlLocalFlagDefinition> {

    /**
     * Create the corresponding definition for the local flag instance.
     */
    public InternalFlagDefinition() {
    }

    @Override
    public String getName() {
      return XmlLocalFlagDefinition.this.getName();
    }

    @Override
    public String getUseName() {
      return getName();
    }

    @Override
    public String getXmlNamespace() {
      return XmlLocalFlagDefinition.this.getXmlNamespace();
    }

    @Override
    public ModuleScopeEnum getModuleScope() {
      return ModuleScopeEnum.LOCAL;
    }

    @Override
    public String getFormalName() {
      return getXmlFlag().getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return MarkupStringConverter.toMarkupString(getXmlFlag().getDescription());
    }

    @SuppressWarnings("null")
    @Override
    public IJavaTypeAdapter<?> getDatatype() {
      IJavaTypeAdapter<?> retval;
      if (getXmlFlag().isSetAsType()) {
        retval = getXmlFlag().getAsType();
      } else {
        // the default
        retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
      }
      return retval;
    }

    @Override
    public XmlLocalFlagDefinition getDefiningInstance() {
      return XmlLocalFlagDefinition.this;
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
    public MarkupMultiline getRemarks() {
      return XmlLocalFlagDefinition.this.getRemarks();
    }

    @Override
    public IXmlMetaschema getContainingMetaschema() {
      return XmlLocalFlagDefinition.this.getContainingMetaschema();
    }
  }
}
