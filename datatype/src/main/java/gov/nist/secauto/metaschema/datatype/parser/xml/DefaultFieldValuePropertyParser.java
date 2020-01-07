package gov.nist.secauto.metaschema.datatype.parser.xml;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.binding.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DefaultFieldValuePropertyParser extends AbstractXmlPropertyParser<FieldValuePropertyBinding> implements FieldValueXmlPropertyParser {

	public DefaultFieldValuePropertyParser(FieldValuePropertyBinding propertyBinding, BindingContext bindingContext) {
		super(propertyBinding, bindingContext);
	}

	@Override
	public <CLASS> void parse(CLASS obj, XmlParsingContext parsingContext) throws BindingException {
		JavaTypeAdapter<?> typeAdapter = getBindingContext().getJavaTypeAdapter((Class<?>)getPropertyBinding().getPropertyInfo().getItemType());
		try {
//			XMLEvent event = reader.peek();
			Object value = typeAdapter.parseType(parsingContext);
				// TODO: handle end element
			getPropertyBinding().getPropertyInfo().getPropertyAccessor().setValue(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(ex);
		}
	}

}
