package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.markup.MarkupMultiline;

public class MarkupMultilineAdapter implements JavaTypeAdapter<MarkupMultiline> {
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
	public MarkupMultiline parseValue(String value) {
		return MarkupMultiline.fromMarkdown(value);
	}

	@Override
	public MarkupMultiline parseType(XmlParsingContext parsingContext) throws BindingException {
		try {
			return markupParser.parseMarkupMultiline(parsingContext.getEventReader());
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		return true;
	}
}
