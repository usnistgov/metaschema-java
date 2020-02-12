package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.junit.jupiter.api.Test;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.binding.datatypes.adapter.MarkupParser;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlEventUtil;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.markup.flexmark.AstCollectingVisitor;

class MarkupParserTest {

	@Test
	void test() throws XMLStreamException {
		String html = "<node>\n";
		html = html + "  <p> some text </p>\n";
		html = html + "  <p><q>text</q></p>\n";
		html = html + "  <p>some <b>text</b> <insert param-id=\"param-id\"/>.</p>\n";
		html = html + "  <h1>Example</h1>\n";
		html = html + "  <p><a href=\"link\">text</a></p>\n";
		html = html + "  <ul>\n";
		html = html + "    <li>a <strong>list item</strong></li>\n";
		html = html + "    <li>another <i>list item</i></li>\n";
		html = html + "  </ul>\n";
//		html = html + "  <table>\n";
//		html = html + "    <tr><th>Heading 1</th></tr>\n";
//		html = html + "    <tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr>\n";
//		html = html + "  </table>\n";
		html = html + "  <p>Some <em>more</em> <strong>text</strong><img alt=\"alt\" src=\"src\"/></p>";
		html = html + "</node>\n";
		XMLInputFactory2 factory = (XMLInputFactory2) WstxInputFactory.newInstance();
		factory.configureForXmlConformance();
		factory.setProperty(XMLInputFactory2.IS_COALESCING, true);
		XMLEventReader2 reader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(html));
		System.out.println("Start: " + XmlEventUtil.toString(reader.nextEvent()));
		System.out.println("Start: " + XmlEventUtil.toString(reader.nextEvent()));
		MarkupParser parser = new MarkupParser();
		MarkupString markupString = parser.parseMarkupMultiline(reader);
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(markupString.getDocument());
		System.out.println(html);
		System.out.println(visitor.getAst());
		System.out.println(markupString.toMarkdown());
	}

}
