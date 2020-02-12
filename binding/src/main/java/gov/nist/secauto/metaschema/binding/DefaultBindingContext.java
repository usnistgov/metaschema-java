package gov.nist.secauto.metaschema.binding;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.datatypes.adapter.DataTypes;
import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

class DefaultBindingContext implements BindingContext {
//	private static final Logger logger = LogManager.getLogger(DefaultBindingContext.class);

	private final Map<Class<?>, ClassBinding<?>> classBindingsByClass = new HashMap<>();
//	private final Map<Class<?>, XmlParsePlan<?>> xmlParsePlansByClass = new HashMap<>();
//	private final Map<Class<?>, XmlWriter> xmlWriterByClass = new HashMap<>();
//	private final Map<Class<?>, JsonWriter> jsonWriterByClass = new HashMap<>();
	private final Map<Type, JavaTypeAdapter<?>> xmlJavaTypeAdapters = new HashMap<>();

	public DefaultBindingContext() {
		// register all known types
		for (DataTypes dts : DataTypes.values()) {
			JavaTypeAdapter<?> adapter = dts.getJavaTypeAdapter();
			if (adapter != null) {
				xmlJavaTypeAdapters.put(dts.getJavaClass(), adapter);
			}
		}
	}

	public <CLASS> JavaTypeAdapter<?> registerJavaTypeAdapter(Class<CLASS> clazz, JavaTypeAdapter<CLASS> adapter) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(adapter, "adapter");

		synchronized (adapter) {
			return xmlJavaTypeAdapters.put(clazz, adapter);
		}
	}

	protected <CLASS> ClassBinding<CLASS> getBoundClassBinding(Class<CLASS> clazz) throws BindingException {
		ClassBinding<CLASS> classBinding = getClassBinding(clazz);
		if (classBinding == null) {
			throw new BindingException(String.format("Class '%s' is not a bound Java class.", clazz.getName()));
		}
		return classBinding;
	}


	@Override
	public boolean hasClassBinding(Class<?> clazz) throws BindingException {
		return getClassBinding(clazz) != null;
	}

	@Override
	public <CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			ClassBinding<CLASS> retval = (ClassBinding<CLASS>) classBindingsByClass.get(clazz);
			if (retval == null) {
				retval = ClassBinding.newClassBinding(clazz);

				if (retval != null) {
					classBindingsByClass.put(clazz, retval);
				}
			}
			return retval;
		}
	}

	@Override
	public <CLASS> JavaTypeAdapter<CLASS> getJavaTypeAdapter(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			// First try to find a simple data binding
			@SuppressWarnings("unchecked")
			JavaTypeAdapter<CLASS> retval = (JavaTypeAdapter<CLASS>) xmlJavaTypeAdapters.get(clazz);
			if (retval == null) {
				// no simple binding exists, try to bind to the object

				// TODO: handle binding exception, which may be caused if the class cannot be
				// bound for any reason
				ClassBinding<CLASS> classBinding = getClassBinding(clazz);
				retval = new ObjectJavaTypeAdapter<CLASS>(classBinding);
				xmlJavaTypeAdapters.put(clazz, retval);
			}
			return retval;
		}
	}

	@Override
	public <CLASS> Serializer<CLASS> newXmlSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new XmlSerializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> Serializer<CLASS> newJsonSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new JsonSerializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> Serializer<CLASS> newYamlSerializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new YamlSerializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> Deserializer<CLASS> newXmlDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new XmlDeserializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> Deserializer<CLASS> newJsonDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new JsonDeserializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> Deserializer<CLASS> newYamlDeserializer(Class<CLASS> clazz, Configuration configuration) throws BindingException {
		AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);
		return new YamlDeserializerImpl<CLASS>(this, classBinding, configuration);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, OutputStream out) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, null).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, File file) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, null).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, Writer writer) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, null).serialize(data, writer);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, OutputStream out, Configuration configuration)
			throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, configuration).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, File file, Configuration configuration) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, configuration).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToXml(CLASS data, Writer writer, Configuration configuration) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newXmlSerializer(clazz, configuration).serialize(data, writer);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, OutputStream out) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, null).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, File file) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, null).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, Writer writer) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, null).serialize(data, writer);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, OutputStream out, Configuration configuration)
			throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, configuration).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, File file, Configuration configuration) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, configuration).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToJson(CLASS data, Writer writer, Configuration configuration)
			throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newJsonSerializer(clazz, configuration).serialize(data, writer);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, OutputStream out) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, null).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, File file) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, null).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, Writer writer) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, null).serialize(data, writer);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, OutputStream out, Configuration configuration)
			throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, configuration).serialize(data, out);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, File file, Configuration configuration) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, configuration).serialize(data, file);
	}

	@Override
	public <CLASS> void serializeToYaml(CLASS data, Writer writer, Configuration configuration)
			throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>)data.getClass();
		newYamlSerializer(clazz, configuration).serialize(data, writer);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, InputStream out) throws BindingException {
		return newXmlDeserializer(clazz, null).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, File file) throws BindingException {
		return newXmlDeserializer(clazz, null).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, URL url) throws BindingException {
		return newXmlDeserializer(clazz, null).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, Reader reader) throws BindingException {
		return newXmlDeserializer(clazz, null).deserialize(reader);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException {
		return newXmlDeserializer(clazz, configuration).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException {
		return newXmlDeserializer(clazz, configuration).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException {
		return newXmlDeserializer(clazz, configuration).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromXml(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException {
		return newXmlDeserializer(clazz, configuration).deserialize(reader);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, InputStream out) throws BindingException {
		return newJsonDeserializer(clazz, null).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, File file) throws BindingException {
		return newJsonDeserializer(clazz, null).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, URL url) throws BindingException {
		return newJsonDeserializer(clazz, null).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, Reader reader) throws BindingException {
		return newJsonDeserializer(clazz, null).deserialize(reader);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException {
		return newJsonDeserializer(clazz, configuration).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException {
		return newJsonDeserializer(clazz, configuration).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException {
		return newJsonDeserializer(clazz, configuration).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromJson(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException {
		return newJsonDeserializer(clazz, configuration).deserialize(reader);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, InputStream out) throws BindingException {
		return newYamlDeserializer(clazz, null).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, File file) throws BindingException {
		return newYamlDeserializer(clazz, null).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, URL url) throws BindingException {
		return newYamlDeserializer(clazz, null).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, Reader reader) throws BindingException {
		return newYamlDeserializer(clazz, null).deserialize(reader);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, InputStream out, Configuration configuration) throws BindingException {
		return newYamlDeserializer(clazz, configuration).deserialize(out);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, File file, Configuration configuration) throws BindingException {
		return newYamlDeserializer(clazz, configuration).deserialize(file);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, URL url, Configuration configuration) throws BindingException {
		return newYamlDeserializer(clazz, configuration).deserialize(url);
	}

	@Override
	public <CLASS> CLASS deserializeFromYaml(Class<CLASS> clazz, Reader reader, Configuration configuration) throws BindingException {
		return newYamlDeserializer(clazz, configuration).deserialize(reader);
	}

}
