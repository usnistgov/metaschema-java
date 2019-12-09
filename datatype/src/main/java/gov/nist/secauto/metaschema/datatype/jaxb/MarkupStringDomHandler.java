package gov.nist.secauto.metaschema.datatype.jaxb;

import java.io.StringWriter;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class MarkupStringDomHandler implements DomHandler<Object, DOMResult> {
	@Override
	public DOMResult createUnmarshaller(ValidationEventHandler errorHandler) {
		return new DOMResult();
//		return new StreamResult(new StringWriter());
	}

	@Override
	public Object getElement(DOMResult rt) {
		Node node = rt.getNode();
		StringWriter writer = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		String xml = writer.toString();
		return xml;
	}

	@Override
	public Source marshal(Object n, ValidationEventHandler errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}

}
