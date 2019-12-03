package gov.nist.secauto.metaschema.codegen.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.SimpleJavaType;
import gov.nist.secauto.metaschema.model.AssemblyInstance;

public class AssemblyInstanceItemContext extends AbstractInstanceItemContext<AssemblyInstance> implements InstanceItemContext {
	private final SimpleJavaType javaType;

	public AssemblyInstanceItemContext(AssemblyInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = new SimpleJavaType(instance.getDefinition());
	}

	@Override
	public SimpleJavaType getJavaType() {
		return javaType;
	}

}
