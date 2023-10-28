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
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeResolver;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IModelDefinitionTypeInfo;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IMetaschemaClassFactory {
  /**
   * Get a new instance of the default class generation factory that uses the
   * provided {@code typeResolver}.
   *
   * @param typeResolver
   *          the resolver used to generate type information for Metasschema
   *          constructs
   * @return the new class factory
   */
  @NonNull
  static IMetaschemaClassFactory newInstance(@NonNull ITypeResolver typeResolver) {
    return DefaultMetaschemaClassFactory.newInstance(typeResolver);
  }

  /**
   * Get the type resolver used to generate type information for Metasschema
   * constructs represented as Java classes, fields, and methods.
   *
   * @return the type resolver
   */
  @NonNull
  ITypeResolver getTypeResolver();

  /**
   * Generate a class in the provided {@code targetDirectory} that represents the
   * provided Module {@code module}.
   *
   * @param module
   *          the Module module to generate the class for
   * @param targetDirectory
   *          the directory to generate the Java class in
   * @return information about the generated class
   * @throws IOException
   *           if an error occurred while generating the Java class
   */
  @NonNull
  IGeneratedModuleClass generateClass(
      @NonNull IModule module,
      @NonNull Path targetDirectory) throws IOException;

  /**
   * Generate a class in the provided {@code targetDirectory} that represents the
   * provided Module definition's {@code typeInfo}.
   *
   * @param typeInfo
   *          the type information for the class to generate
   * @param targetDirectory
   *          the directory to generate the Java class in
   * @return the generated class details
   * @throws IOException
   *           if an error occurred while generating the Java class
   */
  @NonNull
  IGeneratedDefinitionClass generateClass(
      @NonNull IModelDefinitionTypeInfo typeInfo,
      @NonNull Path targetDirectory) throws IOException;

  /**
   * Generate a package-info.java class in the provided {@code targetDirectory}
   * that represents a collection of Module modules.
   *
   * @param javaPackage
   *          the Java package name to use
   * @param xmlNamespace
   *          the default XML namespace for all bound Module information elements
   *          in the generated package
   * @param metaschemaProductions
   *          a collection of previously generated Module modules and definition
   *          classes
   * @param targetDirectory
   *          the directory to generate the Java class in
   * @return the generated class details
   * @throws IOException
   *           if an error occurred while generating the Java class
   */
  @NonNull
  IGeneratedClass generatePackageInfoClass(
      @NonNull String javaPackage,
      @NonNull URI xmlNamespace,
      @NonNull Collection<IGeneratedModuleClass> metaschemaProductions,
      @NonNull Path targetDirectory) throws IOException;
}
