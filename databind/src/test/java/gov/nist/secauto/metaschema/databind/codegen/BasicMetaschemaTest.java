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
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.MetaschemaLoader;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.codegen.config.DefaultBindingConfiguration;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class BasicMetaschemaTest {
  private static final MetaschemaLoader LOADER = new MetaschemaLoader();
  private static final Logger LOGGER = LogManager.getLogger(BasicMetaschemaTest.class);
  // @TempDir
  // Path generationDir;
  @NonNull
  Path generationDir = ObjectUtils.notNull(Paths.get("target/generated-test-sources/metaschema"));

  @NonNull
  private static IMetaschema loadMetaschema(@NonNull Path metaschemaFile) throws MetaschemaException, IOException {
    return LOADER.load(metaschemaFile);
  }

  public static Class<?> compileMetaschema(@NonNull Path metaschemaFile, @Nullable Path bindingFile,
      @NonNull String rootClassName, @NonNull Path classDir)
      throws IOException, ClassNotFoundException, MetaschemaException {
    IMetaschema metaschema = loadMetaschema(metaschemaFile);

    DefaultBindingConfiguration bindingConfiguration = new DefaultBindingConfiguration();
    if (bindingFile != null && Files.exists(bindingFile) && Files.isRegularFile(bindingFile)) {
      bindingConfiguration.load(bindingFile);
    }

    MetaschemaCompilerHelper.compileMetaschema(metaschema, classDir, bindingConfiguration);

    // Load classes
    return MetaschemaCompilerHelper.getClassLoader(
        classDir,
        ObjectUtils.notNull(Thread.currentThread().getContextClassLoader()))
        .loadClass(rootClassName);
  }

  private static Object read(@NonNull Format format, @NonNull Path file, @NonNull Class<?> rootClass)
      throws IOException {
    IBindingContext context = IBindingContext.instance();

    IDeserializer<?> deserializer = context.newDeserializer(format, rootClass);
    LOGGER.info("Reading content: {}", file);
    Object value = deserializer.deserialize(file);
    return value;
  }

  private static <CLASS> void write(@NonNull Format format, @NonNull Path file, CLASS rootObject) throws IOException {
    IBindingContext context = IBindingContext.instance();
    @SuppressWarnings("unchecked") Class<CLASS> clazz = (Class<CLASS>) rootObject.getClass();

    try (Writer writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      assert writer != null;
      context.newSerializer(format, clazz).serialize(rootObject, writer);
    }
  }

  private static void runTests(@NonNull String testPath, @NonNull String rootClassName, @NonNull Path classDir)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
    runTests(testPath, rootClassName, classDir, null);
  }

  @SuppressWarnings("unused")
  private static void runTests(
      @NonNull String testPath,
      @NonNull String rootClassName,
      @NonNull Path classDir,
      java.util.function.Consumer<Object> assertions)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {

    Class<?> rootClass = compileMetaschema(
        ObjectUtils.notNull(Paths.get(String.format("src/test/resources/metaschema/%s/metaschema.xml", testPath))),
        Paths.get(String.format("src/test/resources/metaschema/%s/binding.xml", testPath)),
        rootClassName,
        classDir);

    assert rootClass != null;

    Path xmlExample = Paths.get(String.format("src/test/resources/metaschema/%s/example.xml",
        testPath));
    LOGGER.info("Testing XML file: {}", xmlExample.toString());
    if (Files.exists(xmlExample)) {
      String xml;
      {
        Object root = read(Format.XML, xmlExample, rootClass);
        LOGGER.atDebug().log("Read XML: Object: {}", root.toString());
        if (assertions != null) {
          assertAll("Deserialize XML", () -> {
            assertions.accept(root);
          });
        }

        LOGGER.atDebug().log("Write XML:");
        write(Format.XML, ObjectUtils.notNull(Paths.get("target/out.xml")), root);

        LOGGER.atDebug().log("Write JSON:");
        write(Format.XML, ObjectUtils.notNull(Paths.get("target/out.json")), root);
      }

      Object root = read(Format.XML, ObjectUtils.notNull(Paths.get("target/out.xml")), rootClass);
      if (assertions != null) {
        assertAll("Deserialize XML (roundtrip)", () -> assertions.accept(root));
      }
    }
  }

  @Test
  void testSimpleMetaschema() throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("simple", "gov.nist.csrc.ns.metaschema.testing.simple.TopLevel", generationDir);
    // runTests("simple", "gov.nist.csrc.ns.metaschema.testing.simple.TopLevel",
    // generationDir, (obj) ->
    // {
    // try {
    // Assertions.assertEquals("test", reflectMethod(obj, "getId"));
    // } catch (NoSuchMethodException | SecurityException e) {
    // Assertions.fail(e);
    // }
    // });
  }

  @Test
  void testSimpleUuidMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("simple_with_uuid", "gov.nist.csrc.ns.metaschema.testing.simple.with.uuid.TopLevel", generationDir,
        (obj) -> {
          try {
            Assertions.assertEquals("5de455cf-2f8d-4da2-9182-323d433e1065", reflectMethod(obj, "getUuid").toString());
          } catch (NoSuchMethodException | SecurityException e) {
            Assertions.fail(e);
          }
        });
  }

  @Test
  void testSimpleWithFieldMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("simple_with_field", "gov.nist.csrc.ns.metaschema.testing.simple.with.field.TopLevel", generationDir);
  }

  private static Object reflectMethod(Object obj, String name) throws NoSuchMethodException {
    return ReflectionUtils.invokeMethod(obj.getClass().getMethod(name), obj);
  }

  @Test
  void testFieldsWithFlagMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("fields_with_flags", "gov.nist.csrc.ns.metaschema.testing.fields.with.flags.TopLevel",
        generationDir,
        (obj) -> {
          try {
            Assertions.assertEquals("test", reflectMethod(obj, "getId"));
            Object field1 = ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexField1"), obj);
            Assertions.assertNotNull(field1);
            Assertions.assertEquals("complex-field1", reflectMethod(field1, "getId"));
            Assertions.assertEquals("test-string", reflectMethod(field1, "getValue"));

            @SuppressWarnings("unchecked") List<Object> field2s
                = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields2"),
                    obj);
            Assertions.assertNotNull(field2s);
            Assertions.assertEquals(1, field2s.size());
            Object field2 = field2s.get(0);
            Assertions.assertEquals("complex-field2-1", reflectMethod(field2, "getId"));
            Assertions.assertEquals("test-string2", reflectMethod(field2, "getValue"));

            @SuppressWarnings("unchecked") List<Object> field3s
                = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields3"),
                    obj);
            Assertions.assertEquals(2, field3s.size());
            Assertions.assertAll("ComplexFields4 item", () -> {
              Object item = field3s.get(0);
              assertEquals("complex-field3-1", reflectMethod(item, "getId2"));
              assertEquals("test-string3", reflectMethod(item, "getValue"));
            });
            Assertions.assertAll("ComplexFields4 item", () -> {
              Object item = field3s.get(1);
              assertEquals("complex-field3-2", reflectMethod(item, "getId2"));
              assertEquals("test-string4", reflectMethod(item, "getValue"));
            });

            Assertions.assertAll("ComplexFields4", () -> {
              @SuppressWarnings("unchecked") Map<String, Object> collection
                  = (Map<String, Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields4"),
                      obj);
              Assertions.assertNotNull(collection);
              Assertions.assertEquals(2, collection.size());
              Set<Map.Entry<String, Object>> entries = collection.entrySet();
              Iterator<Map.Entry<String, Object>> iter = entries.iterator();

              Assertions.assertAll("ComplexFields4 item", () -> {
                Map.Entry<String, Object> entry = iter.next();
                assertEquals("complex-field4-1", entry.getKey());
                assertEquals("complex-field4-1", reflectMethod(entry.getValue(), "getId2"));
                assertEquals("test-string5", reflectMethod(entry.getValue(), "getValue"));
              });

              Assertions.assertAll("ComplexFields4 item", () -> {
                Map.Entry<String, Object> entry = iter.next();
                assertEquals("complex-field4-2", entry.getKey());
                assertEquals("complex-field4-2", reflectMethod(entry.getValue(), "getId2"));
                assertEquals("test-string6", reflectMethod(entry.getValue(), "getValue"));
              });
            });
          } catch (NoSuchMethodException | SecurityException e) {
            Assertions.fail(e);
          }
        });
  }

  @Test
  void testAssemblyMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("assembly", "gov.nist.itl.metaschema.codegen.xml.example.assembly.TopLevel", generationDir, (obj) -> {
      try {
        Assertions.assertEquals("test", reflectMethod(obj, "getId"));
      } catch (NoSuchMethodException | SecurityException e) {
        Assertions.fail(e);
      }
    });
  }

  @Test
  void testLocalDefinitionsMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("local-definitions", "gov.nist.csrc.ns.metaschema.testing.local.definitions.TopLevel", generationDir);
  }
}
