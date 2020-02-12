package gov.nist.secauto.metaschema.binding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.io.json.parser.DefaultJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;

abstract class AbstractJsonDeserializer<CLASS> extends AbstractDeserializer<CLASS> {
	private static final Logger logger = LogManager.getLogger(AbstractJsonDeserializer.class);

	private JsonFactory jsonFactory;

	public AbstractJsonDeserializer(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding, Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	protected abstract JsonFactory getJsonFactoryInstance();

	protected JsonFactory getJsonFactory() {
		synchronized (this) {
			if (jsonFactory == null) {
				jsonFactory = getJsonFactoryInstance();
			}
			return jsonFactory;
		}
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		synchronized (this) {
			this.jsonFactory = jsonFactory;
		}
	}

	protected JsonParser newJsonParser(Reader reader) throws BindingException {
		try {
			JsonFactory factory = getJsonFactory();
			JsonParser retval = factory.createParser(reader);
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	protected JsonGenerator newJsonGenerator(Writer writer) throws BindingException {
		try {
			JsonFactory factory = getJsonFactory();
			JsonGenerator retval = factory.createGenerator(writer);
			retval.setPrettyPrinter(new DefaultPrettyPrinter());
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public CLASS deserialize(Reader reader) throws BindingException {
		JsonParser parser = newJsonParser(reader);

		
		Class<CLASS> clazz = getClassBinding().getClazz();

		JsonToken token;
		try {
			token = parser.nextToken();
		} catch (IOException ex) {
			throw new BindingException("Unable to read JSON.", ex);
		}
		if (!parser.isExpectedStartObjectToken()) {
			throw new BindingException(String.format("Start object expected. Found '%s'.", token.toString()));
		}


		ClassBinding<CLASS> classBinding = getClassBinding();
		boolean isParseRoot = false;
		if (getConfiguration().isFeatureEnabled(Feature.DESERIALIZE_ROOT, false)) {
			if (classBinding.hasRootWrapper()) {
				isParseRoot = true;
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("Root parsing is enabled (DESERIALIZE_ROOT), but class '%s' is missing a '%s' annotation.", clazz.getName(), RootWrapper.class.getName()));
			}
		}

		BindingContext bindingContext = getBindingContext();
		JsonParsingContext parsingContext = new DefaultJsonParsingContext(parser, bindingContext);
		return classBinding.getJsonReader(bindingContext).readJson(parsingContext, null, isParseRoot);
	}

}
