package gov.nist.secauto.metaschema.binding;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public interface BindingContext {
	
	public static BindingContext newInstance() {
		return new DefaultBindingContext();
	}

	boolean hasClassBinding(Class<?> clazz) throws BindingException;
	<CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException;
	<CLASS> JavaTypeAdapter<CLASS> getJavaTypeAdapter(Class<CLASS> itemType) throws BindingException;
	<CLASS> Serializer<CLASS> newXmlSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;
	<CLASS> Serializer<CLASS> newJsonSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;
	<CLASS> Serializer<CLASS> newYamlSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;
	<CLASS> Deserializer<CLASS> newXmlDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;
	<CLASS> Deserializer<CLASS> newJsonDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;
	<CLASS> Deserializer<CLASS> newYamlDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException;

	
	<CLASS> void serializeToXml(CLASS data, OutputStream out) throws BindingException;
	<CLASS> void serializeToXml(CLASS data, File file) throws BindingException;
	<CLASS> void serializeToXml(CLASS data, Writer writer) throws BindingException;
	<CLASS> void serializeToXml(CLASS data, OutputStream out, Configuration configuration) throws BindingException;
	<CLASS> void serializeToXml(CLASS data, File file, Configuration configuration) throws BindingException;
	<CLASS> void serializeToXml(CLASS data, Writer writer, Configuration configuration) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, OutputStream out) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, File file) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, Writer writer) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, OutputStream out, Configuration configuration) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, File file, Configuration configuration) throws BindingException;
	<CLASS> void serializeToJson(CLASS data, Writer writer, Configuration configuration) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, OutputStream out) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, File file) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, Writer writer) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, OutputStream out, Configuration configuration) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, File file, Configuration configuration) throws BindingException;
	<CLASS> void serializeToYaml(CLASS data, Writer writer, Configuration configuration) throws BindingException;

	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, InputStream out) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, File file) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, URL url) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, Reader reader) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, InputStream out) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, File file) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, URL url) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, Reader reader) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, InputStream out) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, File file) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, URL url) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, Reader reader) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException;
	<CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException;
}
