package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.annotations.XmlSchema;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class ModelUtil {

	public static String resolveLocalName(String provided, String contextSimpleName) {
		String retval;
		if ("##default".equals(provided)) {
			retval = contextSimpleName;
		} else {
			retval = provided;
		}
		return retval;
	}

	public static String resolveNamespace(String provided, Class<?> containingClass) throws BindingException {
		String retval;
		if ("##default".equals(provided)) {
			// get namespace from package-info
			XmlSchema xmlSchema = containingClass.getPackage().getAnnotation(XmlSchema.class);
			if (xmlSchema == null) {
				retval = "";
			} else {
				retval = xmlSchema.namespace();
			}
		} else {
			retval = provided;
		}
		return retval;
	}

}
