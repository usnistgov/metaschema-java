package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.binding.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.FieldXmlParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.writer.xml.FieldXmlWriter;
import gov.nist.secauto.metaschema.datatype.writer.xml.XmlWriter;

// TODO: handle collapsible
public class FieldClassBinding<CLASS> extends AbstractClassBinding<CLASS> {
	private final FieldValuePropertyBinding fieldValuePropertyBinding;

	public FieldClassBinding(Class<CLASS> clazz) {
		super(clazz);
		this.fieldValuePropertyBinding = ClassIntrospector.getFieldValueBinding(clazz);
	}

	public FieldValuePropertyBinding getFieldValuePropertyBinding() {
		return fieldValuePropertyBinding;
	}

	@Override
	public XmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
		return new FieldXmlParsePlan<CLASS>(this, bindingContext);
	}

	@Override
	public XmlWriter<CLASS> newXmlWriter(BindingContext bindingContext) {
		return new FieldXmlWriter<CLASS>(this, bindingContext);
	}
}
