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

package gov.nist.secauto.metaschema.core.metapath.item.node;

import gov.nist.secauto.metaschema.core.metapath.item.node.IFeatureFlagContainerItem.FlagContainer;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFeatureModelContainerItem.ModelContainer;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.CouplingBetweenObjects")
final class DefaultNodeItemFactory
    extends AbstractNodeItemFactory {
  @NonNull
  static final DefaultNodeItemFactory SINGLETON = new DefaultNodeItemFactory();

  /**
   * Get the singleton instance of this node factory.
   *
   * @return the node factory instance
   */
  @NonNull
  public static DefaultNodeItemFactory instance() {
    return SINGLETON;
  }

  private DefaultNodeItemFactory() {
    // prevent construction
  }

  @Override
  @NonNull
  public Supplier<FlagContainer> newDataModelSupplier(@NonNull IFieldNodeItem item) {
    return () -> {
      Map<QName, IFlagNodeItem> flags = generateFlags(item);
      return new FlagContainer(flags);
    };
  }

  @Override
  @NonNull
  public Supplier<ModelContainer> newDataModelSupplier(@NonNull IAssemblyNodeItem item) {
    return () -> {
      Map<QName, IFlagNodeItem> flags = generateFlags(item);
      Map<QName, List<? extends IModelNodeItem<?, ?>>> modelItems = generateModelItems(item);
      return new ModelContainer(flags, modelItems);
    };
  }

  @Override
  public Supplier<ModelContainer> newDataModelSupplier(IRootAssemblyNodeItem item) {
    return () -> {
      Map<QName, List<? extends IModelNodeItem<?, ?>>> modelItems = CollectionUtil.singletonMap(
          item.getName(),
          CollectionUtil.singletonList(item));
      return new ModelContainer(CollectionUtil.emptyMap(), modelItems);
    };
  }

  /**
   * Given the provided parent node item, generate a mapping of flag name to flag
   * node item for each flag on the parent assembly.
   *
   * @param parent
   *          the parent assembly containing flags
   * @return a mapping of flag name to flag item
   */
  @SuppressWarnings("PMD.UseConcurrentHashMap") // need an ordered Map
  @NonNull
  protected Map<QName, IFlagNodeItem> generateFlags(@NonNull IModelNodeItem<?, ?> parent) {
    Map<QName, IFlagNodeItem> retval = new LinkedHashMap<>();

    Object parentValue = parent.getValue();
    assert parentValue != null;
    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      Object flagValue = instance.getValue(parentValue);
      if (flagValue != null) {
        IFlagNodeItem item = newFlagNodeItem(instance, parent, flagValue);
        retval.put(instance.getXmlQName(), item);
      }
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  /**
   * Given the provided parent node item, generate a mapping of model instance
   * name to model node item(s) for each model instance on the parent assembly.
   *
   * @param parent
   *          the parent assembly containing model instances
   * @return a mapping of model instance name to model node item(s)
   */
  @SuppressWarnings({ "PMD.UseConcurrentHashMap", "PMD.CognitiveComplexity" }) // need an ordered map
  @NonNull
  protected Map<QName, List<? extends IModelNodeItem<?, ?>>> generateModelItems(@NonNull IAssemblyNodeItem parent) {
    Map<QName, List<? extends IModelNodeItem<?, ?>>> retval = new LinkedHashMap<>();

    Object parentValue = parent.getValue();
    assert parentValue != null;
    for (IModelInstance instance : CollectionUtil.toIterable(getValuedModelInstances(parent.getDefinition()))) {
      if (instance instanceof INamedModelInstanceAbsolute) {
        INamedModelInstanceAbsolute namedInstance = (INamedModelInstanceAbsolute) instance;

        Object instanceValue = namedInstance.getValue(parentValue);
        if (instanceValue != null) {
          List<IModelNodeItem<?, ?>> items = generateModelInstanceItems(
              parent,
              namedInstance,
              ObjectUtils.notNull(namedInstance.getItemValues(instanceValue).stream()));
          retval.put(namedInstance.getXmlQName(), items);
        }
      } else if (instance instanceof IChoiceGroupInstance) {
        IChoiceGroupInstance choiceInstance = (IChoiceGroupInstance) instance;

        Object instanceValue = choiceInstance.getValue(parentValue);
        if (instanceValue != null) {
          Map<INamedModelInstanceGrouped, List<Object>> instanceMap
              = choiceInstance.getItemValues(instanceValue).stream()
                  .map(item -> {
                    assert item != null;
                    INamedModelInstanceGrouped itemInstance = choiceInstance.getItemInstance(item);
                    return Map.entry(itemInstance, item);
                  })
                  .collect(Collectors.groupingBy(
                      entry -> entry.getKey(),
                      LinkedHashMap::new,
                      Collectors.mapping(entry -> entry.getValue(), Collectors.toUnmodifiableList())));

          for (Map.Entry<INamedModelInstanceGrouped, List<Object>> entry : instanceMap.entrySet()) {
            INamedModelInstanceGrouped namedInstance = entry.getKey();
            assert namedInstance != null;

            List<IModelNodeItem<?, ?>> items = generateModelInstanceItems(
                parent,
                namedInstance,
                ObjectUtils.notNull(entry.getValue().stream()));
            retval.put(namedInstance.getXmlQName(), items);
          }
        }
      }

    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  private List<IModelNodeItem<?, ?>> generateModelInstanceItems(
      @NonNull IAssemblyNodeItem parent,
      @NonNull INamedModelInstance namedInstance,
      @NonNull Stream<?> itemValues) {
    AtomicInteger index = new AtomicInteger(); // NOPMD - intentional

    // the item values will be all non-null items
    return itemValues.map(itemValue -> {
      assert itemValue != null;
      return newModelItem(namedInstance, parent, index.incrementAndGet(), itemValue);
    }).collect(Collectors.toUnmodifiableList());
  }

  @Override
  public Supplier<ModelContainer> newMetaschemaModelSupplier(@NonNull IModuleNodeItem item) {
    return () -> {
      IModule module = item.getModule();

      // build flags from Metaschema definitions
      Map<QName, IFlagNodeItem> flags = ObjectUtils.notNull(
          Collections.unmodifiableMap(module.getExportedFlagDefinitions().stream()
              .map(def -> newFlagNodeItem(ObjectUtils.notNull(def), item))
              .collect(
                  Collectors.toMap(
                      IFlagNodeItem::getName,
                      Function.identity(),
                      (v1, v2) -> v2,
                      LinkedHashMap::new))));

      // build model items from Metaschema definitions
      Stream<IFieldNodeItem> fieldStream = module.getExportedFieldDefinitions().stream()
          .map(def -> newFieldNodeItem(ObjectUtils.notNull(def), item));
      Stream<IAssemblyNodeItem> assemblyStream = module.getExportedAssemblyDefinitions().stream()
          .map(def -> newAssemblyNodeItem(ObjectUtils.notNull(def), item));

      Map<QName, List<? extends IModelNodeItem<?, ?>>> modelItems
          = ObjectUtils.notNull(Stream.concat(fieldStream, assemblyStream)
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.groupingBy(IModelNodeItem::getName),
                      Collections::unmodifiableMap)));
      return new ModelContainer(flags, modelItems);
    };
  }

  @Override
  public Supplier<FlagContainer> newMetaschemaModelSupplier(@NonNull IFieldNodeItem item) {
    return () -> {
      Map<QName, IFlagNodeItem> flags = generateMetaschemaFlags(item);
      return new FlagContainer(flags);
    };
  }

  @Override
  public Supplier<ModelContainer> newMetaschemaModelSupplier(
      @NonNull IAssemblyNodeItem item) {
    return () -> {
      Map<QName, IFlagNodeItem> flags = generateMetaschemaFlags(item);
      Map<QName, List<? extends IModelNodeItem<?, ?>>> modelItems = generateMetaschemaModelItems(item);
      return new ModelContainer(flags, modelItems);
    };
  }

  @NonNull
  protected Map<QName, IFlagNodeItem> generateMetaschemaFlags(
      @NonNull IModelNodeItem<?, ?> parent) {
    Map<QName, IFlagNodeItem> retval = new LinkedHashMap<>(); // NOPMD - intentional

    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      assert instance != null;
      IFlagNodeItem item = newFlagNodeItem(instance, parent);
      retval.put(instance.getXmlQName(), item);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @NonNull
  protected Map<QName, List<? extends IModelNodeItem<?, ?>>> generateMetaschemaModelItems(
      @NonNull IAssemblyNodeItem parent) {
    Map<QName, List<? extends IModelNodeItem<?, ?>>> retval = new LinkedHashMap<>(); // NOPMD - intentional

    for (INamedModelInstance instance : CollectionUtil.toIterable(getNamedModelInstances(parent.getDefinition()))) {
      assert instance != null;
      IModelNodeItem<?, ?> item = newModelItem(instance, parent);
      retval.put(instance.getXmlQName(), Collections.singletonList(item));
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(IAssemblyInstanceGrouped instance, IAssemblyNodeItem parent,
      int position, Object value) {
    throw new UnsupportedOperationException("implement");
  }
}
