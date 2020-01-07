package gov.nist.secauto.metaschema.datatype.writer.xml;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface XmlWriter<CLASS> {
	void writeXml(CLASS obj, QName name, XmlWritingContext writingContext) throws BindingException;
}
