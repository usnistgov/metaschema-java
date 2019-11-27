package gov.nist.secauto.metaschema.codegen.context;

import java.io.PrintWriter;
import java.util.Collection;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public interface InstanceContext {

	String getPropertyName();
	Collection<String> getImports();
	MarkupString getDescription();
	void writeVariable(PrintWriter writer);
	void writeGetter(PrintWriter writer);
	void writeSetter(PrintWriter writer);

}
