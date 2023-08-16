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

package gov.nist.secauto.metaschema.core.model.xml; // NOPMD - excessive public methods and coupling is unavoidable

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFeatureFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFeatureInlinedDefinition;
import gov.nist.secauto.metaschema.core.model.IFeatureModelContainer;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.ExternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Represents a Metaschema assembly definition declared locally as an instance.
 */
class XmlInlineAssemblyDefinition
    extends AbstractAssemblyInstance {
  @NonNull
  private final InlineAssemblyDefinitionType xmlAssembly;
  @NonNull
  private final InternalAssemblyDefinition assemblyDefinition;

  /**
   * Constructs a new Metaschema assembly definition from an XML representation
   * bound to Java objects.
   *
   * @param xmlAssembly
   *          the XML representation bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  public XmlInlineAssemblyDefinition(
      @NonNull InlineAssemblyDefinitionType xmlAssembly,
      @NonNull IModelContainer parent) {
    super(parent);
    this.xmlAssembly = xmlAssembly;
    this.assemblyDefinition = new InternalAssemblyDefinition();
  }

  /**
   * Get the underlying XML model.
   *
   * @return the XML model
   */
  protected InlineAssemblyDefinitionType getXmlAssembly() {
    return xmlAssembly;
  }

  @Override
  public InternalAssemblyDefinition getDefinition() {
    return assemblyDefinition;
  }

  @Override
  public String getFormalName() {
    return getXmlAssembly().isSetFormalName() ? getXmlAssembly().getFormalName() : null;
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getDescription() {
    return getXmlAssembly().isSetDescription()
        ? MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription())
        : null;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return ModelFactory.toProperties(CollectionUtil.listOrEmpty(getXmlAssembly().getPropList()));
  }

  @SuppressWarnings("null")
  @Override
  public String getName() {
    return getXmlAssembly().getName();
  }

  @Override
  public String getUseName() {
    // an inline definition doesn't have a use name
    return null;
  }

  @Override
  public String getGroupAsName() {
    return getXmlAssembly().isSetGroupAs() ? getXmlAssembly().getGroupAs().getName() : null;
  }

  @Override
  public int getMinOccurs() {
    return XmlModelParser.getMinOccurs(getXmlAssembly().getMinOccurs());
  }

  @Override
  public int getMaxOccurs() {
    return XmlModelParser.getMaxOccurs(getXmlAssembly().getMaxOccurs());
  }

  @Override
  public JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return XmlModelParser.getJsonGroupAsBehavior(getXmlAssembly().getGroupAs());
  }

  @Override
  public XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return XmlModelParser.getXmlGroupAsBehavior(getXmlAssembly().getGroupAs());
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlAssembly().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlAssembly().getRemarks()) : null;
  }

  @Override
  public Object getValue(@NonNull Object parentValue) {
    // there is no value
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<?> getItemValues(Object instanceValue) {
    // there are no item values
    return Collections.emptyList();
  }

  /**
   * The corresponding definition for the local flag instance.
   */
  @SuppressWarnings("PMD.GodClass")
  private final class InternalAssemblyDefinition
      implements IAssemblyDefinition,
      IFeatureInlinedDefinition<IAssemblyInstance>,
      IFeatureModelContainer<IModelInstance, INamedModelInstance, IFieldInstance, IAssemblyInstance, IChoiceInstance>,
      IFeatureFlagContainer<IFlagInstance> {
    private final Lazy<XmlFlagContainerSupport> flagContainer;
    private final Lazy<XmlModelContainerSupport> modelContainer;
    private final Lazy<IModelConstrained> constraints;

    private InternalAssemblyDefinition() {
      this.flagContainer = Lazy.lazy(() -> new XmlFlagContainerSupport(xmlAssembly, this));
      this.modelContainer = Lazy.lazy(() -> new XmlModelContainerSupport(xmlAssembly, this));
      this.constraints = Lazy.lazy(() -> {
        IModelConstrained retval;
        if (getXmlAssembly().isSetConstraint()) {
          retval = new AssemblyConstraintSupport(
              ObjectUtils.notNull(getXmlAssembly().getConstraint()),
              ExternalModelSource.instance(
                  ObjectUtils.requireNonNull(getContainingMetaschema().getLocation())));
        } else {
          retval = new AssemblyConstraintSupport();
        }
        return retval;
      });
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    @NonNull
    public IAssemblyInstance getInlineInstance() {
      return XmlInlineAssemblyDefinition.this;
    }

    @Override
    public String getFormalName() {
      return XmlInlineAssemblyDefinition.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return XmlInlineAssemblyDefinition.this.getDescription();
    }

    @Override
    public @NonNull Map<QName, Set<String>> getProperties() {
      return XmlInlineAssemblyDefinition.this.getProperties();
    }

    @Override
    public ModuleScopeEnum getModuleScope() {
      return ModuleScopeEnum.LOCAL;
    }

    @Override
    public String getName() {
      return XmlInlineAssemblyDefinition.this.getName();
    }

    @Override
    public String getUseName() {
      // always use the name instead
      return null;
    }

    @Override
    public boolean isRoot() {
      // a local assembly is never a root
      return false;
    }

    @Override
    public String getRootName() {
      // a local assembly is never a root
      return null;
    }

    @SuppressWarnings("null")
    @Override
    public XmlFlagContainerSupport getFlagContainer() {
      return flagContainer.get();
    }

    @SuppressWarnings("null")
    @Override
    public XmlModelContainerSupport getModelContainer() {
      return modelContainer.get();
    }

    @SuppressWarnings("null")
    @Override
    public IModelConstrained getConstraintSupport() {
      return constraints.get();
    }

    @Override
    public MarkupMultiline getRemarks() {
      return XmlInlineAssemblyDefinition.this.getRemarks();
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      return XmlInlineAssemblyDefinition.super.getContainingDefinition().getContainingMetaschema();
    }
  }
}
