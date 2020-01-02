package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.codegen.type.MapJavaType;

public class MapInstanceGenerator extends AbstractCardinalityInstanceGenerator<MapJavaType>
		implements CardinalityInstanceGenerator {

	public MapInstanceGenerator(InstanceItemContext itemInstanceContext,
			AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator,
				JavaType.createGenericMap(JavaType.create(String.class), itemInstanceContext.getJavaType()));
	}
}
