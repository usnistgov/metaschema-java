package gov.nist.secauto.metaschema.model.info.instances;

import com.sun.xml.bind.api.impl.NameConverter;

public class ClassUtils {

//	public static String getInstanceJavaType(AbstractInstanceGenerator instance, AbstractClassGenerator clazz) {
//		instance.
//	}

//	public static String toClassName(InfoElement infoElement) {
//		return NameConverter.standard.toClassName(infoElement.getName());
//	}

	public static String toPropertyName(String name) {
		return NameConverter.standard.toPropertyName(name);
	}

	public static String toVariableName(String name) {
		return NameConverter.standard.toVariableName(name);
	}

}
