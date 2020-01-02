package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.annotations.XmlSchema;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class ModelUtil {

	static String resolveLocalName(String provided, PropertyAccessor accessor) {
		String retval;
		if ("##default".equals(provided)) {
			retval = accessor.getSimpleName();
		} else {
			retval = provided;
		}
		return retval;
	}

	static String resolveNamespace(String provided, PropertyAccessor accessor) throws BindingException {
		String retval;
		if ("##default".equals(provided)) {
			Class<?> containingClass = accessor.getContainingClass();
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
