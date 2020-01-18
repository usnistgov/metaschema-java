package gov.nist.secauto.metaschema.binding.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultXmlObjectPropertyParser;

public class AssemblyPropertyBinding extends AbstractModelItemPropertyBinding {

	public static AssemblyPropertyBinding fromJavaField(java.lang.reflect.Field field, Assembly assemblyAnnotation) throws BindingException {
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);

		return new AssemblyPropertyBinding(propertyInfo, assemblyAnnotation);
	}
	
	private final Assembly assemblyAnnotation;

	public AssemblyPropertyBinding(PropertyInfo propertyInfo, Assembly assemblyAnnotation) {
		super(propertyInfo);
		Objects.requireNonNull(assemblyAnnotation,"assemblyAnnotation");
		this.assemblyAnnotation = assemblyAnnotation;
	}

	protected Assembly getAssemblyAnnotation() {
		return assemblyAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, bindingContext);
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getAssemblyAnnotation().name(), getPropertyInfo().getSimpleName());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getAssemblyAnnotation().namespace(), getPropertyInfo().getContainingClass());
	}
}
