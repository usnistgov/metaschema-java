package gov.nist.secauto.metaschema.datatype.binding.property;

import java.util.Objects;

import gov.nist.secauto.metaschema.datatype.annotations.Assembly;
import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultXmlObjectPropertyParser;

public class AssemblyPropertyBinding extends AbstractModelItemPropertyBinding {

	public static AssemblyPropertyBinding fromJavaField(java.lang.reflect.Field field, Assembly assemblyAnnotation) throws BindingException {
		CollectionPropertyInfo propertyInfo = CollectionPropertyInfo.newCollectionPropertyInfo(field);

		return new AssemblyPropertyBinding(propertyInfo, assemblyAnnotation);
	}
	
	private final Assembly assemblyAnnotation;

	public AssemblyPropertyBinding(CollectionPropertyInfo propertyInfo, Assembly assemblyAnnotation) {
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
		return ModelUtil.resolveLocalName(getAssemblyAnnotation().name(), getPropertyInfo().getPropertyAccessor().getSimpleName());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getAssemblyAnnotation().namespace(), getPropertyInfo().getPropertyAccessor().getContainingClass());
	}
}
