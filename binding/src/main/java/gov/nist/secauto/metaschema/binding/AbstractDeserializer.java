package gov.nist.secauto.metaschema.binding;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

abstract class AbstractDeserializer<CLASS> extends AbstractSerializationBase<CLASS> implements Deserializer<CLASS> {

	public AbstractDeserializer(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
			Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	@Override
	public CLASS deserialize(InputStream in) throws BindingException {
		return deserialize(new InputStreamReader(in));
	}

	@Override
	public CLASS deserialize(File file) throws BindingException {
		try (FileReader reader = new FileReader(file, Charset.forName("UTF-8"))) {
			return deserialize(reader);
		} catch (IOException ex) {
			throw new BindingException("Unable to open file: " + file.getPath(), ex);
		}
	}

	@Override
	public CLASS deserialize(URL url) throws BindingException {
		try (InputStream in = url.openStream()) {
			return deserialize(in);
		} catch (IOException ex) {
			throw new BindingException("Unable to open url: " + url.toString(), ex);
		}
	}
}
