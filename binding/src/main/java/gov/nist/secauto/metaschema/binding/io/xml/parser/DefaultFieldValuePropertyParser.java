package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;

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
