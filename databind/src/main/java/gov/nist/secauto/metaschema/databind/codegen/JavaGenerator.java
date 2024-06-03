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
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.codegen.config.IBindingConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods for generating Java classes based on a single or a
 * collection of Metaschemas.
 */
public final class JavaGenerator {
  private static final Logger LOGGER = LogManager.getLogger(JavaGenerator.class);

  private JavaGenerator() {
    // disable construction
  }

  /**
   * Generate Java sources for the provided Metaschema module.
   *
   * @param module
   *          the Metaschema module to generate Java sources for
   * @param targetDir
   *          the directory to generate sources in
   * @param bindingConfiguration
   *          the binding customizations to use when generating the Java classes
   * @return information about all the produced classes
   * @throws IOException
   *           if an error occurred while generating the class
   */
  public static IProduction generate(
      @NonNull IModule module,
      @NonNull Path targetDir,
      @NonNull IBindingConfiguration bindingConfiguration) throws IOException {
    return generate(CollectionUtil.singletonList(module), targetDir, bindingConfiguration);
  }

  /**
   * Generates Java classes for Module fields and flags.
   *
   * @param modules
   *          the Metaschema modules to build classes for
   * @param targetDirectory
   *          the directory to generate classes in
   * @param bindingConfiguration
   *          binding customizations that can be used to set namespaces, class
   *          names, and other aspects of generated classes
   * @return information about all the produced classes
   * @throws IOException
   *           if a build error occurred while generating the class
   */
  @NonNull
  public static IProduction generate(
      @NonNull Collection<? extends IModule> modules,
      @NonNull Path targetDirectory,
      @NonNull IBindingConfiguration bindingConfiguration) throws IOException {
    Objects.requireNonNull(modules, "metaschemas");
    Objects.requireNonNull(targetDirectory, "generationTargetDirectory");
    Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Generating Java classes in: {}", targetDirectory);
    }

    return IProduction.of(modules, bindingConfiguration, targetDirectory);
  }
}
