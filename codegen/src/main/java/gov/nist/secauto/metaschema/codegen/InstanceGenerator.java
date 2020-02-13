package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;

public interface InstanceGenerator {

	String getPropertyName();
	MarkupLine getDescription();
	void buildInstance(ClassBuilder builder);

}
