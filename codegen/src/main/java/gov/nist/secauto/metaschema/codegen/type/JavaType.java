package gov.nist.secauto.metaschema.codegen.type;

import java.util.Set;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;

public interface JavaType {
	String getType(AbstractClassGenerator<?> classContext);
	Set<String> getImports(AbstractClassGenerator<?> classContext);
}
