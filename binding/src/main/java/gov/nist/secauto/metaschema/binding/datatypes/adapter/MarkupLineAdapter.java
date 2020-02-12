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
import gov.nist.secauto.metaschema.markup.MarkupLine;
import gov.nist.secauto.metaschema.markup.MarkupString;

public class MarkupLineAdapter extends AbstractMarkupAdapter<MarkupLine> {

//	@Override
//	public boolean isParsingEndElement() {
//		return false;
//	}

	@Override
	public MarkupLine parse(String value) {
		return MarkupLine.fromMarkdown(value);
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		return false;
	}

	@Override
	public MarkupLine parse(XmlParsingContext parsingContext) throws BindingException {
		try {
			return getMarkupParser().parseMarkupline(parsingContext.getEventReader());
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	protected void writeXmlElementInternal(Object value, StartElement parent, XmlWritingContext writingContext)
			throws BindingException {
		MarkupXmlWriter writingVisitor = new MarkupXmlWriter(parent.getName().getNamespaceURI(),
				writingContext.getXMLEventFactory());
		writingVisitor.process(((MarkupString) value).getDocument(), writingContext.getEventWriter(), false);
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {

		MarkupLine ml;
		try {
			ml = (MarkupLine) value;
		} catch (ClassCastException ex) {
			throw new BindingException(ex);
		}

		JsonGenerator generator = writingContext.getEventWriter();
		String jsonString;
		if (generator instanceof YAMLGenerator) {
			jsonString = ml.toMarkdownYaml().trim();
		} else {
			jsonString = ml.toMarkdown().trim();
		}
		try {
			generator.writeString(jsonString);
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public MarkupLine parse(JsonParsingContext parsingContext) throws BindingException {
		try {
			JsonParser parser = parsingContext.getEventReader();
			MarkupLine retval = parse(parser.getValueAsString());
			// skip past value
			parser.nextToken();
			return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public String getDefaultJsonFieldName() {
		return "RICHTEXT";
	}

	@Override
	public boolean isUnrappedValueAllowedInXml() {
		return false;
	}

}
