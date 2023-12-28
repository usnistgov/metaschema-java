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

package gov.nist.secauto.metaschema.databind.model.metaschema.impl;

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingContainerModelAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelNamedAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.GroupAs;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a named model instance associated with a Java object binding.
 *
 * @param <BINDING>
 *          the Java type of the associated object binding
 * @param <PARENT>
 *          the Java type of the parent container
 */
public abstract class AbstractInstanceModelNamed<
    BINDING,
    PARENT extends IBindingContainerModelAbsolute>
    extends AbstractInstanceModel<BINDING, PARENT>
    implements IBindingInstanceModelNamedAbsolute,
    IFeatureInstanceModelGroupAs {
  @NonNull
  private final Map<QName, Set<String>> properties;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final Lazy<IAssemblyNodeItem> boundNodeItem;

  /**
   * Construct a new bound named instance.
   *
   * @param binding
   *          the instance object bound to a Java class
   * @param bindingInstance
   *          the Metaschema module instance for the bound object
   * @param position
   *          the zero-based position of this bound object relative to its bound
   *          object siblings
   * @param parent
   *          the container containing this binding
   * @param properties
   *          the collection of properties associated with this instance, which
   *          may be empty
   * @param groupAs
   *          the instance grouping information
   */
  protected AbstractInstanceModelNamed(
      @NonNull BINDING binding,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      int position,
      @NonNull PARENT parent,
      @NonNull List<Property> properties,
      @Nullable GroupAs groupAs) {
    super(binding, parent);
    this.properties = ModelSupport.parseProperties(properties);
    this.groupAs = ModelSupport.groupAs(groupAs);
    this.boundNodeItem = ObjectUtils.notNull(
        Lazy.lazy(() -> (IAssemblyNodeItem) getContainingDefinition().getBoundNodeItem()
            .getModelItemsByName(bindingInstance.getEffectiveName())
            .get(position)));
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return properties;
  }

  @Override
  public IGroupAs getGroupAs() {
    return groupAs;
  }

  @SuppressWarnings("null")
  @Override
  public INodeItem getBoundNodeItem() {
    return boundNodeItem.get();
  }
}
