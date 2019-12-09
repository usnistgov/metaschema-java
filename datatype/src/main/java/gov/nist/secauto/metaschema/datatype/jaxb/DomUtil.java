package gov.nist.secauto.metaschema.datatype.jaxb;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public class DomUtil {

	private DomUtil() {
		// prevent construction
	}

	public static Iterable<Node> iterable(final NodeList nodeList) {
	    return () -> new Iterator<Node>() {

	        private int index = 0;

	        @Override
	        public boolean hasNext() {
	            return index < nodeList.getLength();
	        }

	        @Override
	        public Node next() {
	            if (!hasNext())
	                throw new NoSuchElementException();
	            return nodeList.item(index++); 
	        }
	    };
	}

	public static MarkupString unmarshalToMarkupString(List<Element> elements) {
		if (elements == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			for (Element element : elements) {
				trans.transform(new DOMSource(element), new StreamResult(writer));
			}
		} catch (final TransformerConfigurationException ex) {
			throw new IllegalStateException(ex);
		} catch (final TransformerException ex) {
			throw new IllegalArgumentException(ex);
		}
		return MarkupString.fromHTML(writer.toString());
	}

	public static List<Element> marshalFromMarkupString(MarkupString value) {
		List<Element> retval;
		if (value == null) {
			retval = null;
		} else {
			retval = null;
//			Node node = value.getNode();
		}
		return retval;
	}
}
