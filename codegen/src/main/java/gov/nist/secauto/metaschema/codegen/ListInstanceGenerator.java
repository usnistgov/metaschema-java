package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.ListJavaType;

public class ListInstanceGenerator extends AbstractCardinalityInstanceGenerator<ListJavaType> implements CardinalityInstanceGenerator {
	public ListInstanceGenerator(InstanceItemContext itemInstanceContext, AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator, new ListJavaType(itemInstanceContext.getJavaType()));
	}
}
