package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public abstract class AbstractJsonReader<CLASS, CLASS_BINDING extends ClassBinding<CLASS>, OBJECT_PARSER extends BoundObjectParser<CLASS, CLASS_BINDING>>
		implements JsonReader<CLASS> {
	private final CLASS_BINDING classBinding;

	public AbstractJsonReader(CLASS_BINDING classBinding) {
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	protected abstract OBJECT_PARSER newObjectParser(PropertyBindingFilter filter,
			JsonParsingContext parsingContext) throws BindingException;

	@Override
	public List<CLASS> readJson(JsonParsingContext parsingContext, PropertyBindingFilter filter, boolean parseRoot)
			throws BindingException {

		OBJECT_PARSER parser = newObjectParser(filter, parsingContext);

		return readJsonInternal(parser, parseRoot);
	}

	protected List<CLASS> readJsonInternal(OBJECT_PARSER parser,
			@SuppressWarnings("unused") boolean parseRoot) throws BindingException {
		return parser.parseObjects();
	}
}
