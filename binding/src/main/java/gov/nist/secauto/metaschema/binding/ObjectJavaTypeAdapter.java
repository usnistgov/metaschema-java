package gov.nist.secauto.metaschema.binding;

import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWriter;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;

public class ObjectJavaTypeAdapter<CLASS> implements JavaTypeAdapter<CLASS> {
	private final ClassBinding<CLASS> classBinding;
	private final BindingContext bindingContext;

	private XmlParsePlan<CLASS> xmlParsePlan;

	public ObjectJavaTypeAdapter(ClassBinding<CLASS> classBinding, BindingContext bindingContext) {
		Objects.requireNonNull(classBinding, "classBinding");
		Objects.requireNonNull(bindingContext, "bindingContext");
		this.classBinding = classBinding;
		this.bindingContext = bindingContext;
	}

	protected ClassBinding<CLASS> getClassBinding() {
		return classBinding;
	}

	protected XmlParsePlan<CLASS> getXmlParsePlan() throws BindingException {
		synchronized (this) {
			if (xmlParsePlan == null) {
				xmlParsePlan = bindingContext.getXmlParsePlan(getClassBinding().getClazz());
			}
			return xmlParsePlan;
		}
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
	public CLASS parse(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CLASS parse(XmlParsingContext parsingContext) throws BindingException {
		// delegates all parsing to the parse plan
		XmlParsePlan<CLASS> plan = getXmlParsePlan();
		return plan.parse(parsingContext);
	}

	@Override
	public void write(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		XmlWriter writer = writingContext.getBindingContext().getXmlWriter(getClassBinding().getClazz());
		writer.writeXml(value, valueQName, writingContext);
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always handling another bound class, which is the type being parsed
		return false;
	}
}
