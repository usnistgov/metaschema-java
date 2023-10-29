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

package gov.nist.secauto.metaschema.databind;

import gov.nist.secauto.metaschema.core.datatype.DataTypeService;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.xml.IModulePostProcessor;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.IGeneratedDefinitionClass;
import gov.nist.secauto.metaschema.databind.codegen.IProduction;
import gov.nist.secauto.metaschema.databind.codegen.ModuleCompilerHelper;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaBindingContext implements IBindingContext {
  @NonNull
  private static final MetaschemaBindingContext SINGLETON = new MetaschemaBindingContext();

  @NonNull
  private final IModuleLoaderStrategy moduleLoaderStrategy;
  @NonNull
  private final Map<Class<?>, IClassBindingStrategy<?>> boundClassToStrategyMap = new ConcurrentHashMap<>();
  @NonNull
  private final List<IBindingMatcher> bindingMatchers = new LinkedList<>();

  /**
   * Construct a new binding context.
   *
   * @param modulePostProcessors
   *          a list of module post processors to call after loading a module
   */
  public MetaschemaBindingContext(@NonNull List<IModulePostProcessor> modulePostProcessors) {
    // only allow extended classes
    moduleLoaderStrategy = new PostProcessingModuleLoaderStrategy(this, modulePostProcessors);
  }

  /**
   * Construct a new binding context.
   */
  public MetaschemaBindingContext() {
    // only allow extended classes
    moduleLoaderStrategy = new SimpleModuleLoaderStrategy(this);
  }

  @NonNull
  public static IBindingContext instance() {
    return SINGLETON;
  }

  @NonNull
  protected IModuleLoaderStrategy getModuleLoaderStrategy() {
    return moduleLoaderStrategy;
  }

  @NonNull
  protected List<IBindingMatcher> getBindingMatchers() {
    return bindingMatchers;
  }

  @Override
  public IBindingContext registerBindingMatcher(IBindingMatcher matcher) {
    synchronized (this) {
      bindingMatchers.add(matcher);
    }
    return this;
  }

  @Override
  public <T extends IDefinition> IClassBindingStrategy<?> registerClassBindingStrategy(
      IClassBindingStrategy<T> strategy) {
    Class<?> clazz = strategy.getBoundClass();
    return boundClassToStrategyMap.computeIfAbsent(clazz, k -> strategy);
  }

  @Override
  public IModule loadModule(Class<? extends IModule> clazz) {
    return getModuleLoaderStrategy().loadModule(clazz);
  }

  @Override
  public IBindingContext registerModule(@NonNull IModule module, @NonNull Path compilePath) throws IOException {
    Files.createDirectories(compilePath);

    ClassLoader classLoader = ModuleCompilerHelper.newClassLoader(
        compilePath,
        ObjectUtils.notNull(Thread.currentThread().getContextClassLoader()));

    IProduction production = ModuleCompilerHelper.compileMetaschema(module, compilePath);
    Stream<IGeneratedDefinitionClass> stream = production.getGlobalDefinitionClasses().stream()
        // get root definitions
        .filter(generatedClass -> generatedClass.isRootClass());
    List<IClassBindingStrategy<IAssemblyDefinition>> strategies = stream
        .map(generatedClass -> {
          try {
            Class<?> clazz = ObjectUtils.notNull(classLoader.loadClass(generatedClass.getClassName().reflectionName()));
            return getModuleLoaderStrategy().getClassBindingStrategy(clazz)
                .checkType(IAssemblyDefinition.class);
          } catch (ClassNotFoundException | BindingException ex) {
            throw new IllegalStateException(ex);
          }
        })
        .collect(Collectors.toList());

    strategies.forEach(strategy -> {
      assert strategy != null;
      registerClassBindingStrategy(strategy);
      registerBindingMatcher(IBindingMatcher.rootMatcher(strategy));
    });
    return this;
  }

  @Override
  public IClassBindingStrategy<?> getClassBindingStrategy(Class<?> clazz) throws IllegalArgumentException {
    // since the strategy is always class based, we know the cast will always be
    // to the correct class parameter
    return boundClassToStrategyMap.computeIfAbsent(clazz,
        key -> getModuleLoaderStrategy().getClassBindingStrategy(ObjectUtils.notNull(key)));
  }

  @Override
  public IClassBindingStrategy<IAssemblyDefinition> getBoundClassForXmlQName(QName rootQName) {
    return getBindingMatchers().stream()
        .flatMap(matcher -> {
          IClassBindingStrategy<IAssemblyDefinition> result = matcher.getBoundClassForXmlQName(rootQName);
          return result == null ? Stream.empty() : Stream.of(result);
        })
        .findFirst()
        .orElse(null);
  }

  @Override
  public IClassBindingStrategy<IAssemblyDefinition> getBoundClassForJsonName(String rootName) {
    return getBindingMatchers().stream()
        .flatMap(matcher -> {
          IClassBindingStrategy<IAssemblyDefinition> result = matcher.getBoundClassForJsonName(rootName);
          return result == null ? Stream.empty() : Stream.of(result);
        })
        .findFirst()
        .orElse(null);
  }

  @Override
  public <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NonNull Class<TYPE> clazz) {
    return DataTypeService.getInstance().getJavaTypeAdapterByClass(clazz);
  }

  @Override
  public <CLASS> ISerializer<CLASS> newSerializer(Format format, Class<CLASS> clazz) {
    IClassBindingStrategy<?> strategy = getClassBindingStrategy(clazz);
    if (strategy == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getClass().getName()));
    }
    IClassBindingStrategy<IAssemblyDefinition> rootAssemblyStrategy;
    try {
      rootAssemblyStrategy = strategy.checkType(IAssemblyDefinition.class);
    } catch (BindingException ex) {
      throw new IllegalStateException(ex);
    }

    ISerializer<CLASS> retval;
    switch (Objects.requireNonNull(format, "format")) {
    case JSON:
      retval = new DefaultJsonSerializer<>(rootAssemblyStrategy);
      break;
    case XML:
      retval = new DefaultXmlSerializer<>(rootAssemblyStrategy);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<>(rootAssemblyStrategy);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }
    return retval;
  }

  @Override
  public <CLASS> IDeserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz) {
    IClassBindingStrategy<?> strategy = getClassBindingStrategy(clazz);
    if (strategy == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getName()));
    }

    IClassBindingStrategy<IAssemblyDefinition> rootAssemblyStrategy;
    try {
      rootAssemblyStrategy = strategy.checkType(IAssemblyDefinition.class);
    } catch (BindingException ex) {
      throw new IllegalStateException(ex);
    }

    IDeserializer<CLASS> retval;
    switch (Objects.requireNonNull(format, "format")) {
    case JSON:
      retval = new DefaultJsonDeserializer<>(rootAssemblyStrategy);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<>(rootAssemblyStrategy);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<>(rootAssemblyStrategy);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }
    return retval;
  }

  @Override
  public IBoundLoader newBoundLoader() {
    return new DefaultBoundLoader(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T copyBoundObject(T source, Object parentInstance) throws BindingException {
    IClassBindingStrategy<?> strategy = getClassBindingStrategy(source.getClass());
    if (strategy == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", source.getClass().getName()));
    }
    return (T) strategy.deepCopy(source, parentInstance);
  }
}
