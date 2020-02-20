/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.codegen.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.MutableConfiguration;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

public class BasicMetaschema {
  private static final Logger logger = LogManager.getLogger(BasicMetaschema.class);

  private static final MetaschemaLoader loader = new MetaschemaLoader();

  private static Metaschema loadMetaschema(File metaschemaFile) throws MetaschemaException, IOException {
    return loader.loadXmlMetaschema(metaschemaFile);
  }

  public static Class<?> compileMetaschema(File metaschemaFile, File classDir)
      throws IOException, ClassNotFoundException, MetaschemaException {
    Metaschema metaschema = loadMetaschema(metaschemaFile);

    String rootClassName = null;

    List<JavaGenerator.GeneratedClass> classesToCompile = new LinkedList<>();
    for (Map.Entry<Metaschema, List<JavaGenerator.GeneratedClass>> entry : JavaGenerator.generate(metaschema, classDir)
        .entrySet()) {
      Metaschema containingMetaschema = entry.getKey();
      for (JavaGenerator.GeneratedClass generatedClass : entry.getValue()) {
        classesToCompile.add(generatedClass);

        if (rootClassName == null && Objects.equals(containingMetaschema, metaschema) && generatedClass.isRootClass()) {
          rootClassName = generatedClass.getClassName();
        }
      }
    }

    DynamicJavaCompiler compiler = new DynamicJavaCompiler(classDir);
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    Class<?> retval = null;
    if (!compiler.compileGeneratedClasses(classesToCompile, diagnostics)) {
      logger.error(diagnostics.getDiagnostics().toString());
      throw new IllegalStateException("failed to compile classes");
    } else {
      retval = compiler.getClassLoader().loadClass(rootClassName);
    }

    return retval;
    // return new DynamicClassLoader(classDir).loadClass(rootClassName);
  }

  private static Object readXml(Reader reader, Class<?> rootClass) throws IOException, BindingException {
    BindingContext context = BindingContext.newInstance();
    Object value = context.newXmlDeserializer(rootClass, null).deserialize(reader);
    return value;
  }

