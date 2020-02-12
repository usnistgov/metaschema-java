package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;

public class DefaultAssemblyPropertyBinding extends AbstractModelItemPropertyBinding implements AssemblyPropertyBinding {

	private final Assembly assemblyAnnotation;

	public DefaultAssemblyPropertyBinding(PropertyInfo propertyInfo, Assembly assemblyAnnotation) {
		super(propertyInfo, assemblyAnnotation.name(), assemblyAnnotation.namespace());
		this.assemblyAnnotation = assemblyAnnotation;
	}

	@Override
	public PropertyBindingType getPropertyBindingType() {
		return PropertyBindingType.ASSEMBLY;
	}

	protected Assembly getAssemblyAnnotation() {
		return assemblyAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, bindingContext);
	}

	@Override
	public int getMinimumOccurance() {
		PropertyInfo propertyInfo = getPropertyInfo();

		int retval;
		if (propertyInfo instanceof CollectionPropertyInfo) {
			retval = ((CollectionPropertyInfo)propertyInfo).getMinimumOccurance();
		} else {
			retval = getAssemblyAnnotation().required() ? 1 : 0;
		}
		return retval;
	}

}
