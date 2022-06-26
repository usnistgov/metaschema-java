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
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ModelFactoryImpl {
  private static final ModelFactoryImpl SINGLETON = new ModelFactoryImpl();

  private final INodeItemFactory nodeItemFactory;

  public static ModelFactoryImpl instance() {
    return SINGLETON;
  }

  protected ModelFactoryImpl() {
    this(new INodeItemFactory() {
    });
  }

  protected ModelFactoryImpl(@NotNull INodeItemFactory factory) {
    nodeItemFactory = factory;
  }

  protected INodeItemFactory getNodeItemFactory() {
    return nodeItemFactory;
  }

  @NotNull
  public IFlagNodeItem newFlagNodeItem(@NotNull IFlagDefinition definition, @Nullable URI baseUri) {
    return getNodeItemFactory().newFlagNodeItem(definition, baseUri);
  }

  @NotNull
  public IFlagNodeItem newFlagNodeItem(@NotNull IFlagInstance instance, @NotNull IModelNodeItem parent) {
    return getNodeItemFactory().newFlagNodeItem(instance, parent);
  }

  @NotNull
  public IRequiredValueFlagNodeItem newFlagNodeItem(
      @NotNull IFlagInstance instance,
      @NotNull IRequiredValueModelNodeItem parent,
      @NotNull Object instanceValue) {
    return getNodeItemFactory().newFlagNodeItem(instance, parent, instanceValue);
  }

  @NotNull
  public IFieldNodeItem newFieldNodeItem(@NotNull IFieldDefinition definition, @Nullable URI baseUri) {
    return getNodeItemFactory().newFieldNodeItem(definition, baseUri);
  }

  @NotNull
  public IFieldNodeItem newFieldNodeItem(@NotNull IFieldInstance instance, @NotNull IAssemblyNodeItem parent) {
    return getNodeItemFactory().newFieldNodeItem(instance, parent);
  }

  @NotNull
  public IRequiredValueFieldNodeItem newFieldNodeItem(
      @NotNull IFieldInstance instance,
      @NotNull IRequiredValueAssemblyNodeItem parent,
      int position,
      @NotNull Object instanceValue) {
    return getNodeItemFactory().newFieldNodeItem(instance, parent, position, instanceValue);
  }

  @NotNull
  public IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyDefinition definition, @Nullable URI baseUri) {
    return getNodeItemFactory().newAssemblyNodeItem(definition, baseUri);
  }

  @NotNull
  public IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyInstance instance, @NotNull IAssemblyNodeItem parent) {
    return getNodeItemFactory().newAssemblyNodeItem(instance, parent);
  }

  @NotNull
  public IRequiredValueAssemblyNodeItem newAssemblyNodeItem(
      @NotNull IAssemblyInstance instance,
      @NotNull IRequiredValueAssemblyNodeItem parent,
      int position,
      @NotNull Object instanceValue) {
    return getNodeItemFactory().newAssemblyNodeItem(instance, parent, position, instanceValue);
  }

  @NotNull
  public Map<@NotNull String, IFlagNodeItem> generateFlags(@NotNull IModelNodeItem parent) {
    Map<@NotNull String, IFlagNodeItem> retval = new LinkedHashMap<>();

    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      IFlagNodeItem item = newFlagNodeItem(instance, parent);
      retval.put(instance.getEffectiveName(), item);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @NotNull
  public Map<@NotNull String, IRequiredValueFlagNodeItem> generateFlagsWithValues(
      @NotNull IRequiredValueModelNodeItem parent) {
    Map<@NotNull String, IRequiredValueFlagNodeItem> retval = new LinkedHashMap<>();

    Object parentValue = parent.getValue();
    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      Object instanceValue = instance.getValue(parentValue);
      if (instanceValue != null) {
        IRequiredValueFlagNodeItem item = newFlagNodeItem(instance, parent, instanceValue);
        retval.put(instance.getEffectiveName(), item);
      }
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  public Map<@NotNull String, List<@NotNull IModelNodeItem>> generateModelItems(@NotNull IAssemblyNodeItem parent) {
    Map<@NotNull String, List<@NotNull IModelNodeItem>> retval = new LinkedHashMap<>();

    for (INamedModelInstance instance : parent.getDefinition().getNamedModelInstances()) {
      @NotNull
      IModelNodeItem item;
      if (instance instanceof IAssemblyInstance) {
        item = newAssemblyNodeItem((IAssemblyInstance) instance, parent);
      } else if (instance instanceof IFieldInstance) {
        item = newFieldNodeItem((IFieldInstance) instance, parent);
      } else {
        throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
      }
      retval.put(instance.getEffectiveName(), Collections.singletonList(item));
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  public Map<@NotNull String, List<@NotNull IRequiredValueModelNodeItem>> generateModelItemsWithValues(
      @NotNull IRequiredValueAssemblyNodeItem parent) {
    Map<@NotNull String, List<@NotNull IRequiredValueModelNodeItem>> retval = new LinkedHashMap<>();

    Object parentValue = parent.getValue();
    for (INamedModelInstance instance : parent.getDefinition().getNamedModelInstances()) {
      Object instanceValue = instance.getValue(parentValue);

      Stream<@NotNull ? extends Object> itemValues = instance.getItemValues(instanceValue).stream();
      AtomicInteger index = new AtomicInteger();
      List<@NotNull IRequiredValueModelNodeItem> items = itemValues.map(itemValue -> {
        @NotNull
        IRequiredValueModelNodeItem item;
        if (instance instanceof IAssemblyInstance) {
          item = newAssemblyNodeItem((IAssemblyInstance) instance, parent, index.incrementAndGet(),
              itemValue);
        } else if (instance instanceof IFieldInstance) {
          item = newFieldNodeItem((IFieldInstance) instance, parent, index.incrementAndGet(),
              itemValue);
        } else {
          throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
        }
        return item;
      }).collect(Collectors.toUnmodifiableList());
      retval.put(instance.getEffectiveName(), items);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }
}
