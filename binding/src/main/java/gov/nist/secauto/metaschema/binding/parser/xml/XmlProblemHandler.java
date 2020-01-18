package gov.nist.secauto.metaschema.binding.parser.xml;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.parser.ProblemHandler;

public class XmlProblemHandler implements ProblemHandler {
	private static final QName XSI_SCHEMA_LOCATION = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
	private static final Set<QName> ignoredQNames;
	
	static {
		ignoredQNames = new HashSet<>();
		ignoredQNames.add(XSI_SCHEMA_LOCATION);
	}

	public boolean handleUnknownAttribute(QName attributeName, @SuppressWarnings("unused") XmlParsingContext parsingContext) {
		if (ignoredQNames.contains(attributeName)) {
			return true;
		}
		return false;
	}

}
