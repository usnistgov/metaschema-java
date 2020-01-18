package gov.nist.secauto.metaschema.codegen.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;

public class FieldInstanceItemContext extends AbstractInstanceItemContext<FieldInstance> implements InstanceItemContext {
	private final JavaType javaType;

	public FieldInstanceItemContext(FieldInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = JavaType.create(instance.getDefinition());
	}

	@Override
	public JavaType getJavaType() {
		return javaType;
	}

}
