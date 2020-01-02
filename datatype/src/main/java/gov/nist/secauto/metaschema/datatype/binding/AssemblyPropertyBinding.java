package gov.nist.secauto.metaschema.datatype.binding;

import java.util.Objects;

import gov.nist.secauto.metaschema.datatype.annotations.Assembly;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;

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
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(XmlParser parser) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, parser);
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getAssemblyAnnotation().name(), getPropertyInfo().getPropertyAccessor());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getAssemblyAnnotation().namespace(), getPropertyInfo().getPropertyAccessor());
	}

}
