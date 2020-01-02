package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class SingletonInstanceGenerator extends AbstractCardinalityInstanceGenerator<JavaType>
		implements CardinalityInstanceGenerator {

	public SingletonInstanceGenerator(InstanceItemContext instanceItemContext,
			AssemblyClassGenerator assemblyClassGenerator) {
		super(instanceItemContext, assemblyClassGenerator, instanceItemContext.getJavaType());
	}

}
