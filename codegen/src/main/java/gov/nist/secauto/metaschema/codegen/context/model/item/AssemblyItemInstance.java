package gov.nist.secauto.metaschema.codegen.context.model.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.context.model.ModelItemInstanceContext;
import gov.nist.secauto.metaschema.codegen.type.SimpleJavaType;
import gov.nist.secauto.metaschema.model.AssemblyInstance;

public class AssemblyItemInstance extends AbstractModelItemInstanceContext<AssemblyInstance> implements ModelItemInstanceContext {
	private final SimpleJavaType javaType;

	public AssemblyItemInstance(AssemblyInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = new SimpleJavaType(instance.getDefinition());
	}

	@Override
	public SimpleJavaType getJavaType() {
		return javaType;
	}

}
