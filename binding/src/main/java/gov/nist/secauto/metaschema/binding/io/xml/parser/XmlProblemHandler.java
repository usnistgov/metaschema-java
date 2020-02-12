package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.io.ProblemHandler;

public interface XmlProblemHandler extends ProblemHandler {
	/**
	 * Callback used to handle an attribute that is unknown to the model being parsed. 
	 * @param obj
	 * @param attributeName
	 * @param parsingContext
	 * @return {@code true} if the attribute was processed by this handler, or {@code false} otherwise
	 */
	boolean handleUnknownAttribute(Object obj, QName attributeName, XmlParsingContext parsingContext);
}
