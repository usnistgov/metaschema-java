package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class AssemblyJsonReader<CLASS> extends
		AbstractJsonReader<CLASS, AssemblyClassBinding<CLASS>, SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>>> {
	private static final Logger logger = LogManager.getLogger(AssemblyJsonReader.class);

	public AssemblyJsonReader(AssemblyClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	protected Map<PropertyBinding, Supplier<?>> handleUnknownProperty(JsonParsingContext parsingContext)
			throws BindingException {
		try {
			// TODO: log a warning?
			JsonUtil.skipValue(parsingContext.getEventReader());
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		return Collections.emptyMap();
	}

	@Override
	protected SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> newObjectParser(PropertyBindingFilter filter,
			JsonParsingContext parsingContext) throws BindingException {
		return new SingleBoundObjectParser<>(getClassBinding(), filter, parsingContext,
				(fieldName, props, context) -> handleUnknownProperty(context));
	}

	@Override
	protected List<CLASS> readJsonInternal(SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> parser,
			boolean parseRoot) throws BindingException {
		List<CLASS> retval;
		if (parseRoot) {
			try {
				retval = parseRoot(parser);
			} catch (IOException ex) {
				throw new BindingException(ex);
			}
		} else {
			retval = super.readJsonInternal(parser, false);
		}
		return retval;
	}

	protected List<CLASS> parseRoot(SingleBoundObjectParser<CLASS, AssemblyClassBinding<CLASS>> objParser)
			throws BindingException, IOException {
		JsonParsingContext parsingContext = objParser.getParsingContext();
		JsonParser parser = parsingContext.getEventReader();
		RootWrapper rootWrapper = getClassBinding().getRootWrapper();
		String rootName = rootWrapper.name();
		String[] ignoreFieldsArray = rootWrapper.ignoreJsonProperties();
		Set<String> ignoreRootFields;
		if (ignoreFieldsArray == null || ignoreFieldsArray.length == 0) {
			ignoreRootFields = Collections.emptySet();
		} else {
			ignoreRootFields = new HashSet<>(Arrays.asList(ignoreFieldsArray));
		}

		List<CLASS> retval = Collections.emptyList();
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
				retval = super.readJsonInternal(objParser, false);
			} else if (ignoreRootFields.contains(fieldName)) {
				// ignore the field
				JsonUtil.skipValue(parser);
			} else {
				if (!parsingContext.getProblemHandler().handleUnknownRootProperty(objParser.getInstance(),
						getClassBinding(), fieldName, parsingContext)) {
					logger.warn("Skipping unhandled top-level JSON field '{}'.", fieldName);
					JsonUtil.skipValue(parser);
				}
			}
		}
		JsonUtil.expectCurrentToken(parser, null);

		return retval;
	}

}
