package gov.nist.secauto.metaschema.codegen.type;

import java.util.Set;

import gov.nist.secauto.metaschema.model.ManagedObject;

public interface JavaType {
	/**
	 * Gets the simple, unqualified class name for the Java type.
	 * 
	 * @return the class name
	 */
	String getClassName();

	/**
	 * Get the package name for the Java type.
	 * 
	 * @return the package name
	 */
	String getPackageName();

	/**
	 * Gets the qualified class name.
	 * 
	 * @return the qualified class name
	 */
	String getQualifiedClassName();

	/**
	 * Get the actual Java type. This will qualify the Java type if it has the same
	 * name as the containing class, or will add an import otherwise.
	 * 
	 * @param classType the Java type of the containing class
	 * @return the type name
	 */
	String getType(JavaType classType);

	/**
	 * Get the set of imports needed for this Java type.
	 * @param classType the Java type of the containing class
	 * @return the set of Java types to be imported
	 */
	Set<JavaType> getImports(JavaType classType);

	/**
	 * Get the import value for this Java type.
	 * @param classType the Java type of the containing class
	 * @return the import value or {@code null} if no import is needed
	 */
	String getImportValue(JavaType classJavaType);

	int hashCode();

	boolean equals(Object obj);

	public static JavaType create(Class<?> clazz) {
		return new ClassJavaType(clazz);
	}
	public static JavaType create(String packageName, String className) {
		return new SimpleJavaType(packageName, className);
	}

	public static JavaType create(ManagedObject obj) {
		return new SimpleJavaType(obj);
	}

	public static ListJavaType createGenericList(Class<?> valueClass) {
		return new ListJavaType(create(valueClass));
	}

	public static ListJavaType createGenericList(JavaType valueType) {
		return new ListJavaType(valueType);
	}

	public static MapJavaType createGenericMap(Class<?> keyClass, Class<?> valueClass) {
		return new MapJavaType(create(keyClass), create(valueClass));
	}

	public static MapJavaType createGenericMap(JavaType keyClass, JavaType valueClass) {
		return new MapJavaType(keyClass, valueClass);
	}
}
