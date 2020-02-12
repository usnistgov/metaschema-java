package gov.nist.secauto.metaschema.binding.io.json.old;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class DataTypeItemBinding extends AbstractItemBinding<ModelItemPropertyBinding> {

	public DataTypeItemBinding(ModelItemPropertyBinding propertyBinding) {
		super(propertyBinding);
	}

	@Override
	public void writeValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException, IOException {
		JavaTypeAdapter<?> adapter = writingContext.getBindingContext()
				.getJavaTypeAdapter(getPropertyBinding().getPropertyInfo().getItemType());

		adapter.writeJsonFieldValue(value, filter, writingContext);
	}
}
