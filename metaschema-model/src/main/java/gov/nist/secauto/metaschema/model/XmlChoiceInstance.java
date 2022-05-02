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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.common.AbstractChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.xmlbeans.ChoiceDocument;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class XmlChoiceInstance
    extends AbstractChoiceInstance {
  @NotNull
  private final ChoiceDocument.Choice xmlChoice;
  private XmlModelContainerSupport modelContainer;

  /**
   * Constructs a mutually exclusive choice between two possible objects.
   * 
   * @param xmlChoice
   *          the XML for the choice definition bound to Java objects
   * @param containingAssembly
   *          the parent assembly definition that contains this choice
   */
  public XmlChoiceInstance(
      @NotNull ChoiceDocument.Choice xmlChoice,
      @NotNull IAssemblyDefinition containingAssembly) {
    super(containingAssembly);
    this.xmlChoice = xmlChoice;

  }

  @Override
  public String getGroupAsName() {
    // a choice does not have a name
    return null;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  @NotNull
  protected ChoiceDocument.Choice getXmlChoice() {
    return xmlChoice;
  }

  /**
   * Lazy initialize the model for this choice.
   */
  @SuppressWarnings("null")
  protected void initModelContainer() {
    synchronized (this) {
      if (modelContainer == null) {
        modelContainer = new XmlModelContainerSupport(getXmlChoice(), getContainingDefinition());
      }
    }
  }

  private Map<@NotNull String, ? extends INamedModelInstance> getNamedModelInstanceMap() {
    initModelContainer();
    return modelContainer.getNamedModelInstanceMap();
  }

  @Override
  public @Nullable INamedModelInstance getModelInstanceByName(String name) {
    return getNamedModelInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public @NotNull Collection<@NotNull ? extends INamedModelInstance> getNamedModelInstances() {
    return getNamedModelInstanceMap().values();
  }

  private Map<@NotNull String, ? extends IFieldInstance> getFieldInstanceMap() {
    initModelContainer();
    return modelContainer.getFieldInstanceMap();
  }

  @Override
  public IFieldInstance getFieldInstanceByName(String name) {
    return getFieldInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFieldInstance> getFieldInstances() {
    return getFieldInstanceMap().values();
  }

  private Map<@NotNull String, ? extends IAssemblyInstance> getAssemblyInstanceMap() {
    initModelContainer();
    return modelContainer.getAssemblyInstanceMap();
  }

  @Override
  public IAssemblyInstance getAssemblyInstanceByName(String name) {
    return getAssemblyInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IAssemblyInstance> getAssemblyInstances() {
    return getAssemblyInstanceMap().values();
  }

  @Override
  public List<@NotNull ? extends IChoiceInstance> getChoiceInstances() {
    initModelContainer();
    return modelContainer.getChoiceInstances();
  }

  @Override
  public List<@NotNull ? extends IModelInstance> getModelInstances() {
    initModelContainer();
    return modelContainer.getModelInstances();
  }

  @Override
  public MarkupMultiline getRemarks() {
    // TODO: add support if remarks are added
    return null;
  }
}
