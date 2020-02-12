package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

public interface XmlObjectPropertyParser extends XmlPropertyParser {
//	List<QName> getHandledQNames();
	@Override
	ModelItemPropertyBinding getPropertyBinding();
	<CLASS> void parse(CLASS obj, XmlParsingContext parsingContext) throws BindingException;

	boolean canConsume(QName nextQName) throws BindingException;
	boolean isChildWrappedInXml();
}
