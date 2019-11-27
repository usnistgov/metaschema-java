package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

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

	public static void writeGetter(PrintWriter writer, String propertyName, String javaType, String variableName) {
		writer.printf("\tpublic %s get%s() {%n", javaType, propertyName);
		writer.printf("\t\treturn %s;%n", variableName);
		writer.printf("\t}%n");
		writer.println();
	}

	public static void writeSetter(PrintWriter writer, String propertyName, String javaType, String variableName) {
		writer.printf("\tpublic void set%s(%s value) {%n", propertyName, javaType);
		writer.printf("\t\tthis.%s = value;%n", variableName);
		writer.printf("\t}%n");
		writer.println();
	}

	public static void writeVariable(PrintWriter writer, String javaType, String variableName) {
		writer.printf("\tprivate %s %s;%n", javaType, variableName);
	}

}
