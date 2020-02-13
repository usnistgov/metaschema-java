package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class CollapsedFieldJsonReader<CLASS> extends AbstractFieldJsonReader<CLASS, CollapsedFieldObjectParser<CLASS>> {

	public CollapsedFieldJsonReader(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	protected CollapsedFieldObjectParser<CLASS> newObjectParser(PropertyBindingFilter filter,
			JsonParsingContext parsingContext) throws BindingException {
		return new CollapsedFieldObjectParser<>(getClassBinding(), filter, parsingContext, (fieldName, props, context) -> handleUnknownProperty(fieldName, props, context));
	}

}
