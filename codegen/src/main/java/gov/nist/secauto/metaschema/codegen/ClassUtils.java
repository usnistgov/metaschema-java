package gov.nist.secauto.metaschema.codegen;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.model.InfoElement;

public class ClassUtils {

//	public static String getInstanceJavaType(AbstractInstanceGenerator instance, AbstractClassGenerator clazz) {
//		instance.
//	}

	public static String toPackageName(InfoElement infoElement) {
		return NameConverter.standard.toPackageName(infoElement.getContainingMetaschema().getXmlNamespace().toString());
	}

	public static String toClassName(InfoElement infoElement) {
		return NameConverter.standard.toClassName(infoElement.getName());
	}

	public static String toPropertyName(String name) {
		return NameConverter.standard.toPropertyName(name);
	}

	public static String toVariableName(String name) {
		return NameConverter.standard.toVariableName(name);
	}

}
