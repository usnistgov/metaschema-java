package gov.nist.secauto.metaschema.binding.writer.json;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.AssemblyPropertyBinding;

public class AssemblyItemBinding extends AbstractBoundClassItemBinding<AssemblyClassBinding<?>, AssemblyPropertyBinding> {

	public AssemblyItemBinding(AssemblyClassBinding<?> classBinding, AssemblyPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public void writeValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException, IOException {
		AssemblyJsonWriter<?> jsonWriter = getClassBinding().getAssemblyJsonWriter(writingContext.getBindingContext());
		jsonWriter.writeJson(value, filter, writingContext);
	}

}
