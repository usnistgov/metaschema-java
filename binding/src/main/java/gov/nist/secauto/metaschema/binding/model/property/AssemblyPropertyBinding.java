package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;

/**
 * Represents a Java property bound to a Metaschema assembly.
 * 
 * @author davidwal
 *
 */
public interface AssemblyPropertyBinding extends ModelItemPropertyBinding {

	public static AssemblyPropertyBinding fromJavaField(java.lang.reflect.Field field, Assembly assemblyAnnotation)
			throws BindingException {
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);

		return new DefaultAssemblyPropertyBinding(propertyInfo, assemblyAnnotation);
	}
}
