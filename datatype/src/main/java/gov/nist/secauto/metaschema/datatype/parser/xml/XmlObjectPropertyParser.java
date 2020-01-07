package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.binding.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface XmlObjectPropertyParser extends XmlPropertyParser {
//	List<QName> getHandledQNames();
	@Override
	ModelItemPropertyBinding getPropertyBinding();
	<CLASS> void parse(CLASS obj, XmlParsingContext parsingContext) throws BindingException;

	boolean canConsume(QName nextQName) throws BindingException;
	boolean isChildWrappedInXml();
}
