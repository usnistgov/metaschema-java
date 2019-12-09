package gov.nist.secauto.metaschema.datatype.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public class MarkupStringXmlAdapter extends XmlAdapter<Element, MarkupString> {

	@Override
	public MarkupString unmarshal(Element element) throws Exception {
		for (Node node : DomUtil.iterable(element.getChildNodes())) {
//			node.
		}
		return null;
	}

	@Override
	public Element marshal(MarkupString v) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
