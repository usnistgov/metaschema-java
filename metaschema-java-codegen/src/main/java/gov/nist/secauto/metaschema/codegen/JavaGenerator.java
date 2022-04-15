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

import gov.nist.secauto.metaschema.codegen.binding.config.IBindingConfiguration;
import gov.nist.secauto.metaschema.codegen.type.DefaultTypeResolver;
import gov.nist.secauto.metaschema.codegen.type.ITypeResolver;
import gov.nist.secauto.metaschema.model.common.IMetaschema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class JavaGenerator {
  private static final Logger LOGGER = LogManager.getLogger(JavaGenerator.class);

  private JavaGenerator() {
    // disable construction
  }

  /**
   * Generate Java sources for the provided metaschema.
   * 
   * @param metaschema
   *          the Metaschema to generate Java sources for
   * @param targetDir
   *          the directory to generate sources in
   * @param bindingConfiguration
   *          the binding customizations to use when generating the Java classes
   * @return information about all the produced classes
   * @throws IOException
   *           if a build error occurred while generating the class
   */
  public static Production generate(
      @NotNull IMetaschema metaschema,
      @NotNull Path targetDir,
      @NotNull IBindingConfiguration bindingConfiguration) throws IOException {
    return generate(Collections.singletonList(metaschema), targetDir, bindingConfiguration);
  }

  /**
   * Generates Java classes for Metaschema fields and flags.
   * 
   * @param metaschemas
   *          the Metaschema instances to build classes for
   * @param targetDirectory
   *          the directory to generate classes in
   * @param bindingConfiguration
   *          binding customizations that can be used to set namespaces, class names, and other
   *          aspects of generated classes
   * @return information about all the produced classes
   * @throws IOException
   *           if a build error occurred while generating the class
   */
  @NotNull
  public static Production generate(
      @NotNull Collection<? extends IMetaschema> metaschemas,
      @NotNull Path targetDirectory,
      @NotNull IBindingConfiguration bindingConfiguration) throws IOException {
    Objects.requireNonNull(metaschemas, "metaschemas");
    Objects.requireNonNull(targetDirectory, "generationTargetDirectory");
    Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Generating Java classes in: {}", targetDirectory);
    }

    ITypeResolver typeResolver = new DefaultTypeResolver(bindingConfiguration);

    return Production.of(metaschemas, typeResolver, targetDirectory);
  }
}
