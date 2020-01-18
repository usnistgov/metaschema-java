package gov.nist.secauto.metaschema.codegen.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;

public class AssemblyInstanceItemContext extends AbstractInstanceItemContext<AssemblyInstance> implements InstanceItemContext {
	private final JavaType javaType;

	public AssemblyInstanceItemContext(AssemblyInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = JavaType.create(instance.getDefinition());
	}

	@Override
	public JavaType getJavaType() {
		return javaType;
	}

}
