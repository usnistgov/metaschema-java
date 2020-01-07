package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.markup.MarkupLine;

public class MarkupLineAdapter implements JavaTypeAdapter<MarkupLine> {
	private MarkupParser markupParser = new MarkupParser();

	@Override
	public boolean isParsingStartElement() {
		return false;
	}

//	@Override
//	public boolean isParsingEndElement() {
//		return false;
//	}

	@Override
	public MarkupLine parseValue(String value) {
		return MarkupLine.fromMarkdown(value);
	}

	@Override
	public MarkupLine parseType(XmlParsingContext parsingContext) throws BindingException {
		try {
			return markupParser.parseMarkupline(parsingContext.getEventReader());
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		return false;
	}
}
