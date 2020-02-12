package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class CollapsibleFieldPropertyItemHandler extends AbstractBoundClassPropertyItemHandler<FieldClassBinding<?>, FieldPropertyBinding>
		implements PropertyItemHandler {

	public CollapsibleFieldPropertyItemHandler(FieldClassBinding<?> classBinding, FieldPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public List<Object> parse(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter) throws IOException {
		// TODO: implement
		JsonParser parser = parsingContext.getEventReader();
		JsonUtil.skipValue(parser);
		// skip over the VALUE or END_OBJECT
		parser.nextToken();
		
		return Collections.emptyList();
	}

	@Override
	public void writeValue(Object value, JsonWritingContext writingContext, NamedPropertyBindingFilter filter)
			throws BindingException, IOException {
		throw new UnsupportedOperationException();
	}

}
