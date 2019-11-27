package gov.nist.secauto.metaschema.codegen.context.model.item;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.context.AbstractInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.model.ModelItemInstanceContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.ModelInstance;

public abstract class AbstractModelItemInstanceContext<INSTANCE extends ModelInstance> extends AbstractInstanceContext<AssemblyClassGenerator> implements ModelItemInstanceContext {
	private final INSTANCE modelInstance;

	public AbstractModelItemInstanceContext(INSTANCE instance, AssemblyClassGenerator classContext) {
		super(classContext);
		this.modelInstance = instance;
	}

	protected INSTANCE getModelInstance() {
		return modelInstance;
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
