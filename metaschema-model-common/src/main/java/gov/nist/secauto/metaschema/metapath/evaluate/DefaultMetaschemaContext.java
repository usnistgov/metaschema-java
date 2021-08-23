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

package gov.nist.secauto.metaschema.metapath.evaluate;

import gov.nist.secauto.metaschema.metapath.ast.IPathExpression;
import gov.nist.secauto.metaschema.metapath.ast.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.RootAssemblyDefinitionInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultMetaschemaContext implements IMetaschemaContext {
  private final IInstanceSet instanceSet;

  public DefaultMetaschemaContext(List<? extends IAssemblyDefinition> rootDefinitions) {
    List<? extends IInstance> rootInstances
        = rootDefinitions.stream().map(x -> new RootAssemblyDefinitionInstance(x)).collect(Collectors.toList());
    this.instanceSet = new DefaultInstanceSet(rootInstances);
  }

  public DefaultMetaschemaContext(IInstanceSet instanceSet) {
    this.instanceSet = instanceSet;
  }

  @Override
  public IInstanceSet getInstanceSet() {
    return instanceSet;
  }

  @Override
  public IMetaschemaContext newInstanceMetaschemaContext(IInstanceSet instanceSet) {
    IMetaschemaContext retval;
    if (getInstanceSet().equals(instanceSet)) {
      retval = this;
    } else {
      retval = new DefaultMetaschemaContext(instanceSet);
    }
    return retval;
  }


  @Override
  public IInstanceSet getChildFlag(Predicate<IInstance> filter) {
    List<IInstance> result = new LinkedList<>();
    for (IInstance instance : getInstanceSet().getInstances()) {
      Collection<? extends IFlagInstance> flags = null;
      if (instance instanceof IAssemblyInstance) {
        flags = ((IAssemblyInstance) instance).getDefinition().getFlagInstances().values();
      } else if (instance instanceof IFieldInstance) {
        flags = ((IFieldInstance) instance).getDefinition().getFlagInstances().values();
      }

      if (flags != null && !flags.isEmpty()) {
        flags.stream().filter(filter).collect(Collectors.toCollection(() -> result));
      }
    }
    return new DefaultInstanceSet(result);
  }

  @Override
  public IInstanceSet getChildModelInstance(Predicate<IInstance> filter) {
    List<IModelInstance> result = new LinkedList<>();
    for (IInstance instance : getInstanceSet().getInstances()) {
      Collection<? extends IModelInstance> modelInstances = null;
      if (instance instanceof IAssemblyInstance) {
        modelInstances = ((IAssemblyInstance) instance).getDefinition().getModelInstances();
      }

      if (modelInstances != null && !modelInstances.isEmpty()) {
        modelInstances.stream().filter(filter).collect(Collectors.toCollection(() -> result));
      }
    }
    return new DefaultInstanceSet(result);
  }

  @Override
  public IInstanceSet search(RelativeDoubleSlashPath expr) {
    // TODO: implement
    throw new UnsupportedOperationException();
  }

  @Override
  public IInstanceSet search(IPathExpression expr) {
    // TODO: implement
    throw new UnsupportedOperationException();
  }
}
