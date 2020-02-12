package gov.nist.secauto.metaschema.binding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

abstract class AbstractSerializer<CLASS> extends AbstractSerializationBase<CLASS> implements Serializer<CLASS> {

	public AbstractSerializer(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
			Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	@Override
	public void serialize(CLASS data, OutputStream out) throws BindingException {
		serialize(data, new OutputStreamWriter(out));
	}

	@Override
	public void serialize(CLASS data, File file) throws BindingException {
		try (FileWriter writer = new FileWriter(file, Charset.forName("UTF-8"))) {
			serialize(data, writer);
		} catch (IOException ex) {
			throw new BindingException("Unable to open file: " + file != null ? file.getPath() : "{null}", ex);
		}
	}

}
