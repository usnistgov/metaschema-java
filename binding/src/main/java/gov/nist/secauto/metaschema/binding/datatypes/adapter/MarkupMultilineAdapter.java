package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatype.markup.MarkupString;

public class MarkupMultilineAdapter  extends AbstractMarkupAdapter<MarkupMultiline> {

	public MarkupMultilineAdapter() {
		super();
	}

	@Override
	public boolean isUnrappedValueAllowedInXml() {
		return true;
	}

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
		writingVisitor.process(((MarkupString<?>) value).getDocument(), writingContext.getEventWriter(), true);
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
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

	@Override
	public MarkupMultiline parse(JsonParsingContext parsingContext) throws BindingException {
		try {
			JsonParser parser = parsingContext.getEventReader();
			MarkupMultiline retval = parse(parser.getValueAsString());
			// skip past value
			parser.nextToken();
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public String getDefaultJsonFieldName() {
		return "PROSE";
	}

}
