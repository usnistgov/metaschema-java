package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

public class ClassUtils {

//	public static String getInstanceJavaType(AbstractInstanceGenerator instance, AbstractClassGenerator clazz) {
//		instance.
//	}

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
