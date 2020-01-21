package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.markup.MarkupString;

public class MarkupMultilineAdapter  extends AbstractMarkupAdapter<MarkupMultiline> {
	@Override
	public MarkupMultiline parse(String value) {
		return MarkupMultiline.fromMarkdown(value);
	}

	@Override
	public MarkupMultiline parse(XmlParsingContext parsingContext) throws BindingException {
		try {
			return getMarkupParser().parseMarkupMultiline(parsingContext.getEventReader());
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		return true;
	}


	@Override
	protected void writeXmlElementInternal(Object value, StartElement parent, XmlWritingContext writingContext)
			throws BindingException {
		MarkupXmlWriter writingVisitor = new MarkupXmlWriter(parent.getName().getNamespaceURI(),
				writingContext.getXMLEventFactory());
		writingVisitor.process(((MarkupString) value).getDocument(), writingContext.getEventWriter(), true);
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {

		MarkupMultiline mml;
		try {
			mml = (MarkupMultiline) value;
		} catch (ClassCastException ex) {
			throw new BindingException(ex);
		}

		JsonGenerator generator = writingContext.getEventWriter();
		String jsonString;
		if (generator instanceof YAMLGenerator) {
			jsonString = mml.toMarkdownYaml().trim();
		} else {
			jsonString = mml.toMarkdown().trim();
		}
		try {
			generator.writeString(jsonString);
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}
}
