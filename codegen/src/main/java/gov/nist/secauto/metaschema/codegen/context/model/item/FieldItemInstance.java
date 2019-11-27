package gov.nist.secauto.metaschema.codegen.context.model.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.context.model.ModelItemInstanceContext;
import gov.nist.secauto.metaschema.codegen.type.SimpleJavaType;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class FieldItemInstance extends AbstractModelItemInstanceContext<FieldInstance> implements ModelItemInstanceContext {
	private final SimpleJavaType javaType;

	public FieldItemInstance(FieldInstance instance, AssemblyClassGenerator classContext) {
		super(instance, classContext);
		this.javaType = new SimpleJavaType(instance.getDefinition());
	}

	@Override
	public SimpleJavaType getJavaType() {
		return javaType;
	}

}
