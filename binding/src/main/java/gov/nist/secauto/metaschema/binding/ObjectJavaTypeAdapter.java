package gov.nist.secauto.metaschema.binding;

import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWriter;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;

public class ObjectJavaTypeAdapter<CLASS> implements JavaTypeAdapter<CLASS> {
	private final ClassBinding<CLASS> classBinding;

	public ObjectJavaTypeAdapter(ClassBinding<CLASS> classBinding) {
		Objects.requireNonNull(classBinding, "classBinding");
		this.classBinding = classBinding;
	}

	protected ClassBinding<CLASS> getClassBinding() {
		return classBinding;
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
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always handling another bound class, which is the type being parsed
		return false;
	}

	@Override
	public CLASS parse(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CLASS parse(XmlParsingContext parsingContext) throws BindingException {
		// delegates all parsing to the parse plan
		XmlParsePlan<CLASS> plan = getClassBinding().getXmlParsePlan(parsingContext.getBindingContext());
		return plan.parse(parsingContext);
	}

	@Override
	public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		XmlWriter writer = getClassBinding().getXmlWriter();
		writer.writeXml(value, valueQName, writingContext);
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException {
		throw new UnsupportedOperationException();
	}
}
