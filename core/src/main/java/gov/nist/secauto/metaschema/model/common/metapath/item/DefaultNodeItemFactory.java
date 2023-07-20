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
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IModelContainer;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class DefaultNodeItemFactory implements INodeItemFactory {
  @NonNull
  static final INodeItemFactory SINGLETON = new DefaultNodeItemFactory();

  /**
   * Get the singleton instance of this node factory.
   *
   * @return the node factory instance
   */
  @NonNull
  public static INodeItemFactory instance() {
    return SINGLETON;
  }

  private DefaultNodeItemFactory() {
    // prevent construction
  }

  @Override
  public IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagDefinition definition,
      @Nullable URI baseUri) {
    return new FlagDefinitionNodeItemImpl(definition, baseUri);
  }

  @Override
  public IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagInstance instance,
      @NonNull IModelNodeItem parent,
      @Nullable Object value) {
    return new FlagInstanceNodeItemImpl(instance, parent, value);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldDefinition definition,
      @Nullable Object value,
      @Nullable URI baseUri) {
    return new FieldDefinitionNodeItemImpl(definition, value, baseUri, this);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @Nullable Object value) {
    return new FieldInstanceNodeItemImpl(instance, parent, position, value, this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition,
      @Nullable Object value,
      @Nullable URI baseUri) {
    return new AssemblyDefinitionNodeItemImpl(definition, value, baseUri, this);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @Nullable Object value) {
    // return new AssemblyInstanceNodeItemImpl(instance, parent, position, value, this);

    IAssemblyNodeItem retval = null;
    if (value == null && !instance.getDefinition().isInline()) {
      // if not inline, need to check for a cycle
      IAssemblyNodeItem cycle = getCycledInstance(instance.getEffectiveName(), instance.getDefinition(), parent);
      if (cycle != null) {
        // generate a cycle wrapper of the original node item
        retval = new CycledAssemblyInstanceNodeItemImpl(instance, parent, cycle);
      }
    }

    if (retval == null) {
      retval = new AssemblyInstanceNodeItemImpl(instance, parent, position, value, this);
    }
    return retval;
  }

  @Nullable
  private IAssemblyNodeItem getCycledInstance(@NonNull String effectiveName, @NonNull IAssemblyDefinition definition,
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

  @Override
  public IDocumentNodeItem newDocumentNodeItem(@NonNull IRootAssemblyDefinition definition, @NonNull Object value,
      @NonNull URI documentUri) {
    return new DocumentNodeItemImpl(definition, value, documentUri, this);
  }

  @Override
  public IMetaschemaNodeItem newMetaschemaNodeItem(@NonNull IMetaschema metaschema) {
    return new MetaschemaNodeItemImpl(metaschema, this);
  }

  @Override
  public Map<String, IFlagNodeItem> generateFlags(@NonNull IModelNodeItem parent) {
    Map<String, IFlagNodeItem> retval = new LinkedHashMap<>(); // NOPMD - intentional

    Object parentValue = parent.getValue();
    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      assert instance != null;
      Object instanceValue = parentValue == null ? null : instance.getValue(parentValue);
      IFlagNodeItem item = newFlagNodeItem(instance, parent, instanceValue);
      retval.put(instance.getEffectiveName(), item);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @Override
  public Map<String, List<IModelNodeItem>> generateModelItems(
      @NonNull IAssemblyNodeItem parent) {
    Map<String, List<IModelNodeItem>> retval // NOPMD - intentional
        = new LinkedHashMap<>();

    Object parentValue = parent.getValue();
    for (INamedModelInstance instance : CollectionUtil.toIterable(getNamedModelInstances(parent.getDefinition()))) {
      assert instance != null;

      Object instanceValue = parentValue == null ? null : instance.getValue(parentValue);

      List<IModelNodeItem> items;
      if (instanceValue != null) {
        Stream<? extends Object> itemValues = instance.getItemValues(instanceValue).stream();
        AtomicInteger index = new AtomicInteger(); // NOPMD - intentional

        items = itemValues.map(itemValue -> {
          assert itemValue != null;
          return generateModelItem(instance, parent, index.incrementAndGet(), itemValue);
        }).collect(Collectors.toUnmodifiableList());
      } else {
        items = CollectionUtil.singletonList(generateModelItem(instance, parent, 1, null));
      }
      retval.put(instance.getEffectiveName(), items);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @NonNull
  private IModelNodeItem generateModelItem(
      @NonNull INamedModelInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int index,
      @Nullable Object value) {
    @NonNull IModelNodeItem item;
    if (instance instanceof IAssemblyInstance) {
      item = newAssemblyNodeItem((IAssemblyInstance) instance, parent, index, value);
    } else if (instance instanceof IFieldInstance) {
      item = newFieldNodeItem((IFieldInstance) instance, parent, index, value);
    } else {
      throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
    }
    return item;
  }

  @SuppressWarnings("null")
  @NonNull
  private Stream<INamedModelInstance> getNamedModelInstances(@NonNull IModelContainer container) {
    return container.getModelInstances().stream()
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
        });
  }

}
