package gov.nist.secauto.metaschema.binding.writer.xml;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface XmlWriter {
	void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException;
}
