package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.datatype.markup.MarkupString;

public interface InstanceGenerator {

	String getPropertyName();
	MarkupString getDescription();
	void buildInstance(ClassBuilder builder);

}
