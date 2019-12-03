package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonFactory;
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
		JsonFactory factory = new JsonFactory();
		JsonParser  parser  = factory.createParser(file);
		
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

		ObjectMapper mapper = new ObjectMapper(factory);
//		mapper.addHandler(new CustomDeserializationProblemHandler());
//		mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		return mapper.readValue(parser, rootClass);
	}

	@SuppressWarnings("unused")
	private static Object readXml(File file, Class<?> rootClass) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(rootClass);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(file);
	}

	@Test
	public void testSimpleMetaschema(@TempDir Path tempDir) throws MetaschemaException, IOException, ClassNotFoundException, JAXBException {
		File classDir = tempDir.toFile();
//		File classDir = new File("target/generated-sources/metaschema");

		Class<?> rootClass = compileMetaschema(new File("src/test/resources/metaschema/simple_metaschema.xml"),
				classDir);

		Object root = readJson(new File("src/test/resources/metaschema/simple.json"), rootClass);
		logger.info("JSON: Object: {}", ToStringBuilder.reflectionToString(root));
//		root = readXml(new File("src/test/resources/metaschema/simple.xml"), rootClass);
//		logger.info("XML: Object: {}", ToStringBuilder.reflectionToString(root));
	}

	@Test
	public void testSimpleWithFlagMetaschema(@TempDir Path tempDir) throws MetaschemaException, IOException, ClassNotFoundException, JAXBException {
//		File classDir = tempDir.toFile();
		File classDir = new File("target/generated-sources/metaschema");

		Class<?> rootClass = compileMetaschema(new File("src/test/resources/metaschema/simple-with-field_metaschema.xml"),
				classDir);
//		Class<?> rootClass = TopLevel.class;

		Object root = readJson(new File("src/test/resources/metaschema/simple-with-field.json"), rootClass);
		logger.info("JSON: Object: {}", ToStringBuilder.reflectionToString(root, new RecursiveToStringStyle()));
//		root = readXml(new File("src/test/resources/metaschema/simple-with-field.xml"), rootClass);
//		logger.info("XML: Object: {}", ToStringBuilder.reflectionToString(root, new RecursiveToStringStyle()));
	}

}
