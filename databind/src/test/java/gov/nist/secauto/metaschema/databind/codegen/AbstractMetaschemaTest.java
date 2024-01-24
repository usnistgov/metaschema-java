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

import static org.junit.jupiter.api.Assertions.assertAll;

import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.IXmlModule;
import gov.nist.secauto.metaschema.core.model.xml.ModuleLoader;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.codegen.config.DefaultBindingConfiguration;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

abstract class AbstractMetaschemaTest {

  private static final ModuleLoader LOADER = new ModuleLoader();
  private static final Logger LOGGER = LogManager.getLogger(AbstractMetaschemaTest.class);
  // @TempDir
  // Path generationDir;
  @NonNull
  Path generationDir = ObjectUtils.notNull(Paths.get("target/generated-test-sources/metaschema"));

  @NonNull
  private static IXmlModule loadModule(@NonNull Path moduleFile) throws MetaschemaException, IOException {
    return LOADER.load(moduleFile);
  }

  public static Class<?> compileModule(@NonNull Path moduleFile, @Nullable Path bindingFile,
      @NonNull String rootClassName, @NonNull Path classDir)
      throws IOException, ClassNotFoundException, MetaschemaException {
    IXmlModule module = loadModule(moduleFile);

    DefaultBindingConfiguration bindingConfiguration = new DefaultBindingConfiguration();
    if (bindingFile != null && Files.exists(bindingFile) && Files.isRegularFile(bindingFile)) {
      bindingConfiguration.load(bindingFile);
    }

    ModuleCompilerHelper.compileModule(module, classDir, bindingConfiguration);

    // Load classes
    return ModuleCompilerHelper.newClassLoader(
        classDir,
        ObjectUtils.notNull(Thread.currentThread().getContextClassLoader()))
        .loadClass(rootClassName);
  }

  private static Object read(
      @NonNull Format format,
      @NonNull Path file,
      @NonNull Class<?> rootClass,
      @NonNull IBindingContext context)
      throws IOException {
    IDeserializer<?> deserializer = context.newDeserializer(format, rootClass);
    LOGGER.info("Reading content: {}", file);
    Object value = deserializer.deserialize(file);
    return value;
  }

  private static <CLASS> void write(
      @NonNull Format format,
      @NonNull Path file,
      @NonNull CLASS rootObject,
      @NonNull IBindingContext context) throws IOException {
    @SuppressWarnings("unchecked") Class<CLASS> clazz = (Class<CLASS>) rootObject.getClass();

    try (Writer writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      assert writer != null;
      context.newSerializer(format, clazz).serialize(rootObject, writer);
    }
  }

  public static void runTests(@NonNull String testPath, @NonNull String rootClassName, @NonNull Path classDir)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
    runTests(testPath, rootClassName, classDir, null);
  }

  @SuppressWarnings("unused")
  public static void runTests(
      @NonNull String testPath,
      @NonNull String rootClassName,
      @NonNull Path classDir,
      java.util.function.Consumer<Object> assertions)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
    runTests(
        ObjectUtils.notNull(Paths.get(String.format("src/test/resources/metaschema/%s/metaschema.xml", testPath))),
        ObjectUtils.notNull(Paths.get(String.format("src/test/resources/metaschema/%s/binding.xml", testPath))),
        ObjectUtils.notNull(Paths.get(String.format("src/test/resources/metaschema/%s/example.xml", testPath))),
        rootClassName,
        classDir,
        assertions);
  }

  @SuppressWarnings("unused")
  public static void runTests(
      @NonNull Path metaschemaPath,
      @NonNull Path bindingPath,
      @Nullable Path examplePath,
      @NonNull String rootClassName,
      @NonNull Path classDir,
      java.util.function.Consumer<Object> assertions)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {

    Class<?> rootClass = compileModule(
        metaschemaPath,
        bindingPath,
        rootClassName,
        classDir);
    assert rootClass != null;

    if (examplePath != null && Files.exists(examplePath)) {
      IBindingContext context = new DefaultBindingContext();
      LOGGER.info("Testing XML file: {}", examplePath.toString());
      String xml;
      {

        Object root = read(Format.XML, examplePath, rootClass, context);
        LOGGER.atDebug().log("Read XML: Object: {}", root.toString());
        if (assertions != null) {
          assertAll("Deserialize XML", () -> {
            assertions.accept(root);
          });
        }

        LOGGER.atDebug().log("Write XML:");
        write(Format.XML, ObjectUtils.notNull(Paths.get("target/out.xml")), root, context);

        LOGGER.atDebug().log("Write JSON:");
        write(Format.XML, ObjectUtils.notNull(Paths.get("target/out.json")), root, context);
      }

      Object root = read(Format.XML, ObjectUtils.notNull(Paths.get("target/out.xml")), rootClass, context);
      if (assertions != null) {
        assertAll("Deserialize XML (roundtrip)", () -> assertions.accept(root));
      }
    }
  }
}
