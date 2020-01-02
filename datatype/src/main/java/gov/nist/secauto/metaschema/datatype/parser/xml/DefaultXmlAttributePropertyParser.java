package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

import gov.nist.secauto.metaschema.datatype.binding.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DefaultXmlAttributePropertyParser extends AbstractXmlPropertyParser<FlagPropertyBinding> implements XmlAttributePropertyParser {

	public DefaultXmlAttributePropertyParser(FlagPropertyBinding xmlAttributePropertyBinding, XmlParser parser) {
		super(xmlAttributePropertyBinding, parser);
	}

	@Override
	public QName getHandledQName() {
		return getPropertyBinding().getXmlQName();
	}

	@Override
	public <CLASS> void parse(CLASS obj, Attribute attribue) throws BindingException {
		JavaTypeAdapter<?> typeAdapter = getParser().getXmlTypeAdapter((Class<?>)getPropertyBinding().getPropertyInfo().getItemType());
		try {
			Object value = typeAdapter.parseValue(attribue.getValue());
			getPropertyBinding().getPropertyInfo().getPropertyAccessor().setValue(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(ex);
		} catch (UnsupportedOperationException ex) {
			throw new BindingException(ex);
		}
	}

}
