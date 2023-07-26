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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IModelContainer;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.net.URI;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class AbstractNodeItemFactory implements INodeItemFactory, INodeItemGenerator {
  @Override
  public IDocumentNodeItem newDocumentNodeItem(
      IRootAssemblyDefinition definition,
      URI documentUri,
      Object value) {
    return new DocumentNodeItemImpl(
        definition,
        value,
        documentUri,
        this);
  }

  @Override
  public IMetaschemaNodeItem newMetaschemaNodeItem(
      IMetaschema metaschema) {
    return new MetaschemaNodeItemImpl(
        metaschema,
        this);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      IFieldDefinition definition,
      IMetaschemaNodeItem metaschema) {
    return new FieldGlobalDefinitionNodeItemImpl(
        definition,
        metaschema,
        this);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      IFieldDefinition definition,
      URI baseUri) {
    return new FieldOrphanedDefinitionNodeItemImpl(
        definition,
        baseUri,
        this);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      IFieldInstance instance,
      IAssemblyNodeItem parent) {
    return new FieldInstanceNoValueNodeItemImpl(instance, parent, this);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      IFieldInstance instance,
      IAssemblyNodeItem parent,
      int position,
      Object value) {
    return new FieldInstanceNodeItemImpl(instance, parent, position, value, this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      IAssemblyDefinition definition,
      IMetaschemaNodeItem metaschema) {
    return new AssemblyGlobalDefinitionNodeItemImpl(
        definition,
        metaschema,
        this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      IAssemblyDefinition definition,
      URI baseUri) {
    return new AssemblyOrphanedDefinitionNodeItemImpl(
        definition,
        baseUri,
        this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      IAssemblyDefinition definition,
      URI baseUri,
      Object value) {
    return new AssemblyOrphanedDefinitionDataNodeItemImpl(
        definition,
        baseUri,
        value,
        this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      IAssemblyInstance instance,
      IAssemblyNodeItem parent) {
    IAssemblyNodeItem retval = null;
    if (!instance.getDefinition().isInline()) {
      // if not inline, need to check for a cycle
      IAssemblyNodeItem cycle = getCycledInstance(instance.getEffectiveName(), instance.getDefinition(), parent);
      if (cycle != null) {
        // generate a cycle wrapper of the original node item
        retval = new CycledAssemblyInstanceNodeItemImpl(instance, parent, cycle);
      }
    }

    if (retval == null) {
      retval = new AssemblyInstanceNoValueNodeItemImpl(instance, parent, this);
    }
    return retval;
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      IAssemblyInstance instance,
      IAssemblyNodeItem parent,
      int position,
      Object value) {
    return new AssemblyInstanceNodeItemImpl(instance, parent, position, value, this);
  }

  @Nullable
  private IAssemblyNodeItem getCycledInstance(
      @NonNull String effectiveName,
      @NonNull IAssemblyDefinition definition,
      @NonNull IAssemblyNodeItem parent) {
    IAssemblyNodeItem retval = null;

    IAssemblyDefinition parentDefinition = parent.getDefinition();
    if (parent.getName().equals(effectiveName) && parentDefinition.equals(definition)) {
      retval = parent;
    } else {
      IAssemblyNodeItem ancestor = parent.getParentContentNodeItem();
      if (ancestor != null) {
        retval = getCycledInstance(effectiveName, definition, ancestor);
      }
    }
    return retval;
  }

  @NonNull
  protected IModelNodeItem<?, ?> newModelItem(
      @NonNull INamedModelInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int index,
      @NonNull Object value) {
    @NonNull IModelNodeItem<?, ?> item;
    if (instance instanceof IAssemblyInstance) {
      item = newAssemblyNodeItem((IAssemblyInstance) instance, parent, index, value);
    } else if (instance instanceof IFieldInstance) {
      item = newFieldNodeItem((IFieldInstance) instance, parent, index, value);
    } else {
      throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
    }
    return item;
  }

  @NonNull
  protected IModelNodeItem<?, ?> newModelItem(
      @NonNull INamedModelInstance instance,
      @NonNull IAssemblyNodeItem parent) {
    @NonNull IModelNodeItem<?, ?> item;
    if (instance instanceof IAssemblyInstance) {
      item = newAssemblyNodeItem((IAssemblyInstance) instance, parent);
    } else if (instance instanceof IFieldInstance) {
      item = newFieldNodeItem((IFieldInstance) instance, parent);
    } else {
      throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
    }
    return item;
  }

  @NonNull
  protected Stream<INamedModelInstance> getNamedModelInstances(@NonNull IModelContainer container) {
    return ObjectUtils.notNull(container.getModelInstances().stream()
        .flatMap(instance -> {
          Stream<INamedModelInstance> retval;
          if (instance instanceof IAssemblyInstance || instance instanceof IFieldInstance) {
            retval = Stream.of((INamedModelInstance) instance);
          } else if (instance instanceof IChoiceInstance) {
            // descend into the choice
            retval = getNamedModelInstances((IChoiceInstance) instance);
          } else {
            throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
          }
          return retval;
        }));
  }

}
