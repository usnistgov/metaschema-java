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

package gov.nist.secauto.metaschema.core.model.xml;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ChoiceType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class XmlChoiceInstance
    extends AbstractChoiceInstance {
  @NonNull
  private final ChoiceType xmlChoice;
  private XmlModelContainerSupport modelContainer;

  /**
   * Constructs a mutually exclusive choice between two possible objects.
   *
   * @param xmlChoice
   *          the XML for the choice definition bound to Java objects
   * @param parent
   *          the parent container, either a choice or assembly
   */
  public XmlChoiceInstance(
      @NonNull ChoiceType xmlChoice,
      @NonNull IModelContainer parent) {
    super(parent);
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
  @NonNull
  protected ChoiceType getXmlChoice() {
    return xmlChoice;
  }

  /**
   * Lazy initialize the model for this choice.
   */
  protected void initModelContainer() {
    synchronized (this) {
      if (modelContainer == null) {
        modelContainer = new XmlModelContainerSupport(getXmlChoice(), getOwningDefinition());
      }
    }
  }

  private Map<String, ? extends INamedModelInstance> getNamedModelInstanceMap() {
    initModelContainer();
    return modelContainer.getNamedModelInstanceMap();
  }

  @Override
  public @Nullable INamedModelInstance getModelInstanceByName(String name) {
    return getNamedModelInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public @NonNull Collection<? extends INamedModelInstance> getNamedModelInstances() {
    return getNamedModelInstanceMap().values();
  }

  private Map<String, ? extends IFieldInstance> getFieldInstanceMap() {
    initModelContainer();
    return modelContainer.getFieldInstanceMap();
  }

  @Override
  public IFieldInstance getFieldInstanceByName(String name) {
    return getFieldInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IFieldInstance> getFieldInstances() {
    return getFieldInstanceMap().values();
  }

  private Map<String, ? extends IAssemblyInstance> getAssemblyInstanceMap() {
    initModelContainer();
    return modelContainer.getAssemblyInstanceMap();
  }

  @Override
  public IAssemblyInstance getAssemblyInstanceByName(String name) {
    return getAssemblyInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyInstance> getAssemblyInstances() {
    return getAssemblyInstanceMap().values();
  }

  @Override
  public List<? extends IChoiceInstance> getChoiceInstances() {
    initModelContainer();
    return modelContainer.getChoiceInstances();
  }

  @Override
  public Collection<? extends IModelInstance> getModelInstances() {
    initModelContainer();
    return modelContainer.getModelInstances();
  }

  @Override
  public MarkupMultiline getRemarks() {
    // TODO: add support when remarks are added
    return null;
  }
}
