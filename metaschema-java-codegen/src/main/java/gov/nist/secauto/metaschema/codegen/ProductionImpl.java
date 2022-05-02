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

package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ProductionImpl implements IProduction {

  @NotNull
  private final Map<@NotNull IMetaschema, MetaschemaProductionImpl> metaschemaToProductionMap // NOPMD - immutable
      = new HashMap<>();
  @NotNull
  private final Map<@NotNull String, IPackageProduction> packageNameToProductionMap // NOPMD - immutable
      = new HashMap<>();

  public void processMetaschema(@NotNull IMetaschema metaschema,
      @NotNull ITypeResolver typeResolver, @NotNull Path targetDirectory) throws IOException {
    for (IMetaschema importedMetaschema : metaschema.getImportedMetaschemas()) {
      processMetaschema(importedMetaschema, typeResolver, targetDirectory);
    }

    if (getMetaschemaProduction(metaschema) == null) {
      addMetaschema(metaschema, typeResolver, targetDirectory);
    }
  }

  public IMetaschemaProduction addMetaschema(@NotNull IMetaschema metaschema, @NotNull ITypeResolver typeResolver,
      @NotNull Path targetDirectory) throws IOException {
    IMetaschemaProduction retval = metaschemaToProductionMap.get(metaschema);
    if (retval == null) {
      metaschemaToProductionMap.put(metaschema,
          new MetaschemaProductionImpl(metaschema, typeResolver, targetDirectory));
    }

    return retval;
  }

  public IPackageProduction addPackage(@NotNull String javaPackage, @NotNull URI xmlNamespace,
      @NotNull List<@NotNull IMetaschemaProduction> metaschemaProductions, @NotNull Path targetDirectory)
      throws IOException {
    IPackageProduction retval
        = new PackageProductionImpl(javaPackage, xmlNamespace, metaschemaProductions, targetDirectory);
    packageNameToProductionMap.put(javaPackage, retval);
    return retval;
  }

  @Override
  @SuppressWarnings("null")
  public Collection<@NotNull IMetaschemaProduction> getMetaschemaProductions() {
    return Collections.unmodifiableCollection(metaschemaToProductionMap.values());
  }

  @SuppressWarnings("null")
  @NotNull
  protected Collection<@NotNull IPackageProduction> getPackageProductions() {
    return Collections.unmodifiableCollection(packageNameToProductionMap.values());
  }

  @Override
  public IMetaschemaProduction getMetaschemaProduction(IMetaschema metaschema) {
    return metaschemaToProductionMap.get(metaschema);
  }

  @Override
  public Stream<@NotNull IDefinitionProduction> getDefinitionProductionsAsStream() {
    return ObjectUtils.notNull(getMetaschemaProductions().stream()
        .flatMap(metaschema -> metaschema.getDefinitionProductions().stream()));
  }

  @Override
  public Stream<@NotNull IGeneratedClass> getGeneratedClasses() {
    return ObjectUtils.notNull(Stream.concat(
        getMetaschemaProductions().stream()
            .flatMap(metaschema -> metaschema.getGeneratedClasses()),
        getPackageProductions().stream()
            .flatMap(javaPackage -> Stream.of(javaPackage.getGeneratedClass()))));
  }

}
