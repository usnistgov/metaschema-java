package gov.nist.secauto.metaschema.binding.io;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface Serializer<CLASS> {
	void serialize(CLASS data, OutputStream os) throws BindingException;
	void serialize(CLASS data, File file) throws BindingException;
	void serialize(CLASS data, Writer writer) throws BindingException;
}
