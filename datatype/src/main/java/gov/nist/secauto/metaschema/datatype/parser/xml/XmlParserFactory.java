package gov.nist.secauto.metaschema.datatype.parser.xml;

import org.codehaus.stax2.XMLInputFactory2;

import com.ctc.wstx.stax.WstxInputFactory;

public class XmlParserFactory {
	private final XMLInputFactory2 factory;

	public XmlParserFactory() {
		factory = (XMLInputFactory2)WstxInputFactory.newInstance();
		// disable construction
	}

	public XmlParser newXmlParser() {
		return new DefaultXmlParser(factory);
	}
}
