package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;

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
			Object value = typeAdapter.parse(attribue.getValue());
			getPropertyBinding().getPropertyInfo().setValue(obj, value);
		} catch (UnsupportedOperationException ex) {
			throw new BindingException(ex);
		}
	}

}
