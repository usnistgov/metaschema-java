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

package gov.nist.secauto.metaschema.databind.codegen;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IMetaschemaClassFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class ProductionImpl implements IProduction {

  @NonNull
  private final Map<IModule, IGeneratedModuleClass> moduleToProductionMap // NOPMD - immutable
      = new HashMap<>();
  @NonNull
  private final Map<String, IPackageProduction> packageNameToProductionMap // NOPMD - immutable
      = new HashMap<>();

  public void addModule(
      @NonNull IModule module,
      @NonNull IMetaschemaClassFactory classFactory,
      @NonNull Path targetDirectory) throws IOException {
    for (IModule importedModule : module.getImportedModules()) {
      assert importedModule != null;
      addModule(importedModule, classFactory, targetDirectory);
    }

    if (moduleToProductionMap.get(module) == null) {
      IGeneratedModuleClass metaschemaClass = classFactory.generateClass(module, targetDirectory);
      moduleToProductionMap.put(module, metaschemaClass);
    }
  }

  protected IPackageProduction addPackage(
      @NonNull PackageMetadata metadata,
      @NonNull IMetaschemaClassFactory classFactory,
      @NonNull Path targetDirectory)
      throws IOException {
    String javaPackage = metadata.getPackageName();

    IPackageProduction retval
        = new PackageProductionImpl(
            metadata,
            classFactory,
            targetDirectory);
    packageNameToProductionMap.put(javaPackage, retval);
    return retval;
  }

  @Override
  @SuppressWarnings("null")
  public Collection<IGeneratedModuleClass> getModuleProductions() {
    return Collections.unmodifiableCollection(moduleToProductionMap.values());
  }

  @SuppressWarnings("null")
  @NonNull
  protected Collection<IPackageProduction> getPackageProductions() {
    return Collections.unmodifiableCollection(packageNameToProductionMap.values());
  }

  @Override
  public IGeneratedModuleClass getModuleProduction(IModule module) {
    return moduleToProductionMap.get(module);
  }

  @Override
  public List<IGeneratedDefinitionClass> getGlobalDefinitionClasses() {
    return ObjectUtils.notNull(getModuleProductions().stream()
        .flatMap(metaschema -> metaschema.getGeneratedDefinitionClasses().stream())
        .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public Stream<? extends IGeneratedClass> getGeneratedClasses() {
    return ObjectUtils.notNull(Stream.concat(
        // generated definitions and Metaschema module
        getModuleProductions().stream()
            .flatMap(module -> Stream.concat(
                Stream.of(module),
                module.getGeneratedDefinitionClasses().stream())),
        // generated package-info.java
        getPackageProductions().stream()
            .flatMap(javaPackage -> Stream.of(javaPackage.getGeneratedClass()))));
  }

}
