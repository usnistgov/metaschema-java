package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jmock.Expectations;

public class MockXmlSupport {
	private MockXmlSupport() {
		// disable construction
	}


	static void mockElementXMLEvent(Expectations expectations, StartElement startEvent, EndElement endEvent,
			QName name) {
		expectations.allowing(startEvent).getEventType();
		expectations.will(Expectations.returnValue(XMLEvent.START_ELEMENT));
		expectations.allowing(startEvent).asStartElement();
		expectations.will(Expectations.returnValue(startEvent));
		expectations.allowing(startEvent).getName();
		expectations.will(Expectations.returnValue(name));
		expectations.allowing(startEvent).isStartElement();
		expectations.will(Expectations.returnValue(true));
		expectations.allowing(startEvent).isEndElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(startEvent).isAttribute();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(startEvent).isCharacters();
		expectations.will(Expectations.returnValue(false));

		expectations.allowing(endEvent).getEventType();
		expectations.will(Expectations.returnValue(XMLEvent.END_ELEMENT));
		expectations.allowing(endEvent).asEndElement();
		expectations.will(Expectations.returnValue(endEvent));
		expectations.allowing(endEvent).getName();
		expectations.will(Expectations.returnValue(name));
		expectations.allowing(endEvent).isStartElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(endEvent).isEndElement();
		expectations.will(Expectations.returnValue(true));
		expectations.allowing(endEvent).isAttribute();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(endEvent).isCharacters();
		expectations.will(Expectations.returnValue(false));
	}

	static void mockCharactersXMLEvent(Expectations expectations, Characters event, String text) {
		expectations.allowing(event).getEventType();
		expectations.will(Expectations.returnValue(XMLEvent.CHARACTERS));
		expectations.allowing(event).asCharacters();
		expectations.will(Expectations.returnValue(event));
		expectations.allowing(event).isStartElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isEndElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isAttribute();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isCharacters();
		expectations.will(Expectations.returnValue(true));
		expectations.allowing(event).getData();
		expectations.will(Expectations.returnValue(text));
	}


	public static void mockAttributeXMLEvent(Expectations expectations, Attribute event,
			QName name, String text) {
		expectations.allowing(event).getEventType();
		expectations.will(Expectations.returnValue(XMLEvent.ATTRIBUTE));
		expectations.allowing(event).getName();
		expectations.will(Expectations.returnValue(name));
		expectations.allowing(event).isStartElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isEndElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isAttribute();
		expectations.will(Expectations.returnValue(true));
		expectations.allowing(event).isCharacters();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).getValue();
		expectations.will(Expectations.returnValue(text));
	}


	public static void mockEndDocument(Expectations expectations, EndDocument event) {
		expectations.allowing(event).getEventType();
		expectations.will(Expectations.returnValue(XMLEvent.END_DOCUMENT));
		expectations.allowing(event).isStartElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isEndElement();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isAttribute();
		expectations.will(Expectations.returnValue(false));
		expectations.allowing(event).isCharacters();
		expectations.will(Expectations.returnValue(false));
	}

}
