package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class AssemblyPropertyItemHandler extends AbstractBoundClassPropertyItemHandler<AssemblyClassBinding<?>, AssemblyPropertyBinding> {

	public AssemblyPropertyItemHandler(AssemblyClassBinding<?> classBinding, AssemblyPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public List<Object> parse(JsonParsingContext parsingContext, PropertyBindingFilter filter) throws BindingException {
		JsonReader<?> jsonReader = getClassBinding().getJsonReader(parsingContext.getBindingContext());
		return Collections.singletonList(jsonReader.readJson(parsingContext, filter, false));
	}

	@Override
	public void writeValue(Object value, JsonWritingContext writingContext, PropertyBindingFilter filter)
			throws BindingException, IOException {
		AssemblyJsonWriter<?> jsonWriter = getClassBinding().getAssemblyJsonWriter(writingContext.getBindingContext());
		jsonWriter.writeJson(value, filter, writingContext);
	}
}
