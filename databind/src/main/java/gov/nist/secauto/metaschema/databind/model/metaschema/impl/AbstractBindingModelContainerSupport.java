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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IContainerModelAbsolute;
import gov.nist.secauto.metaschema.core.model.IContainerModelSupport;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.AssemblyReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FieldReference;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractBindingModelContainerSupport
    implements IContainerModelSupport<
        IModelInstanceAbsolute,
        INamedModelInstanceAbsolute,
        IFieldInstanceAbsolute,
        IAssemblyInstanceAbsolute> {

  protected static void addInstance(
      @NonNull IAssemblyInstanceAbsolute assembly,
      @NonNull List<IModelInstanceAbsolute> modelInstances,
      @NonNull Map<QName, INamedModelInstanceAbsolute> namedModelInstances,
      @NonNull Map<QName, IAssemblyInstanceAbsolute> assemblyInstances) {
    QName effectiveName = assembly.getXmlQName();
    modelInstances.add(assembly);
    namedModelInstances.put(effectiveName, assembly);
    assemblyInstances.put(effectiveName, assembly);
  }

  protected static void addInstance(
      @NonNull IFieldInstanceAbsolute field,
      @NonNull List<IModelInstanceAbsolute> modelInstances,
      @NonNull Map<QName, INamedModelInstanceAbsolute> namedModelInstances,
      @NonNull Map<QName, IFieldInstanceAbsolute> fieldInstances) {
    QName effectiveName = field.getXmlQName();
    modelInstances.add(field);
    namedModelInstances.put(effectiveName, field);
    fieldInstances.put(effectiveName, field);
  }

  @NonNull
  protected static IAssemblyInstanceAbsolute newInstance(
      @NonNull AssemblyReference obj,
      @NonNull IBoundInstanceModelGroupedAssembly objInstance,
      int position,
      @NonNull IContainerModelAbsolute parent) {
    IAssemblyDefinition owningDefinition = parent.getOwningDefinition();
    IModule module = owningDefinition.getContainingModule();

    String name = ObjectUtils.requireNonNull(obj.getRef());
    IAssemblyDefinition definition = module.getScopedAssemblyDefinitionByName(
        module.toModelQName(name));

    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve assembly reference '%s' in definition '%s' in module '%s'",
              name,
              owningDefinition.getName(),
              module.getShortName()));
    }
    return new InstanceModelAssemblyReference(obj, objInstance, position, definition, parent);
  }

  @NonNull
  protected static IFieldInstanceAbsolute newInstance(
      @NonNull FieldReference obj,
      @NonNull IBoundInstanceModelGroupedAssembly objInstance,
      int position,
      @NonNull IContainerModelAbsolute parent) {
    IAssemblyDefinition owningDefinition = parent.getOwningDefinition();
    IModule module = owningDefinition.getContainingModule();

    String name = ObjectUtils.requireNonNull(obj.getRef());
    IFieldDefinition definition = module.getScopedFieldDefinitionByName(
        module.toModelQName(name));
    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve field reference '%s' in definition '%s' in module '%s'",
              name,
              owningDefinition.getName(),
              module.getShortName()));
    }
    return new InstanceModelFieldReference(obj, objInstance, position, definition, parent);
  }
}
