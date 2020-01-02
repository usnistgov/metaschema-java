package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.io.Reader;

import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.Parser;

public interface XmlParser extends Parser {
	<CLASS> CLASS parse(Reader reader, Class<CLASS> clazz) throws BindingException;
	XmlProblemHandler getProblemHandler();
	<CLASS> JavaTypeAdapter<CLASS> getXmlTypeAdapter(Class<CLASS> itemClass) throws BindingException;
}
