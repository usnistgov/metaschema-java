package gov.nist.secauto.metaschema.binding;

import java.io.Reader;
import java.io.Writer;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface BindingContext {
	
	public static BindingContext newInstance() {
		return new DefaultBindingContext();
	}

	boolean hasClassBinding(Class<?> clazz) throws BindingException;
	<CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException;
	<CLASS> CLASS parseXml(Reader reader, Class<CLASS> clazz) throws BindingException;
	void writeXml(Writer writer, Object data) throws BindingException;
	void writeJson(Writer writer, Object data) throws BindingException;
	void writeYaml(Writer writer, Object data) throws BindingException;
	<CLASS> JavaTypeAdapter<CLASS> getJavaTypeAdapter(Class<CLASS> itemType) throws BindingException;
//	<CLASS> XmlParsePlan<CLASS> getXmlParsePlan(Class<CLASS> clazz) throws BindingException;
//	XmlWriter getXmlWriter(Class<?> clazz) throws BindingException;
//	JsonWriter getJsonWriter(Class<?> clazz) throws BindingException;
}
