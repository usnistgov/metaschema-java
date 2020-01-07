package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.util.Objects;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.ClassBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;

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
	public CLASS parseValue(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CLASS parseType(XmlParsingContext parsingContext) throws BindingException {
		// delegates all parsing to the parse plan
		XmlParsePlan<CLASS> plan = getXmlParsePlan();
		return plan.parse(parsingContext);
	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always handling another bound class, which is the type being parsed
		return false;
	}
}
