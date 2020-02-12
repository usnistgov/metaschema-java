package gov.nist.secauto.metaschema.binding.io.xml.parser;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

public class DefaultXmlProblemHandler implements XmlProblemHandler {
	private static final QName XSI_SCHEMA_LOCATION = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
	private static final Set<QName> ignoredQNames;
	
	static {
		ignoredQNames = new HashSet<>();
		ignoredQNames.add(XSI_SCHEMA_LOCATION);
	}

	@Override
	public boolean handleUnknownAttribute(Object obj, QName attributeName, XmlParsingContext parsingContext) {
		if (ignoredQNames.contains(attributeName)) {
			return true;
		}
		return false;
	}

}
