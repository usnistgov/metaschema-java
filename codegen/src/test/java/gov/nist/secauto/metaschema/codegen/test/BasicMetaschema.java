package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.secauto.metaschema.codegen.JavaGenerator;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaFactory;

public class BasicMetaschema {
	private static final Logger logger = LogManager.getLogger(BasicMetaschema.class);
	
	private static final JsonFactory jsonFactory = new JsonFactory();

	private static Metaschema loadMetaschema(File metaschemaFile) throws MetaschemaException, IOException {
		return MetaschemaFactory.loadMetaschemaFromXml(metaschemaFile);
	}

	public static Class<?> compileMetaschema(File metaschemaFile, File classDir)
			throws IOException, ClassNotFoundException, MetaschemaException {
		Metaschema metaschema = loadMetaschema(metaschemaFile);

		String rootClassName = JavaGenerator.generate(metaschema, classDir).get(metaschema);

		return new DynamicClassLoader(classDir).loadClass(rootClassName);
	}

	private static Object readJson(File file, Class<?> rootClass)
			throws JsonParseException, JsonMappingException, IOException {
		
		JsonParser parser = jsonFactory.createParser(file);
		
		if (!JsonToken.START_OBJECT.equals(parser.nextToken())) {
			String msg = String.format("Expected START_OBJECT. Invalid token: %s",parser.getCurrentToken());
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		findobject:
		while (!JsonToken.END_OBJECT.equals(parser.nextToken())) {
//			logger.trace("token: {}", parser.getCurrentToken());
			if (JsonToken.FIELD_NAME.equals(parser.getCurrentToken())) {
//				logger.trace("{}: {}",parser.getCurrentToken(), parser.getText());
				switch (parser.getText()) {

				case "$schema":
					// skip the value string
					if (!JsonToken.VALUE_STRING.equals(parser.nextToken())) {
						String msg = String.format("Expected VALUE_STRING. Invalid token: %s",parser.getCurrentToken());
						logger.error(msg);
						throw new RuntimeException(msg);
					}
					break;
				default:
					// advance to the object
					parser.nextToken();
					break findobject;
				}
			}
		}

		ObjectMapper mapper = new ObjectMapper(jsonFactory);
//		mapper.addHandler(new CustomDeserializationProblemHandler());
//		mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		return mapper.readValue(parser, rootClass);
	}

	private static String writeJson(Object rootObject) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
		
		objectMapper.writeValue(jsonGenerator, rootObject);
		return writer.toString();
	}

	private static Object readXml(File file, Class<?> rootClass) throws JAXBException {
		JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContext.newInstance(rootClass);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(file);
	}

	private static Object writeXml(Object rootObject) throws JAXBException {
		JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContext.newInstance(rootObject.getClass());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		StringWriter writer = new StringWriter();
		jaxbMarshaller.marshal(rootObject, writer);
		return writer.toString();
	}

	private void runTests(String testPath, File classDir) throws ClassNotFoundException, IOException, MetaschemaException, JAXBException {
		Class<?> rootClass = compileMetaschema(new File(String.format("src/test/resources/metaschema/%s/metaschema.xml",testPath)),
				classDir);

		File jsonExample = new File(String.format("src/test/resources/metaschema/%s/example.json", testPath));
		logger.info("Testing JSON file: {}", jsonExample.getName());
		if (jsonExample.exists()) {
			Object root = readJson(jsonExample, rootClass);
			logger.info("Read JSON: Object: {}", root.toString());
			logger.info("Write JSON: Object: {}", writeJson(root));
		}
		File xmlExample = new File(String.format("src/test/resources/metaschema/%s/example.xml", testPath));
		logger.info("Testing XML file: {}", xmlExample.getName());
		if (xmlExample.exists()) {
			Object root = readXml(xmlExample, rootClass);
			logger.info("Read XML: Object: {}", root.toString());
			logger.info("Write XML: Object: {}", writeXml(root));
		}
	}

	@Test
	public void testSimpleMetaschema(@TempDir Path tempDir) throws MetaschemaException, IOException, ClassNotFoundException, JAXBException {
		File classDir = tempDir.toFile();
//		File classDir = new File("target/generated-sources/metaschema");
		runTests("simple", classDir);
	}

	@Test
	public void testSimpleWithFieldMetaschema(@TempDir Path tempDir) throws MetaschemaException, IOException, ClassNotFoundException, JAXBException {
		File classDir = tempDir.toFile();
//		File classDir = new File("target/generated-sources/metaschema");
		runTests("simple_with_field", classDir);
	}

}
