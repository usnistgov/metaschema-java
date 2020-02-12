package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public class SingletonPropertyValueHandler extends AbstractPropertyValueHandler {

	private Object value;

	public SingletonPropertyValueHandler(ClassBinding<?> classBinding,
			PropertyItemHandler propertyItemHandler) {
		super(classBinding, propertyItemHandler);
	}

	@Override
	public boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException {
		// Parse the value at the current token; after this the current token is
		// expected to be the end of the value (e.g., VALUE, END_OBJECT
		PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
		List<Object> values = propertyItemHandler.parse(parsingContext, null);

		if (values == null) {
			throw new BindingException(String.format(
					"Expected a singleton value for property '%s' on class '%s', but no value was parsed.",
					getPropertyBinding().getPropertyInfo().getSimpleName(), getClassBinding().getClazz().getSimpleName()));
		} else if (values.size() != 1) {
			throw new BindingException(
					String.format("Expected a singleton value for property '%s' on class '%s', but parsed %d values.",
							getPropertyBinding().getPropertyInfo().getSimpleName(),  getClassBinding().getClazz().getSimpleName(),
							values.size()));
		} else {
			value = values.get(0);
		}
		return false;
	}

	@Override
	public Object getObject() {
		return value;
	}

}
