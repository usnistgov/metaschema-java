package gov.nist.secauto.metaschema.binding.parser.xml;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FieldValuePropertyBinding;

public class DefaultFieldValuePropertyParser extends AbstractXmlPropertyParser<FieldValuePropertyBinding>
		implements FieldValueXmlPropertyParser {

	public DefaultFieldValuePropertyParser(FieldValuePropertyBinding propertyBinding, BindingContext bindingContext) {
		super(propertyBinding, bindingContext);
	}

	@Override
	public void parse(Object obj, XmlParsingContext parsingContext) throws BindingException {

		JavaTypeAdapter<?> typeAdapter = getBindingContext()
				.getJavaTypeAdapter(getPropertyBinding().getPropertyInfo().getItemType());
//			XMLEvent event = reader.peek();
		Object value = typeAdapter.parse(parsingContext);
		// TODO: handle end element
		getPropertyBinding().getPropertyInfo().setValue(obj, value);
	}

}
