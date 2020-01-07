package gov.nist.secauto.metaschema.datatype.parser.xml;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.parser.ParsingContext;

public interface XmlParsingContext extends ParsingContext<XMLEventReader2> {
	
	@Override
	XmlProblemHandler getProblemHandler();
}
