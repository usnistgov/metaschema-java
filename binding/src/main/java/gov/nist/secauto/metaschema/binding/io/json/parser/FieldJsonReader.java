package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class FieldJsonReader<CLASS> extends AbstractJsonReader<CLASS, FieldClassBinding<CLASS>> {

	public FieldJsonReader(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	public CLASS readJson(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter, boolean parseRoot) throws BindingException {
		CLASS retval = getClassBinding().newInstance();

		try {
			parseBody(retval, filter, parsingContext);
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		return retval;
	}

}
