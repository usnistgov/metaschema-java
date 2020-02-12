package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.stream.EventFilter;
import javax.xml.stream.events.XMLEvent;

public class CommentFilter implements EventFilter {

	@Override
	public boolean accept(XMLEvent event) {
		return event.getEventType() != XMLEvent.COMMENT;
	}

}
