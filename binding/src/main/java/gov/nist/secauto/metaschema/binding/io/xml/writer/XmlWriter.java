package gov.nist.secauto.metaschema.binding.io.xml.writer;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface XmlWriter {
	void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException;
}
