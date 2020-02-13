package gov.nist.secauto.metaschema.codegen.item;

import java.net.URI;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.datatype.markup.MarkupString;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

public abstract class AbstractInstanceItemContext<INSTANCE extends ModelInstance> implements InstanceItemContext {
	private final AssemblyClassGenerator containingClassGenerator;
	private final INSTANCE modelInstance;

	public AbstractInstanceItemContext(INSTANCE instance, AssemblyClassGenerator classContext) {
		this.modelInstance = instance;
		this.containingClassGenerator = classContext;
	}

	@Override
	public INSTANCE getModelInstance() {
		return modelInstance;
	}

	protected AssemblyClassGenerator getContainingClassGenerator() {
		return containingClassGenerator;
	}

	@Override
	public URI getXmlNamespace() {
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
	
}
