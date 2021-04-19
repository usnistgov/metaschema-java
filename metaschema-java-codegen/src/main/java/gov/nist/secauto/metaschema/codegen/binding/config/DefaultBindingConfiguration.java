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

package gov.nist.secauto.metaschema.codegen.binding.config;

import gov.nist.csrc.ns.metaschemaBinding.x10.JavaModelBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.JavaObjectDefinitionBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingsDocument;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingsType;
import gov.nist.csrc.ns.metaschemaBinding.x10.ModelBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.ObjectDefinitionBindingType;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;

import org.apache.xmlbeans.XmlException;
import org.glassfish.jaxb.core.api.impl.NameConverter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultBindingConfiguration implements BindingConfiguration {
  private Map<String, String> namespaceToPackageNameMap = new HashMap<>();
  // metaschema location -> ModelType -> Definition Name -> BindingConfiguration
  private final Map<String, MetaschemaBindingConfiguration> metaschemaUrlToMetaschemaBindingConfigurationMap
      = new HashMap<>();

  /**
   * Create a new binding configuration.
   */
  public DefaultBindingConfiguration() {

  }

  @Override
  public String getPackageNameForMetaschema(Metaschema metaschema) {
    URI namespace = metaschema.getXmlNamespace();
    return getPackageNameForNamespace(namespace.toASCIIString());
  }

  @Override
  public String getClassName(ObjectDefinition definition) {
    DefinitionBindingConfiguration config = getDefinitionBindingConfiguration(definition);

    String retval = null;
    if (config != null) {
      retval = config.getClassName();
    }

    if (retval == null) {
      retval = NameConverter.standard.toClassName(definition.getName());
    }
    return retval;
  }

  /**
   * Binds an XML namespace, which is normally associated with one or more Metaschema, with a provided
   * Java package name.
   * 
   * @param namespace
   *          an XML namespace URI
   * @param packageName
   *          the package name to associate with the namespace
   * @throws IllegalStateException
   *           if the binding configuration is changing a previously changed namespace to package
   *           binding
   */
  public void addModelBindingConfig(String namespace, String packageName) {
    if (namespaceToPackageNameMap.containsKey(namespace)) {
      String oldPackageName = namespaceToPackageNameMap.get(namespace);
      if (!oldPackageName.equals(packageName)) {
        throw new IllegalStateException(
            String.format("Attempt to redefine existing package name '%s' to '%s' for namespace '%s'", oldPackageName,
                packageName, namespace));
      } // else the same package name, so do nothing
    } else {
      namespaceToPackageNameMap.put(namespace, packageName);
    }
  }

  /**
   * Based on the current binding configuration, generate a Java package name for the provided
   * namespace. If the namespace is already mapped, such as through the use of
   * {@link #addModelBindingConfig(String, String)}, then the provided package name will be used. If
   * the namespace is not mapped, then the namespace URI will be translated into a Java package name.
   * 
   * @param namespace
   *          the namespace to generate a Java package name for
   * @return a Java package name
   */
  protected String getPackageNameForNamespace(String namespace) {
    String packageName = namespaceToPackageNameMap.get(namespace);
    if (packageName == null) {
      packageName = NameConverter.standard.toPackageName(namespace.toString());
    }
    return packageName;
  }

  protected MetaschemaBindingConfiguration getMetaschemaBindingConfiguration(Metaschema metaschema) {
    String metaschemaUri = metaschema.getLocation().toString();
    return getMetaschemaBindingConfiguration(metaschemaUri);

  }

  protected MetaschemaBindingConfiguration getMetaschemaBindingConfiguration(String metaschemaUri) {
    return metaschemaUrlToMetaschemaBindingConfigurationMap.get(metaschemaUri);
  }

  public MetaschemaBindingConfiguration addMetaschemaBindingConfiguration(String metaschemaUri,
      MetaschemaBindingConfiguration config) {
    Objects.requireNonNull(metaschemaUri, "metaschemaUri");
    Objects.requireNonNull(config, "config");
    return metaschemaUrlToMetaschemaBindingConfigurationMap.put(metaschemaUri, config);
  }

  // public void addMManagedObjectBindingConfig(String metaschemaUri, String name,
  // MutableDefinitionBindingConfiguration managedObjectConfig) {
  //
  // Map<String, MutableDefinitionBindingConfiguration> metaschemaConfigs
  // = metaschemaUrlToObjectDefinitionMap.get(metaschemaUri);
  // if (metaschemaConfigs == null) {
  // metaschemaConfigs = new HashMap<>();
  // metaschemaUrlToObjectDefinitionMap.put(metaschemaUri, metaschemaConfigs);
  // }
  //
  // if (metaschemaConfigs.containsKey(name)) {
  // throw new IllegalStateException(String.format(
  // "Attempt to add an already existing binding configuration set for managed object '%s' in
  // metaschema %s'.",
  // name, metaschemaUri));
  // }
  // metaschemaConfigs.put(name, managedObjectConfig);
  // }

  protected DefinitionBindingConfiguration getDefinitionBindingConfiguration(ObjectDefinition definition) {
    String metaschemaUri = definition.getContainingMetaschema().getLocation().toString();
    String definitionName = definition.getName();

    MetaschemaBindingConfiguration metaschemaConfig = getMetaschemaBindingConfiguration(metaschemaUri);

    DefinitionBindingConfiguration retval = null;
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

  public void load(File file) throws MalformedURLException, IOException, MetaschemaException {
    URL resource = file.toURI().toURL();
    load(resource);
  }

  public void load(URL resource) throws IOException, MetaschemaException {
    MetaschemaBindingsDocument xml;
    try {
      xml = MetaschemaBindingsDocument.Factory.parse(resource);
    } catch (XmlException ex) {
      throw new MetaschemaException(ex);
    }

    MetaschemaBindingsType bindings = xml.getMetaschemaBindings();

    for (ModelBindingType model : bindings.getModelBindingList()) {
      processModelBindingConfig(model);
    }

    for (MetaschemaBindingType metaschema : bindings.getMetaschemaBindingList()) {
      try {
        processMetaschemaBindingConfig(resource, metaschema);
      } catch (MalformedURLException | URISyntaxException ex) {
        throw new MetaschemaException(ex);
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
    URL metaschemaUrl = new URL(configResource, href);
    String metaschemaUri = metaschemaUrl.toURI().toString();

    MetaschemaBindingConfiguration metaschemaConfig = getMetaschemaBindingConfiguration(metaschemaUri);
    if (metaschemaConfig == null) {
      metaschemaConfig = new MetaschemaBindingConfiguration();
      addMetaschemaBindingConfiguration(metaschemaUri, metaschemaConfig);
    }
    for (ObjectDefinitionBindingType assemblyBinding : metaschema.getDefineAssemblyBindingList()) {
      String name = assemblyBinding.getName();
      DefinitionBindingConfiguration config = metaschemaConfig.getAssemblyDefinitionBindingConfig(name);
      config = processDefinitionBindingConfiguration(config, assemblyBinding);
      metaschemaConfig.addAssemblyDefinitionBindingConfig(name, config);
    }

    for (ObjectDefinitionBindingType fieldBinding : metaschema.getDefineFieldBindingList()) {
      String name = fieldBinding.getName();
      DefinitionBindingConfiguration config = metaschemaConfig.getFieldDefinitionBindingConfig(name);
      config = processDefinitionBindingConfiguration(config, fieldBinding);
    }
  }

  private MutableDefinitionBindingConfiguration
      processDefinitionBindingConfiguration(DefinitionBindingConfiguration oldConfig,
          ObjectDefinitionBindingType objectDefinitionBinding) {
    MutableDefinitionBindingConfiguration config;
    if (oldConfig != null) {
      config = new DefaultDefinitionBindingConfiguration(oldConfig);
    } else {
      config = new DefaultDefinitionBindingConfiguration();
    }

    if (objectDefinitionBinding.isSetJava()) {
      JavaObjectDefinitionBindingType java = objectDefinitionBinding.getJava();
      if (java.isSetUseClassName()) {
        config.setClassName(java.getUseClassName());
      }

      if (java.isSetExtendBaseClass()) {
        config.setQualifiedBaseClassName(java.getExtendBaseClass());
      }

      for (String interfaceName : java.getImplementInterfaceList()) {
        config.addInterfaceToImplement(interfaceName);
      }
    }
    return config;
  }

  public class MetaschemaBindingConfiguration {
    private final Map<String, DefinitionBindingConfiguration> assemblyBindingConfigs = new HashMap<>();
    private final Map<String, DefinitionBindingConfiguration> fieldBindingConfigs = new HashMap<>();

    private MetaschemaBindingConfiguration() {
    }

    public DefinitionBindingConfiguration getAssemblyDefinitionBindingConfig(String name) {
      return assemblyBindingConfigs.get(name);
    }

    public DefinitionBindingConfiguration getFieldDefinitionBindingConfig(String name) {
      return fieldBindingConfigs.get(name);
    }

    public DefinitionBindingConfiguration addAssemblyDefinitionBindingConfig(String name,
        DefinitionBindingConfiguration managedObjectConfig) {
      return assemblyBindingConfigs.put(name, managedObjectConfig);
    }

    public DefinitionBindingConfiguration addFieldDefinitionBindingConfig(String name,
        DefinitionBindingConfiguration managedObjectConfig) {
      return fieldBindingConfigs.put(name, managedObjectConfig);
    }
  }
}
