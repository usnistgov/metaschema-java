package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class AssemblyJsonReader<CLASS> extends AbstractJsonReader<CLASS, AssemblyClassBinding<CLASS>> {
	private static final Logger logger = LogManager.getLogger(AssemblyJsonReader.class);

	public AssemblyJsonReader(AssemblyClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	public CLASS readJson(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter, boolean parseRoot) throws BindingException {
		CLASS retval = getClassBinding().newInstance();

		try {
			if (parseRoot) {
				parseRoot(retval, filter, parsingContext);
			} else {
				parseBody(retval, filter, parsingContext);
			}
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		return retval;
	}

	protected void parseRoot(CLASS obj, NamedPropertyBindingFilter filter, JsonParsingContext parsingContext) throws BindingException, IOException {
		JsonParser parser = parsingContext.getEventReader();
		RootWrapper rootWrapper = getClassBinding().getRootWrapper();
		String rootName = rootWrapper.name();
		String[] ignoreFieldsArray = rootWrapper.ignoreJsonProperties();
		Set<String> ignoreRootFields;
		if (ignoreFieldsArray == null || ignoreFieldsArray.length == 0) {
			ignoreRootFields = Collections.emptySet();
		} else {
			ignoreRootFields =  new HashSet<>(Arrays.asList(ignoreFieldsArray));
		}
		JsonToken token;
		while ((token = parser.nextToken()) != null) {
//			logger.info("Token: {}", token.toString());
			if (JsonToken.END_OBJECT.equals(token)) {
				break;
			} else if (!JsonToken.FIELD_NAME.equals(token)) {
				throw new BindingException(String.format("Expected FIELD_NAME token, found '%s'", token.toString()));
			}

			String fieldName = parser.currentName();
			if (fieldName.equals(rootName)) {
				// process the object value, bound to the requested class
				JsonUtil.readNextToken(parser, JsonToken.START_OBJECT);
				parseBody(obj, filter, parsingContext);
			} else if (ignoreRootFields.contains(fieldName)) {
				// ignore the field
				JsonUtil.skipValue(parser);
			} else {
				if (!parsingContext.getProblemHandler().handleUnknownProperty(obj, fieldName, parsingContext)) {
					logger.warn("Skipping unhandled JSON field '{}'.", fieldName);
					JsonUtil.skipValue(parser);
				}
			}
		}
		JsonUtil.expectCurrentToken(parser, null);
	}
}
