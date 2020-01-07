package gov.nist.secauto.metaschema.codegen.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ReflectionUtils;

import gov.nist.secauto.metaschema.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaFactory;

public class BasicMetaschema {
	private static final Logger logger = LogManager.getLogger(BasicMetaschema.class);

	private static Metaschema loadMetaschema(File metaschemaFile) throws MetaschemaException, IOException {
		return MetaschemaFactory.loadMetaschemaFromXml(metaschemaFile);
	}

	public static Class<?> compileMetaschema(File metaschemaFile, File classDir)
			throws IOException, ClassNotFoundException, MetaschemaException {
		Metaschema metaschema = loadMetaschema(metaschemaFile);

		String rootClassName = null;
		
		List<JavaGenerator.GeneratedClass> classesToCompile = new LinkedList<>();
		for (Map.Entry<Metaschema, List<JavaGenerator.GeneratedClass>> entry : JavaGenerator.generate(metaschema, classDir).entrySet()) {
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
		} else {
			retval = compiler.getClassLoader().loadClass(rootClassName);
		}

		return retval;
//		return new DynamicClassLoader(classDir).loadClass(rootClassName);
	}

	private static Object readJson(Reader reader, Class<?> rootClass)
			throws IOException {

		return null;
	}

	private static String writeJson(Object rootObject)
			throws IOException {
		StringWriter writer = new StringWriter();
//		JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
//		jsonGenerator.writeObject(rootObject);
		return writer.toString();
	}

	private static Object readXml(Reader reader, Class<?> rootClass) throws IOException, BindingException {
		BindingContext context = BindingContext.newInstance();
		Object value = context.parseXml(reader, rootClass);
		return value;
	}

	private static String writeXml(Object rootObject) throws IOException {
		StringWriter writer = new StringWriter();
//		JacksonUtil.writeXml(writer, rootObject);
		return writer.toString();
	}

	private void runTests(String testPath, File classDir)
			throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
		runTests(testPath, classDir, null);
	}

	private void runTests(String testPath, File classDir, java.util.function.Consumer<Object> assertions)
			throws ClassNotFoundException, IOException, MetaschemaException, BindingException {
		Class<?> rootClass = compileMetaschema(
				new File(String.format("src/test/resources/metaschema/%s/metaschema.xml", testPath)), classDir);

//		File jsonExample = new File(String.format("src/test/resources/metaschema/%s/example.json", testPath));
//		logger.info("Testing JSON file: {}", jsonExample.getName());
//		if (jsonExample.exists()) {
//			String json;
//			{
//				final Object root = readJson(new FileReader(jsonExample), rootClass);
//				logger.info("Read JSON: Object: {}", root.toString());
//				if (assertions != null) {
//					assertAll("Deserialize JSON", () -> assertions.accept(root));
//				}
//				json = writeJson(root);
//				logger.info("Write JSON: Object: {}", json);
//			}
//
//			final Object root = readJson(new StringReader(json), rootClass);
//			if (assertions != null) {
//				assertAll("Deserialize JSON (roundtrip)", () -> assertions.accept(root));
//			}
//		}
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
//				xml = writeXml(root);
//				logger.info("Write XML: Object: {}", xml);
			}

//			Object root = readXml(new StringReader(xml), rootClass);
//			if (assertions != null) {
//				assertAll("Deserialize XML (roundtrip)", () -> assertions.accept(root));
//			}
		}
	}

	@Test
	public void testSimpleMetaschema(@TempDir Path tempDir)
			throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
		File classDir = tempDir.toFile();
//		File classDir = new File("target/generated-sources/metaschema");

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
//		File classDir = new File("target/generated-sources/metaschema");
		runTests("simple_with_field", classDir);
	}

	private static Object reflectMethod(Object obj, String name) throws NoSuchMethodException, SecurityException {
		return ReflectionUtils.invokeMethod(obj.getClass().getMethod(name), obj);
	}

	@Test
	public void testFieldsWithFlagMetaschema(@TempDir Path tempDir)
			throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
		File classDir = tempDir.toFile();
//		File classDir = new File("target/generated-sources/metaschema");
		runTests("fields_with_flags", classDir, (obj) -> {
			try {
				Assertions.assertEquals("test", reflectMethod(obj, "getId"));
				Object field1 = ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexField1"), obj);
				Assertions.assertNotNull(field1);
				Assertions.assertEquals("complex-field1", reflectMethod(field1, "getId"));
				Assertions.assertEquals("test-string", reflectMethod(field1, "getValue"));

				@SuppressWarnings("unchecked")
				List<Object> field2s = (List<Object>) ReflectionUtils
						.invokeMethod(obj.getClass().getMethod("getComplexFields2"), obj);
				Assertions.assertNotNull(field2s);
				Assertions.assertEquals(1, field2s.size());
				Object field2 = field2s.get(0);
				Assertions.assertEquals("complex-field2-1", reflectMethod(field2, "getId"));
				Assertions.assertEquals("test-string2", reflectMethod(field2, "getValue"));

				@SuppressWarnings("unchecked")
				List<Object> field3s = (List<Object>) ReflectionUtils
						.invokeMethod(obj.getClass().getMethod("getComplexFields3"), obj);
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
					Map<String, Object> collection = (Map<String, Object>) ReflectionUtils
							.invokeMethod(obj.getClass().getMethod("getComplexFields4"), obj);
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
//		File classDir = new File("target/generated-sources/metaschema");

		runTests("assembly", classDir, (obj) -> {
			try {
				Assertions.assertEquals("test", reflectMethod(obj, "getId"));
			} catch (NoSuchMethodException | SecurityException e) {
				Assertions.fail(e);
			}
		});
	}

	@Test
	public void testOSCALCatalog(@TempDir Path tempDir)
			throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
//		File classDir = tempDir.toFile();
		File classDir = new File("target/generated-sources/metaschema");

		Class<?> rootClass = compileMetaschema(
				new File("../../../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml"), classDir);

		File xmlExample = new File("../../../../OSCAL/content/nist.gov/SP800-53/rev4/xml/NIST_SP-800-53_rev4_catalog.xml");
		logger.info("Testing XML file: {}", xmlExample.getName());
		if (xmlExample.exists()) {
			Object root = readXml(new FileReader(xmlExample), rootClass);
			logger.debug("Read XML: Object: {}", root.toString());
			logger.info("done");
		}
	}
}
