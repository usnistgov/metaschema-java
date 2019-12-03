package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;
import java.util.Collection;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public interface InstanceGenerator {

	String getPropertyName();
	Collection<String> getImports();
	MarkupString getDescription();
	void writeVariable(PrintWriter writer);
	void writeGetter(PrintWriter writer);
	void writeSetter(PrintWriter writer);

}
