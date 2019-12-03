package gov.nist.secauto.metaschema.codegen.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.SimpleJavaType;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class FieldInstanceItemContext extends AbstractInstanceItemContext<FieldInstance> implements InstanceItemContext {
	private final SimpleJavaType javaType;

	public FieldInstanceItemContext(FieldInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = new SimpleJavaType(instance.getDefinition());
	}

	@Override
	public SimpleJavaType getJavaType() {
		return javaType;
	}

}
