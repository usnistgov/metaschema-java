package gov.nist.secauto.metaschema.binding.io.json.old;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class AssemblyItemBinding extends AbstractBoundClassItemBinding<AssemblyClassBinding<?>, AssemblyPropertyBinding> {

	public AssemblyItemBinding(AssemblyClassBinding<?> classBinding, AssemblyPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public void writeValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException, IOException {
		
		AssemblyJsonWriter<?> jsonWriter = getClassBinding().getAssemblyJsonWriter(writingContext.getBindingContext());
		jsonWriter.writeJson(value, filter, writingContext);
	}

}
