package gov.nist.secauto.metaschema.binding.parser.xml;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public interface XmlObjectPropertyParser extends XmlPropertyParser {
//	List<QName> getHandledQNames();
	@Override
	ModelItemPropertyBinding getPropertyBinding();
	<CLASS> void parse(CLASS obj, XmlParsingContext parsingContext) throws BindingException;

	boolean canConsume(QName nextQName) throws BindingException;
	boolean isChildWrappedInXml();
}
