package gov.nist.secauto.metaschema.binding;

import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

class ObjectJavaTypeAdapter<CLASS> implements JavaTypeAdapter<CLASS> {
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
	public CLASS parse(JsonParsingContext parsingContext) throws BindingException {
		// TODO: support same pathway as parse(XML)
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		XmlWriter writer = getClassBinding().getXmlWriter();
		writer.writeXml(value, valueQName, writingContext);
	}

	@Override
	public void writeJsonFieldValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext) throws BindingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDefaultJsonFieldName() {
		throw new UnsupportedOperationException("A bound object must always be referenced from an assembly or field property");
	}

	@Override
	public boolean isUnrappedValueAllowedInXml() {
		return false;
	}

}
