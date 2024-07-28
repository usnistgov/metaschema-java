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

package gov.nist.secauto.metaschema.databind.codegen.config;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.ClassUtils;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.JavaModelBindingType;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.JavaObjectDefinitionBindingType;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.MetaschemaBindingType;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.MetaschemaBindingsDocument;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.MetaschemaBindingsType;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.ModelBindingType;
import gov.nist.secauto.metaschema.databind.codegen.xmlbeans.ObjectDefinitionBindingType;

import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class DefaultBindingConfiguration implements IBindingConfiguration {
  private final Map<String, String> namespaceToPackageNameMap = new ConcurrentHashMap<>();
  // metaschema location -> ModelType -> Definition name -> IBindingConfiguration
  private final Map<String, MetaschemaBindingConfiguration> moduleUrlToMetaschemaBindingConfigurationMap
      = new ConcurrentHashMap<>();

  @Override
  public String getPackageNameForModule(IModule module) {
    URI namespace = module.getXmlNamespace();
    return getPackageNameForNamespace(ObjectUtils.notNull(namespace.toASCIIString()));
  }

  /**
   * Retrieve the binding configuration for the provided {@code definition}.
   *
   * @param definition
   *          the definition to get the config for
   * @return the binding configuration or {@code null} if there is not
   *         configuration
   */
  @Nullable
  public IDefinitionBindingConfiguration getBindingConfigurationForDefinition(
      @NonNull IModelDefinition definition) {
    String moduleUri = ObjectUtils.notNull(definition.getContainingModule().getLocation().toASCIIString());
    String definitionName = definition.getName();

    MetaschemaBindingConfiguration metaschemaConfig = getMetaschemaBindingConfiguration(moduleUri);

    IDefinitionBindingConfiguration retval = null;
    if (metaschemaConfig != null) {
      switch (definition.getModelType()) {
      case ASSEMBLY:
        retval = metaschemaConfig.getAssemblyDefinitionBindingConfig(definitionName);
        break;
      case FIELD:
        retval = metaschemaConfig.getFieldDefinitionBindingConfig(definitionName);
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported definition type '%s'", definition.getModelType()));
      }
    }
    return retval;
  }

  @Override
  public String getQualifiedBaseClassName(IModelDefinition definition) {
    IDefinitionBindingConfiguration config = getBindingConfigurationForDefinition(definition);
    return config == null
        ? null
        : config.getQualifiedBaseClassName();
  }

  @Override
  public String getClassName(IModelDefinition definition) {
    IDefinitionBindingConfiguration config = getBindingConfigurationForDefinition(definition);

    String retval = null;
    if (config != null) {
      retval = config.getClassName();
    }

    if (retval == null) {
      retval = ClassUtils.toClassName(definition.getName());
    }
    return retval;
  }

  @Override
  public @NonNull String getClassName(@NonNull IModule module) {
    // TODO: make this configurable
    return ClassUtils.toClassName(module.getShortName() + "Module");
  }

  @Override
  public List<String> getQualifiedSuperinterfaceClassNames(IModelDefinition definition) {
    IDefinitionBindingConfiguration config = getBindingConfigurationForDefinition(definition);
    return config == null
        ? CollectionUtil.emptyList()
        : config.getInterfacesToImplement();
  }

  /**
   * Binds an XML namespace, which is normally associated with one or more Module,
   * with a provided Java package name.
   *
   * @param namespace
   *          an XML namespace URI
   * @param packageName
   *          the package name to associate with the namespace
   * @throws IllegalStateException
   *           if the binding configuration is changing a previously changed
   *           namespace to package binding
   */
  public void addModelBindingConfig(String namespace, String packageName) {
    if (namespaceToPackageNameMap.containsKey(namespace)) {
      String oldPackageName = namespaceToPackageNameMap.get(namespace);
      if (!oldPackageName.equals(packageName)) {
        throw new IllegalStateException(
            String.format("Attempt to redefine existing package name '%s' to '%s' for namespace '%s'",
                oldPackageName,
                packageName,
                namespace));
      } // else the same package name, so do nothing
    } else {
      namespaceToPackageNameMap.put(namespace, packageName);
    }
  }

  /**
   * Based on the current binding configuration, generate a Java package name for
   * the provided namespace. If the namespace is already mapped, such as through
   * the use of {@link #addModelBindingConfig(String, String)}, then the provided
   * package name will be used. If the namespace is not mapped, then the namespace
   * URI will be translated into a Java package name.
   *
   * @param namespace
   *          the namespace to generate a Java package name for
   * @return a Java package name
   */
  @NonNull
  protected String getPackageNameForNamespace(@NonNull String namespace) {
    String packageName = namespaceToPackageNameMap.get(namespace);
    if (packageName == null) {
      packageName = ClassUtils.toPackageName(namespace);
    }
    return packageName;
  }

  /**
   * Get the binding configuration for the provided Module.
   *
   * @param module
   *          the Module module
   * @return the configuration for the Module or {@code null} if there is no
   *         configuration
   */
  protected MetaschemaBindingConfiguration getMetaschemaBindingConfiguration(@NonNull IModule module) {
    String moduleUri = ObjectUtils.notNull(module.getLocation().toString());
    return getMetaschemaBindingConfiguration(moduleUri);

  }

  /**
   * Get the binding configuration for the Module modulke located at the provided
   * {@code moduleUri}.
   *
   * @param moduleUri
   *          the location of the Module module
   * @return the configuration for the Module module or {@code null} if there is
   *         no configuration
   */
  @Nullable
  protected MetaschemaBindingConfiguration getMetaschemaBindingConfiguration(@NonNull String moduleUri) {
    return moduleUrlToMetaschemaBindingConfigurationMap.get(moduleUri);
  }

  /**
   * Set the binding configuration for the Module module located at the provided
   * {@code moduleUri}.
   *
   * @param moduleUri
   *          the location of the Module module
   * @param config
   *          the Module binding configuration
   * @return the old configuration for the Module module or {@code null} if there
   *         was no previous configuration
   */
  public MetaschemaBindingConfiguration addMetaschemaBindingConfiguration(
      @NonNull String moduleUri,
      @NonNull MetaschemaBindingConfiguration config) {
    Objects.requireNonNull(moduleUri, "moduleUri");
    Objects.requireNonNull(config, "config");
    return moduleUrlToMetaschemaBindingConfigurationMap.put(moduleUri, config);
  }

  /**
   * Load the binding configuration from the provided {@code file}.
   *
   * @param file
   *          the configuration resource
   * @throws IOException
   *           if an error occurred while reading the {@code file}
   */
  public void load(Path file) throws IOException {
    URL resource = file.toAbsolutePath().normalize().toUri().toURL();
    load(resource);
  }

  /**
   * Load the binding configuration from the provided {@code file}.
   *
   * @param file
   *          the configuration resource
   * @throws IOException
   *           if an error occurred while reading the {@code file}
   */
  public void load(File file) throws IOException {
    load(file.toPath());
  }

  /**
   * Load the binding configuration from the provided {@code resource}.
   *
   * @param resource
   *          the configuration resource
   * @throws IOException
   *           if an error occurred while reading the {@code resource}
   */
  public void load(URL resource) throws IOException {
    MetaschemaBindingsDocument xml;
    try {
      xml = MetaschemaBindingsDocument.Factory.parse(resource);
    } catch (XmlException ex) {
      throw new IOException(ex);
    }

    MetaschemaBindingsType bindings = xml.getMetaschemaBindings();

    for (ModelBindingType model : bindings.getModelBindingList()) {
      processModelBindingConfig(model);
    }

    for (MetaschemaBindingType metaschema : bindings.getMetaschemaBindingList()) {
      try {
        processMetaschemaBindingConfig(resource, metaschema);
      } catch (MalformedURLException | URISyntaxException ex) {
        throw new IOException(ex);
      }
    }
  }

  private void processModelBindingConfig(ModelBindingType model) {
    String namespace = model.getNamespace();

    if (model.isSetJava()) {
      JavaModelBindingType java = model.getJava();
      if (java.isSetUsePackageName()) {
        addModelBindingConfig(namespace, java.getUsePackageName());
      }
    }
  }

  private void processMetaschemaBindingConfig(URL configResource, MetaschemaBindingType metaschema)
      throws MalformedURLException, URISyntaxException {
    String href = metaschema.getHref();
    URL moduleUrl = new URL(configResource, href);
    String moduleUri = ObjectUtils.notNull(moduleUrl.toURI().normalize().toString());

    MetaschemaBindingConfiguration metaschemaConfig = getMetaschemaBindingConfiguration(moduleUri);
    if (metaschemaConfig == null) {
      metaschemaConfig = new MetaschemaBindingConfiguration();
      addMetaschemaBindingConfiguration(moduleUri, metaschemaConfig);
    }
    for (ObjectDefinitionBindingType assemblyBinding : metaschema.getDefineAssemblyBindingList()) {
      String name = ObjectUtils.requireNonNull(assemblyBinding.getName());
      IDefinitionBindingConfiguration config = metaschemaConfig.getAssemblyDefinitionBindingConfig(name);
      config = processDefinitionBindingConfiguration(config, assemblyBinding);
      metaschemaConfig.addAssemblyDefinitionBindingConfig(name, config);
    }

    for (ObjectDefinitionBindingType fieldBinding : metaschema.getDefineFieldBindingList()) {
      String name = ObjectUtils.requireNonNull(fieldBinding.getName());
      IDefinitionBindingConfiguration config = metaschemaConfig.getFieldDefinitionBindingConfig(name);
      config = processDefinitionBindingConfiguration(config, fieldBinding);
      metaschemaConfig.addFieldDefinitionBindingConfig(name, config);
    }
  }

  @NonNull
  private static IMutableDefinitionBindingConfiguration processDefinitionBindingConfiguration(
      @Nullable IDefinitionBindingConfiguration oldConfig,
      @NonNull ObjectDefinitionBindingType objectDefinitionBinding) {
    IMutableDefinitionBindingConfiguration config = oldConfig == null
        ? new DefaultDefinitionBindingConfiguration()
        : new DefaultDefinitionBindingConfiguration(oldConfig);

    if (objectDefinitionBinding.isSetJava()) {
      JavaObjectDefinitionBindingType java = objectDefinitionBinding.getJava();
      if (java.isSetUseClassName()) {
        config.setClassName(ObjectUtils.notNull(java.getUseClassName()));
      }

      if (java.isSetExtendBaseClass()) {
        config.setQualifiedBaseClassName(ObjectUtils.notNull(java.getExtendBaseClass()));
      }

      for (String interfaceName : java.getImplementInterfaceList()) {
        config.addInterfaceToImplement(ObjectUtils.notNull(interfaceName));
      }
    }
    return config;
  }

  public static final class MetaschemaBindingConfiguration {
    private final Map<String, IDefinitionBindingConfiguration> assemblyBindingConfigs = new ConcurrentHashMap<>();
    private final Map<String, IDefinitionBindingConfiguration> fieldBindingConfigs = new ConcurrentHashMap<>();

    private MetaschemaBindingConfiguration() {
    }

    /**
     * Get the binding configuration for the {@link IAssemblyDefinition} with the
     * provided {@code name}.
     *
     * @param name
     *          the definition name
     * @return the definition's binding configuration or {@code null} if no
     *         configuration is provided
     */
    @Nullable
    public IDefinitionBindingConfiguration getAssemblyDefinitionBindingConfig(@NonNull String name) {
      return assemblyBindingConfigs.get(name);
    }

    /**
     * Get the binding configuration for the {@link IFieldDefinition} with the
     * provided {@code name}.
     *
     * @param name
     *          the definition name
     * @return the definition's binding configuration or {@code null} if no
     *         configuration is provided
     */
    @Nullable
    public IDefinitionBindingConfiguration getFieldDefinitionBindingConfig(@NonNull String name) {
      return fieldBindingConfigs.get(name);
    }

    /**
     * Set the binding configuration for the {@link IAssemblyDefinition} with the
     * provided {@code name}.
     *
     * @param name
     *          the definition name
     * @param config
     *          the new binding configuration for the definition
     * @return the definition's old binding configuration or {@code null} if no
     *         configuration was previously provided
     */
    @Nullable
    public IDefinitionBindingConfiguration addAssemblyDefinitionBindingConfig(@NonNull String name,
        @NonNull IDefinitionBindingConfiguration config) {
      return assemblyBindingConfigs.put(name, config);
    }

    /**
     * Set the binding configuration for the {@link IFieldDefinition} with the
     * provided {@code name}.
     *
     * @param name
     *          the definition name
     * @param config
     *          the new binding configuration for the definition
     * @return the definition's old binding configuration or {@code null} if no
     *         configuration was previously provided
     */
    @Nullable
    public IDefinitionBindingConfiguration addFieldDefinitionBindingConfig(@NonNull String name,
        @NonNull IDefinitionBindingConfiguration config) {
      return fieldBindingConfigs.put(name, config);
    }
  }
}
