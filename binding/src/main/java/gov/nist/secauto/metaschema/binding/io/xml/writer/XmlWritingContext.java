package gov.nist.secauto.metaschema.binding.io.xml.writer;

import javax.xml.stream.XMLEventWriter;

import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.binding.io.WritingContext;

public interface XmlWritingContext extends WritingContext<XMLEventWriter> {
	XMLEventFactory2 getXMLEventFactory();
}
