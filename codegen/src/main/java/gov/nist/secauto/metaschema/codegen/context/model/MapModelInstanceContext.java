package gov.nist.secauto.metaschema.codegen.context.model;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.MapJavaType;

public class MapModelInstanceContext extends AbstractModelInstanceContext<MapJavaType> implements ModelInstanceContext {

	public MapModelInstanceContext(ModelItemInstanceContext itemInstanceContext, AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator, new MapJavaType(String.class,itemInstanceContext.getJavaType()));
	}
}
