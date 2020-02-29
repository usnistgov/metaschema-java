/**
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

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.csrc.ns.metaschemaBinding.x10.JavaManagedObjectBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.JavaModelBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.ManagedObjectBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingType;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingsDocument;
import gov.nist.csrc.ns.metaschemaBinding.x10.MetaschemaBindingsType;
import gov.nist.csrc.ns.metaschemaBinding.x10.ModelBindingType;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DefaultBindingConfiguration implements BindingConfiguration {
  private Map<String, String> namespaceToPackageNameMap = new HashMap<>();
  private Map<String, Map<String, MutableManagedObjectBindingConfiguration>> metaschemaUrlToManagedObjectMap
      = new HashMap<>();

  public DefaultBindingConfiguration() {

  }

  @Override
  public String getPackageName(Metaschema metaschema) {
    URI namespace = metaschema.getXmlNamespace();

    String packageName = getPackageNameForNamespace(namespace.toASCIIString());
    if (packageName == null) {
      packageName = NameConverter.standard.toPackageName(namespace.toString());
    }
    return packageName;
  }

  @Override
  public String getClassName(ManagedObject managedObject) {
    String retval = null;

    ManagedObjectBindingConfiguration managedObjectBinding = getManagedObjectConfig(
        managedObject.getContainingMetaschema().getLocation().toString(), managedObject.getName());
    if (managedObjectBinding != null) {
      retval = managedObjectBinding.getClassName();
    }

    if (retval == null) {
      retval = NameConverter.standard.toClassName(managedObject.getName());
    }
    return retval;
  }

  public void addModelBindingConfig(String namespace, String packageName) {
    if (namespaceToPackageNameMap.containsKey(namespace)) {
      String oldPackageName = namespaceToPackageNameMap.get(namespace);
      if (!oldPackageName.equals(packageName)) {
        throw new IllegalStateException(
            String.format("Attempt to redefine existing package name '%s' to '%s' for namespace '%s'", oldPackageName,
                packageName, namespace));
      }
    } else {
      namespaceToPackageNameMap.put(namespace, packageName);
    }
  }

  protected String getPackageNameForNamespace(String namespace) {
    return namespaceToPackageNameMap.get(namespace);
  }

  public void addMManagedObjectBindingConfig(String metaschemaUri, String name,
      MutableManagedObjectBindingConfiguration managedObjectConfig) {

    Map<String, MutableManagedObjectBindingConfiguration> metaschemaConfigs
        = metaschemaUrlToManagedObjectMap.get(metaschemaUri);
    if (metaschemaConfigs == null) {
      metaschemaConfigs = new HashMap<>();
      metaschemaUrlToManagedObjectMap.put(metaschemaUri, metaschemaConfigs);
    }

    if (metaschemaConfigs.containsKey(name)) {
      throw new IllegalStateException(String.format(
          "Attempt to add an already existing binding configuration set for managed object '%s' in metaschema %s'.",
          name, metaschemaUri));
    }
    metaschemaConfigs.put(name, managedObjectConfig);
  }

  public MutableManagedObjectBindingConfiguration getManagedObjectConfig(String metaschemaUrl, String name) {
    Map<String, MutableManagedObjectBindingConfiguration> metaschemaConfigs
        = metaschemaUrlToManagedObjectMap.get(metaschemaUrl);

    MutableManagedObjectBindingConfiguration retval = null;
    if (metaschemaConfigs != null) {
      retval = metaschemaConfigs.get(name);
    }
    return retval;
  }

  @Override
  public JavaTypeSupplier getJavaTypeSupplier() {
    return new DeconflictingJavaTypeSupplier(this);
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

  private void processMetaschemaBindingConfig(URL configResource,
      MetaschemaBindingType metaschema) throws MalformedURLException, URISyntaxException {
    String href = metaschema.getHref();
    URL metaschemaUrl = new URL(configResource, href);
    String metaschemaUri = metaschemaUrl.toURI().toString();

    for (ManagedObjectBindingType managedObject : metaschema.getDefineAssemblyBindingList()) {
      processManagedObjectBindingConfig(metaschemaUri, managedObject);
    }

    for (ManagedObjectBindingType managedObject : metaschema.getDefineFieldBindingList()) {
      processManagedObjectBindingConfig(metaschemaUri, managedObject);
    }
  }

  private void processManagedObjectBindingConfig(String metaschemaUri,
      ManagedObjectBindingType managedObject) {
    String name = managedObject.getName();

    MutableManagedObjectBindingConfiguration managedObjectConfig = getManagedObjectConfig(metaschemaUri, name);

    boolean configCreated = false;
    if (managedObject.isSetJava()) {
      JavaManagedObjectBindingType java = managedObject.getJava();
      if (managedObjectConfig == null) {
        configCreated = true;
        managedObjectConfig = new DefaultMutableManagedObjectConfiguration();
      }

      boolean configChanged = false;
      if (java.isSetUseClassName()) {
        managedObjectConfig.setClassName(java.getUseClassName());
        configChanged = true;
      }

      if (java.isSetExtendBaseClass()) {
        managedObjectConfig.setQualifiedBaseClassName(java.getExtendBaseClass());
        configChanged = true;
      }

      for (String interfaceName : java.getImplementInterfaceList()) {
        managedObjectConfig.addInterfaceToImplement(interfaceName);
        configChanged = true;
      }

      if (configChanged && configCreated) {
        addMManagedObjectBindingConfig(metaschemaUri, name, managedObjectConfig);
      }
    }
  }
}
