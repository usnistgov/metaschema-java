package gov.nist.secauto.metaschema.datatype.binding;

import java.io.Reader;
import java.io.Writer;

import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;

public interface BindingContext {
	
	public static BindingContext newInstance() {
		return new DefaultBindingContext();
	}

	<CLASS> CLASS parseXml(Reader reader, Class<CLASS> clazz) throws BindingException;
	<CLASS> void writeXml(Writer writer, CLASS object) throws BindingException;
	<CLASS> JavaTypeAdapter<CLASS> getJavaTypeAdapter(Class<CLASS> itemType) throws BindingException;
	<CLASS> XmlParsePlan<CLASS> getXmlParsePlan(Class<CLASS> clazz) throws BindingException;
}
