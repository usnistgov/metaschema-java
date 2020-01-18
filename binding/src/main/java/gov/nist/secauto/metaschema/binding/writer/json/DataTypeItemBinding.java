package gov.nist.secauto.metaschema.binding.writer.json;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public class DataTypeItemBinding extends AbstractItemBinding<ModelItemPropertyBinding> {

	public DataTypeItemBinding(ModelItemPropertyBinding propertyBinding) {
		super(propertyBinding);
	}

	@Override
	public void writeValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException, IOException {
		JavaTypeAdapter<?> adapter = writingContext.getBindingContext()
				.getJavaTypeAdapter(getPropertyBinding().getPropertyInfo().getItemType());

		adapter.writeJsonFieldValue(value, filter, writingContext);
	}
}
