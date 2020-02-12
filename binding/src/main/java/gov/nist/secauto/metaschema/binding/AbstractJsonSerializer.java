package gov.nist.secauto.metaschema.binding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.json.writer.DefaultJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;

abstract class AbstractJsonSerializer<CLASS> extends AbstractSerializer<CLASS> {
	private JsonFactory jsonFactory;

	public AbstractJsonSerializer(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding, Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	protected abstract JsonFactory getJsonFactoryInstance();

	protected JsonFactory getJsonFactory() {
		synchronized (this) {
			if (jsonFactory == null) {
				jsonFactory = getJsonFactoryInstance();
			}
			return jsonFactory;
		}
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		synchronized (this) {
			this.jsonFactory = jsonFactory;
		}
	}

	protected JsonParser newJsonParser(Reader reader) throws BindingException {
		try {
			JsonFactory factory = getJsonFactory();
			JsonParser retval = factory.createParser(reader);
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	protected JsonGenerator newJsonGenerator(Writer writer) throws BindingException {
		try {
			JsonFactory factory = getJsonFactory();
			JsonGenerator retval = factory.createGenerator(writer);
			retval.setPrettyPrinter(new DefaultPrettyPrinter());
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public void serialize(CLASS data, Writer writer) throws BindingException {
		JsonGenerator generator = newJsonGenerator(writer);
		try {
			generator.writeStartObject();
			writeJsonInternal(data, generator);
			generator.writeEndObject();
			generator.close();
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	protected void writeJsonInternal(CLASS obj, JsonGenerator generator) throws BindingException {
		BindingContext bindingContext = getBindingContext();
		AssemblyClassBinding<CLASS> classBinding = getClassBinding();
		AssemblyJsonWriter<CLASS> writer = classBinding.getAssemblyJsonWriter(bindingContext);
		JsonWritingContext writingContext = new DefaultJsonWritingContext(generator, bindingContext);

		if (classBinding.hasRootWrapper()) {
			// write root property
			String name = classBinding.getRootWrapper().name();
			try {
				writingContext.getEventWriter().writeFieldName(name);
			} catch (IOException ex) {
				throw new BindingException(ex);
			}
		} else {
			throw new UnsupportedOperationException(String.format(
					"The target assembly class '%s' is not a root. Please use an assembly class with the '%s' annotation.",
					classBinding.getClazz().getName(), RootWrapper.class.getName()));
		}

		writer.writeJson(obj, null, writingContext);
	}

}
