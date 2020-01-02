package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.namespace.QName;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.ParsePlan;

public class ObjectJavaTypeAdapter<CLASS> implements JavaTypeAdapter<CLASS> {
	private final ParsePlan<XMLEventReader2, CLASS> parsePlan;

	public ObjectJavaTypeAdapter(Class<CLASS> itemClass, ParsePlan<XMLEventReader2, CLASS> parsePlan) {
		this.parsePlan = parsePlan;
	}

	protected ParsePlan<XMLEventReader2, CLASS> getParsePlan() {
		return parsePlan;
	}

	@Override
	public boolean isParsingStartElement() {
		return true;
	}

//	@Override
//	public boolean isParsingEndElement() {
//		return true;
//	}

	@Override
	public CLASS parseValue(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CLASS parseType(XMLEventReader2 reader) throws BindingException {
		// delegates all parsing to the parse plan
		return getParsePlan().parse(reader);
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always handling another bound class, which is the type being parsed
		return false;
	}
}
