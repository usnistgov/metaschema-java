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

import gov.nist.secauto.metaschema.core.model.IFeatureInlinedDefinition;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingContainerModelAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceModelNamedAbsolute;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.GroupAs;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.Property;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class AbstractInstanceModelNamedInline<
    BINDING,
    DEFINITION extends IBindingDefinitionModel,
    INSTANCE extends IBindingInstanceModelNamedAbsolute,
    PARENT extends IBindingContainerModelAbsolute>
    extends AbstractInstanceModelNamed<BINDING, PARENT>
    implements IFeatureInlinedDefinition<DEFINITION, INSTANCE>,
    IFeatureBindingContainerFlag {

  /**
   * Construct a new bound named instance that represents an inline definition.
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
  protected AbstractInstanceModelNamedInline(
      @NonNull BINDING binding,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      int position,
      @NonNull PARENT parent,
      @NonNull List<Property> properties,
      @Nullable GroupAs groupAs) {
    super(binding, bindingInstance, position, parent, properties, groupAs);
  }

  @Override
  public String getJsonKeyFlagName() {
    IBindingInstanceFlag jsonKey = getJsonKeyFlagInstance();
    return jsonKey == null ? null : jsonKey.getEffectiveName();
  }
}
