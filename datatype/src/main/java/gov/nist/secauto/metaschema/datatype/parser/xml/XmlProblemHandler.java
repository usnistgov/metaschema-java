package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.codehaus.stax2.XMLEventReader2;

public class XmlProblemHandler {
	private static final QName XSI_SCHEMA_LOCATION = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
	private static final Set<QName> ignoredQNames;
	
	static {
		ignoredQNames = new HashSet<>();
		ignoredQNames.add(XSI_SCHEMA_LOCATION);
	}

	


	public boolean handleUnknownAttribute(QName attributeName, XmlParser xmlParser, XMLEventReader2 reader) {
		if (ignoredQNames.contains(attributeName)) {
			return true;
		}
		return false;
	}

}
