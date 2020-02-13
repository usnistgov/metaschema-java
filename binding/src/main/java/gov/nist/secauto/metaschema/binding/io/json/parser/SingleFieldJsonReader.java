package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class SingleFieldJsonReader<CLASS> extends AbstractFieldJsonReader<CLASS, SingleBoundObjectParser<CLASS, FieldClassBinding<CLASS>>> {

	public SingleFieldJsonReader(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	protected SingleBoundObjectParser<CLASS, FieldClassBinding<CLASS>> newObjectParser(PropertyBindingFilter filter,
			JsonParsingContext parsingContext) throws BindingException {
		return new SingleBoundObjectParser<>(getClassBinding(), filter, parsingContext, (fieldName, props, context) -> handleUnknownProperty(fieldName, props, context));
	}

}
