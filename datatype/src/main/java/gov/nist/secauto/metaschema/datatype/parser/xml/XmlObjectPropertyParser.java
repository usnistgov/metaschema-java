package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.binding.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface XmlObjectPropertyParser extends XmlPropertyParser {
//	List<QName> getHandledQNames();
	@Override
	ModelItemPropertyBinding getPropertyBinding();
	<CLASS> void parse(CLASS obj, XMLEventReader2 reader) throws BindingException;

	boolean canConsume(QName nextQName) throws BindingException;
	boolean isChildWrappedInXml();
}
