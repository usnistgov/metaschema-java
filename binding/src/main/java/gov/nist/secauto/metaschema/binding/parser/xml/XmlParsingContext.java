package gov.nist.secauto.metaschema.binding.parser.xml;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.binding.parser.ParsingContext;

public interface XmlParsingContext extends ParsingContext<XMLEventReader2> {
	
	@Override
	XmlProblemHandler getProblemHandler();
}
