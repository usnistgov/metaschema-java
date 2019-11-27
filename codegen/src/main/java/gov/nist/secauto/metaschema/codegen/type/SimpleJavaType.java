package gov.nist.secauto.metaschema.codegen.type;

import gov.nist.secauto.metaschema.codegen.ClassUtils;
import gov.nist.secauto.metaschema.model.ManagedObject;

public class SimpleJavaType extends AbstractJavaType {

	public SimpleJavaType(Class<?> clazz) {
		super(clazz);
	}

	public SimpleJavaType(String packageName, String className) {
		super(packageName, className);
	}

	public SimpleJavaType(ManagedObject definition) {
		this(ClassUtils.toPackageName(definition), ClassUtils.toClassName(definition));
	}

}
