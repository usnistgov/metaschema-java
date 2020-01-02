package gov.nist.secauto.metaschema.datatype.parser.xml;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.binding.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DefaultFieldValuePropertyParser extends AbstractXmlPropertyParser<FieldValuePropertyBinding> implements FieldValueXmlPropertyParser {

	public DefaultFieldValuePropertyParser(FieldValuePropertyBinding propertyBinding, XmlParser parser) {
		super(propertyBinding, parser);
	}

	@Override
	public <CLASS> void parse(CLASS obj, XMLEventReader2 reader) throws BindingException {
		JavaTypeAdapter<?> typeAdapter = getParser().getXmlTypeAdapter((Class<?>)getPropertyBinding().getPropertyInfo().getItemType());
		try {
//			XMLEvent event = reader.peek();
			Object value = typeAdapter.parseType(reader);
				// TODO: handle end element
			getPropertyBinding().getPropertyInfo().getPropertyAccessor().setValue(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(ex);
		}
	}

}
