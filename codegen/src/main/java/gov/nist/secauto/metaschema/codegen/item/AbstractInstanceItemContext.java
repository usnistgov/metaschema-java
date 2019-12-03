package gov.nist.secauto.metaschema.codegen.item;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.ModelInstance;

public abstract class AbstractInstanceItemContext<INSTANCE extends ModelInstance> implements InstanceItemContext {
	private final AssemblyClassGenerator containingClassGenerator;
	private final INSTANCE modelInstance;

	public AbstractInstanceItemContext(INSTANCE instance, AssemblyClassGenerator classContext) {
		this.modelInstance = instance;
		this.containingClassGenerator = classContext;
	}

	protected INSTANCE getModelInstance() {
		return modelInstance;
	}

	protected AssemblyClassGenerator getContainingClassGenerator() {
		return containingClassGenerator;
	}

	@Override
	public Object getXmlNamespace() {
		return getModelInstance().getContainingMetaschema().getXmlNamespace();
	}

	@Override
	public MarkupString getDescription() {
		return getModelInstance().getDescription();
	}

	@Override
	public String getInstanceName() {
		return getModelInstance().getInstanceName();
	}

	@Override
	public void writeVariableAnnotations(PrintWriter writer) {
	}

	@Override
	public Class<?> getSerializerClass() {
		return null;
	}

	@Override
	public Class<?> getDeserializerClass() {
		return null;
	}
	
}
