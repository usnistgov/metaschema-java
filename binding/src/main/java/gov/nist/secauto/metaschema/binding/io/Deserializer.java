package gov.nist.secauto.metaschema.binding.io;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface Deserializer<CLASS> {
	CLASS deserialize(InputStream out) throws BindingException;
	CLASS deserialize(File file) throws BindingException;
	CLASS deserialize(URL url) throws BindingException;
	CLASS deserialize(Reader reader) throws BindingException;
}
