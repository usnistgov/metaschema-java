package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

public class DataTypePropertyItemHandler extends AbstractProperrtyItemHandler<PropertyBinding> {

	public DataTypePropertyItemHandler(PropertyBinding propertyBinding) {
		super(propertyBinding);
	}

	@Override
	public List<Object> parse(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter) throws BindingException {
		JavaTypeAdapter<?> adapter = parsingContext.getBindingContext()
				.getJavaTypeAdapter(getPropertyBinding().getPropertyInfo().getItemType());

		return Collections.singletonList(adapter.parse(parsingContext));
	}

	@Override
	public void writeValue(Object value, JsonWritingContext writingContext, NamedPropertyBindingFilter filter)
			throws BindingException, IOException {
		JavaTypeAdapter<?> adapter = writingContext.getBindingContext()
				.getJavaTypeAdapter(getPropertyBinding().getPropertyInfo().getItemType());

		adapter.writeJsonFieldValue(value, filter, writingContext);
	}

}
