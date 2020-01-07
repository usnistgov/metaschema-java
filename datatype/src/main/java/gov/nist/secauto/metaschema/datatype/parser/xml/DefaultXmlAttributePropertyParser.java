package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DefaultXmlAttributePropertyParser extends AbstractXmlPropertyParser<FlagPropertyBinding> implements XmlAttributePropertyParser {

	public DefaultXmlAttributePropertyParser(FlagPropertyBinding xmlAttributePropertyBinding, BindingContext bindingContext) {
		super(xmlAttributePropertyBinding, bindingContext);
	}

	@Override
	public QName getHandledQName() {
		return getPropertyBinding().getXmlQName();
	}

	@Override
	public <CLASS> void parse(CLASS obj, XmlParsingContext parser, Attribute attribue) throws BindingException {
		JavaTypeAdapter<?> typeAdapter = getBindingContext().getJavaTypeAdapter((Class<?>)getPropertyBinding().getPropertyInfo().getItemType());
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