  private static <CLASS> void writeXml(Writer writer, CLASS rootObject) throws IOException, BindingException {
    BindingContext context = BindingContext.newInstance();
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) rootObject.getClass();
    context.newXmlSerializer(clazz, null).serialize(rootObject, writer);
  }

  private static String writeXml(Object rootObject) throws IOException, BindingException {
    StringWriter writer = new StringWriter();
    writeXml(writer, rootObject);
    return writer.toString();
  }

  @SuppressWarnings("unused")
  private static Object readJson(Reader reader, Class<?> rootClass) throws IOException, BindingException {
    BindingContext context = BindingContext.newInstance();
    return context.newJsonDeserializer(rootClass, new MutableConfiguration().enableFeature(Feature.DESERIALIZE_ROOT))
        .deserialize(reader);
  }

  private static <CLASS> void writeJson(Writer writer, CLASS rootObject) throws IOException, BindingException {
    BindingContext context = BindingContext.newInstance();
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) rootObject.getClass();
    context.newJsonSerializer(clazz, null).serialize(rootObject, writer);
  }

  @SuppressWarnings("unused")
  private static <CLASS> void writeYaml(Writer writer, CLASS rootObject) throws IOException, BindingException {
    BindingContext context = BindingContext.newInstance();
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) rootObject.getClass();
    context.newYamlSerializer(clazz, null).serialize(rootObject, writer);
  }

  private void runTests(String testPath, File classDir)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
    runTests(testPath, classDir, null);
  }

  private void runTests(String testPath, File classDir, java.util.function.Consumer<Object> assertions)
      throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
    Class<?> rootClass = compileMetaschema(
        new File(String.format("src/test/resources/metaschema/%s/metaschema.xml", testPath)), classDir);

    // File jsonExample = new File(String.format("src/test/resources/metaschema/%s/example.json",
    // testPath));
    // logger.info("Testing JSON file: {}", jsonExample.getName());
    // if (jsonExample.exists()) {
    // String json;
    // {
    // final Object root = readJson(new FileReader(jsonExample), rootClass);
    // logger.info("Read JSON: Object: {}", root.toString());
    // if (assertions != null) {
    // assertAll("Deserialize JSON", () -> assertions.accept(root));
    // }
    // json = writeJson(root);
    // logger.info("Write JSON: Object: {}", json);
    // }
    //
    // final Object root = readJson(new StringReader(json), rootClass);
    // if (assertions != null) {
    // assertAll("Deserialize JSON (roundtrip)", () -> assertions.accept(root));
    // }
    // }
    File xmlExample = new File(String.format("src/test/resources/metaschema/%s/example.xml", testPath));
    logger.info("Testing XML file: {}", xmlExample.getName());
    if (xmlExample.exists()) {
      String xml;
      {
        Object root = readXml(new FileReader(xmlExample), rootClass);
        logger.info("Read XML: Object: {}", root.toString());
        if (assertions != null) {
          assertAll("Deserialize XML", () -> assertions.accept(root));
        }
        xml = writeXml(root);
        logger.info("Write XML: Object: {}", xml);

        StringWriter writer = new StringWriter();
        writeJson(writer, root);
        logger.info("Write JSON: Object: {}", writer.toString());
      }

      // Object root = readXml(new StringReader(xml), rootClass);
      // if (assertions != null) {
      // assertAll("Deserialize XML (roundtrip)", () -> assertions.accept(root));
      // }
    }
  }

  @Test
  public void testSimpleMetaschema(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    File classDir = tempDir.toFile();
    // File classDir = new File("target/generated-sources/metaschema");

    runTests("simple", classDir, (obj) -> {
      try {
        Assertions.assertEquals("test", reflectMethod(obj, "getId"));
      } catch (NoSuchMethodException | SecurityException e) {
        Assertions.fail(e);
      }
    });
  }

  @Test
  public void testSimpleWithFieldMetaschema(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    File classDir = tempDir.toFile();
    // File classDir = new File("target/generated-sources/metaschema");
    runTests("simple_with_field", classDir);
  }

  private static Object reflectMethod(Object obj, String name) throws NoSuchMethodException, SecurityException {
    return ReflectionUtils.invokeMethod(obj.getClass().getMethod(name), obj);
  }

  @Test
  public void testFieldsWithFlagMetaschema(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    File classDir = tempDir.toFile();
    // File classDir = new File("target/generated-sources/metaschema");
    runTests("fields_with_flags", classDir, (obj) -> {
      try {
        Assertions.assertEquals("test", reflectMethod(obj, "getId"));
        Object field1 = ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexField1"), obj);
        Assertions.assertNotNull(field1);
        Assertions.assertEquals("complex-field1", reflectMethod(field1, "getId"));
        Assertions.assertEquals("test-string", reflectMethod(field1, "getValue"));

        @SuppressWarnings("unchecked")
        List<Object> field2s
            = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields2"), obj);
        Assertions.assertNotNull(field2s);
        Assertions.assertEquals(1, field2s.size());
        Object field2 = field2s.get(0);
        Assertions.assertEquals("complex-field2-1", reflectMethod(field2, "getId"));
        Assertions.assertEquals("test-string2", reflectMethod(field2, "getValue"));

        @SuppressWarnings("unchecked")
        List<Object> field3s
            = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields3"), obj);
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
          @SuppressWarnings("unchecked")
          Map<String, Object> collection
              = (Map<String, Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields4"), obj);
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
  public void testAssemblyMetaschema(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    File classDir = tempDir.toFile();
    // File classDir = new File("target/generated-sources/metaschema");

    runTests("assembly", classDir, (obj) -> {
      try {
        Assertions.assertEquals("test", reflectMethod(obj, "getId"));
      } catch (NoSuchMethodException | SecurityException e) {
        Assertions.fail(e);
      }
    });
  }

  public Object measureDeserializer(String format, File file, Deserializer<Object> deserializer, int iterations)
      throws FileNotFoundException, BindingException {
    Object retval = null;
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      retval = deserializer.deserialize(new FileReader(file));
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1000000;
      logger.info(String.format("%s read in %d milliseconds from %s", format, timeElapsed, file));
      totalTime += timeElapsed;
    }
    long average = totalTime / iterations;
    logger.info(String.format("%s read in %d milliseconds (on average) from %s", format, average, file));
    return retval;
  }

  public void measureSerializer(Object root, String format, File file, Serializer<Object> serializer, int iterations)
      throws BindingException, IOException {
    long totalTime = 0;
    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      serializer.serialize(root, new FileWriter(file));
      long endTime = System.nanoTime();
      long timeElapsed = (endTime - startTime) / 1000000;
      logger.info(String.format("%s written in %d milliseconds to %s", format, timeElapsed, file));
      totalTime += timeElapsed;
    }
    long average = totalTime / iterations;
    logger.info(String.format("%s written in %d milliseconds (on average) to %s", format, average, file));
  }

  @Test
  public void testOSCALCatalog(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    // File classDir = tempDir.toFile();
    File classDir = new File("target/generated-sources/metaschema");

    Class<?> rootClass
        = compileMetaschema(new File("../../../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml"), classDir);

    File xmlExample = new File("../../../../OSCAL/content/nist.gov/SP800-53/rev4/xml/NIST_SP-800-53_rev4_catalog.xml");
    logger.info("Testing XML file: {}", xmlExample.getName());
    if (xmlExample.exists()) {
      int iterations = 1;
      BindingContext context = BindingContext.newInstance();
      MutableConfiguration config
          = new MutableConfiguration().enableFeature(Feature.SERIALIZE_ROOT).enableFeature(Feature.DESERIALIZE_ROOT);
      @SuppressWarnings("unused")
      Object root = null;
      @SuppressWarnings("unchecked")
      Deserializer<Object> xmlDeserializer = context.newXmlDeserializer((Class<Object>) rootClass, config);
      root = measureDeserializer("XML", xmlExample, xmlDeserializer, iterations);
      //
      // File outXml = new File("target/oscal-catalog.xml");
      // @SuppressWarnings("unchecked")
      // Serializer<Object> xmlSerializer = context.newXmlSerializer((Class<Object>)rootClass, config);
      // measureSerializer(root, "XML", outXml, xmlSerializer, iterations);

      @SuppressWarnings("unchecked")
      Serializer<Object> jsonSerializer = context.newJsonSerializer((Class<Object>) rootClass, config);

      File outJson = new File("target/oscal-catalog.json");
      measureSerializer(root, "JSON", outJson, jsonSerializer, iterations);

      @SuppressWarnings("unchecked")
      Deserializer<Object> jsonDeserializer = context.newJsonDeserializer((Class<Object>) rootClass, config);
      root = measureDeserializer("JSON", outJson, jsonDeserializer, iterations);

      File outJson2 = new File("target/oscal-catalog-after-read.json");
      measureSerializer(root, "JSON", outJson2, jsonSerializer, iterations);

      // File outYaml = new File("target/oscal-catalog.yml");
      // writeYaml(new FileWriter(outYaml, Charset.forName("UTF-8")), root);
      // logger.info("Write YAML: Saved as: {}", outYaml.getPath());
    }

  }

  @Test
  @Disabled
  public void testOSCALCatalogMetrics(@TempDir Path tempDir)
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    File classDir = tempDir.toFile();
    // File classDir = new File("target/generated-sources/metaschema");

    Class<?> rootClass
        = compileMetaschema(new File("../../../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml"), classDir);

    File xmlExample = new File("../../../../OSCAL/content/nist.gov/SP800-53/rev4/xml/NIST_SP-800-53_rev4_catalog.xml");
    logger.info("Testing XML file: {}", xmlExample.getName());
    if (xmlExample.exists()) {
      int iterations = 50;
      BindingContext context = BindingContext.newInstance();
      MutableConfiguration config
          = new MutableConfiguration().enableFeature(Feature.SERIALIZE_ROOT).enableFeature(Feature.DESERIALIZE_ROOT);
      @SuppressWarnings("unchecked")
      Deserializer<Object> xmlDeserializer = context.newXmlDeserializer((Class<Object>) rootClass, config);
      Object root = measureDeserializer("XML", xmlExample, xmlDeserializer, iterations);

      File outXml = new File("target/oscal-catalog.xml");
      @SuppressWarnings("unchecked")
      Serializer<Object> xmlSerializer = context.newXmlSerializer((Class<Object>) rootClass, config);
      measureSerializer(root, "XML", outXml, xmlSerializer, iterations);

      File outJson = new File("target/oscal-catalog.json");
      @SuppressWarnings("unchecked")
      Serializer<Object> jsonSerializer = context.newJsonSerializer((Class<Object>) rootClass, config);
      measureSerializer(root, "JSON", outJson, jsonSerializer, iterations);

      @SuppressWarnings("unchecked")
      Deserializer<Object> jsonDeserializer = context.newJsonDeserializer((Class<Object>) rootClass, config);
      root = measureDeserializer("JSON", outJson, jsonDeserializer, iterations);

      // File outYaml = new File("target/oscal-catalog.yml");
      // writeYaml(new FileWriter(outYaml, Charset.forName("UTF-8")), root);
      // logger.info("Write YAML: Saved as: {}", outYaml.getPath());
    }

  }
}
