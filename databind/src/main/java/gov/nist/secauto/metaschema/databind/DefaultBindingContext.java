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
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IModuleLoader;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.IProduction;
import gov.nist.secauto.metaschema.databind.codegen.ModuleCompilerHelper;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The implementation of a {@link IBindingContext} provided by this library.
 * <p>
 * This implementation caches Module information, which can dramatically improve
 * read and write performance at the cost of some memory use. Thus, using the
 * same singleton of this class across multiple I/O operations will improve
 * overall read and write performance when processing the same types of data.
 * <p>
 * Serializers and deserializers provided by this class using the
 * {@link #newSerializer(Format, Class)} and
 * {@link #newDeserializer(Format, Class)} methods will
 * <p>
 * This class is synchronized and is thread-safe.
 */
public class DefaultBindingContext implements IBindingContext {
  private static DefaultBindingContext singleton;
  @NonNull
  private final IModuleLoaderStrategy moduleLoaderStrategy;
  @NonNull
  private final Map<Class<?>, IBoundDefinitionModelComplex> boundClassToStrategyMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<IBoundDefinitionModelAssembly, IBindingMatcher> bindingMatchers = new ConcurrentHashMap<>();

  /**
   * Get the singleton instance of this binding context.
   *
   * @return the binding context
   */
  @NonNull
  public static DefaultBindingContext instance() {
    synchronized (DefaultBindingContext.class) {
      if (singleton == null) {
        singleton = new DefaultBindingContext();
      }
    }
    return ObjectUtils.notNull(singleton);
  }

  /**
   * Construct a new binding context.
   *
   * @param modulePostProcessors
   *          a list of module post processors to call after loading a module
   */
  public DefaultBindingContext(@NonNull List<IModuleLoader.IModulePostProcessor> modulePostProcessors) {
    // only allow extended classes
    moduleLoaderStrategy = new PostProcessingModuleLoaderStrategy(this, modulePostProcessors);
  }

  /**
   * Construct a new binding context.
   */
  public DefaultBindingContext() {
    // only allow extended classes
    moduleLoaderStrategy = new SimpleModuleLoaderStrategy(this);
  }

  @NonNull
  protected IModuleLoaderStrategy getModuleLoaderStrategy() {
    return moduleLoaderStrategy;
  }

  /**
   * Get the binding matchers that are associated with this class.
   *
   * @return the list of matchers
   * @see #registerBindingMatcher(Class)
   * @see #registerBindingMatcher(IBoundDefinitionModelAssembly)
   */
  @NonNull
  protected Collection<IBindingMatcher> getBindingMatchers() {
    return ObjectUtils.notNull(bindingMatchers.values());
  }

  @Override
  @NonNull
  public final IBindingMatcher registerBindingMatcher(@NonNull IBoundDefinitionModelAssembly definition) {
    return ObjectUtils.notNull(bindingMatchers.computeIfAbsent(definition, key -> IBindingMatcher.of(definition)));
  }

  @Override
  public final IBindingMatcher registerBindingMatcher(@NonNull Class<? extends IBoundObject> clazz) {
    IBoundDefinitionModelComplex definition = getBoundDefinitionForClass(clazz);
    if (definition == null) {
      throw new IllegalArgumentException(String.format("Unable to find bound definition for class '%s'.",
          clazz.getName()));
    }

    try {
      IBoundDefinitionModelAssembly assemblyDefinition = IBoundDefinitionModelAssembly.class.cast(definition);
      return registerBindingMatcher(ObjectUtils.notNull(assemblyDefinition));
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException(
          String.format("The provided class '%s' is not a root assembly.", clazz.getName()), ex);
    }
  }

  @Override
  public final IBoundDefinitionModelComplex registerClassBinding(IBoundDefinitionModelComplex definition) {
    Class<?> clazz = definition.getBoundClass();
    return boundClassToStrategyMap.computeIfAbsent(clazz, k -> definition);
  }

  @Override
  public IBoundDefinitionModelComplex getBoundDefinitionForClass(@NonNull Class<? extends IBoundObject> clazz) {
    return moduleLoaderStrategy.getBoundDefinitionForClass(clazz);
  }

  @Override
  public <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NonNull Class<TYPE> clazz) {
    return DataTypeService.getInstance().getJavaTypeAdapterByClass(clazz);
  }

  /**
   * {@inheritDoc}
   * <p>
   * A serializer returned by this method is thread-safe.
   */
  @Override
  public <CLASS extends IBoundObject> ISerializer<CLASS> newSerializer(
      @NonNull Format format,
      @NonNull Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    IBoundDefinitionModelAssembly definition;
    try {
      definition = IBoundDefinitionModelAssembly.class.cast(getBoundDefinitionForClass(clazz));
    } catch (ClassCastException ex) {
      throw new IllegalStateException(
          String.format("Class '%s' is not a bound assembly.", clazz.getClass().getName()), ex);
    }
    if (definition == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getClass().getName()));
    }
    ISerializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonSerializer<>(definition);
      break;
    case XML:
      retval = new DefaultXmlSerializer<>(definition);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<>(definition);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   * <p>
   * A deserializer returned by this method is thread-safe.
   */
  @Override
  public <CLASS extends IBoundObject> IDeserializer<CLASS> newDeserializer(
      @NonNull Format format,
      @NonNull Class<CLASS> clazz) {
    IBoundDefinitionModelAssembly definition;
    try {
      definition = IBoundDefinitionModelAssembly.class.cast(getBoundDefinitionForClass(clazz));
    } catch (ClassCastException ex) {
      throw new IllegalStateException(
          String.format("Class '%s' is not a bound assembly.", clazz.getClass().getName()),
          ex);
    }
    if (definition == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getName()));
    }
    IDeserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonDeserializer<>(definition);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<>(definition);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<>(definition);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  @SuppressWarnings({ "PMD.UseProperClassLoader", "unchecked" }) // false positive
  @NonNull
  public IBindingContext registerModule(
      @NonNull IModule module,
      @NonNull Path compilePath) throws IOException {
    if (!(module instanceof IBoundModule)) {
      Files.createDirectories(compilePath);

      ClassLoader classLoader = ModuleCompilerHelper.newClassLoader(
          compilePath,
          ObjectUtils.notNull(Thread.currentThread().getContextClassLoader()));

      IProduction production = ModuleCompilerHelper.compileMetaschema(module, compilePath);
      production.getModuleProductions().stream()
          .map(item -> {
            try {
              return (Class<? extends IBoundModule>) classLoader.loadClass(item.getClassName().reflectionName());
            } catch (ClassNotFoundException ex) {
              throw new IllegalStateException(ex);
            }
          })
          .forEachOrdered(clazz -> {
            IBoundModule boundModule = registerModule(ObjectUtils.notNull(clazz));
            // force the binding matchers to load
            boundModule.getRootAssemblyDefinitions();
          });
    }
    return this;
  }

  @Override
  public IBoundModule registerModule(Class<? extends IBoundModule> clazz) {
    return getModuleLoaderStrategy().loadModule(clazz);
    // retval.getExportedAssemblyDefinitions().stream()
    // .map(def -> (IBoundDefinitionModelAssembly) def)
    // .filter(def -> def.isRoot())
    // .forEachOrdered(def -> registerBindingMatcher(ObjectUtils.notNull(def)));
    // return retval;
  }

  @Override
  public Class<? extends IBoundObject> getBoundClassForRootXmlQName(@NonNull QName rootQName) {
    Class<? extends IBoundObject> retval = null;
    for (IBindingMatcher matcher : getBindingMatchers()) {
      retval = matcher.getBoundClassForXmlQName(rootQName);
      if (retval != null) {
        break;
      }
    }
    return retval;
  }

  @Override
  public Class<? extends IBoundObject> getBoundClassForRootJsonName(@NonNull String rootName) {
    Class<? extends IBoundObject> retval = null;
    for (IBindingMatcher matcher : getBindingMatchers()) {
      retval = matcher.getBoundClassForJsonName(rootName);
      if (retval != null) {
        break;
      }
    }
    return retval;
  }

  @Override
  public DefaultBoundLoader newBoundLoader() {
    return new DefaultBoundLoader(this);
  }

  @Override
  public <CLASS extends IBoundObject> CLASS deepCopy(@NonNull CLASS other, IBoundObject parentInstance)
      throws BindingException {
    IBoundDefinitionModelComplex definition = getBoundDefinitionForClass(other.getClass());
    if (definition == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", other.getClass().getName()));
    }
    return ObjectUtils.asType(definition.deepCopyItem(other, parentInstance));
  }
}
