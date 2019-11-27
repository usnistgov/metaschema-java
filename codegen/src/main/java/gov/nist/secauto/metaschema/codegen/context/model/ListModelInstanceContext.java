package gov.nist.secauto.metaschema.codegen.context.model;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.ListJavaType;

public class ListModelInstanceContext extends AbstractModelInstanceContext<ListJavaType> implements ModelInstanceContext {
	public ListModelInstanceContext(ModelItemInstanceContext itemInstanceContext, AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator, new ListJavaType(itemInstanceContext.getJavaType()));
	}
}
