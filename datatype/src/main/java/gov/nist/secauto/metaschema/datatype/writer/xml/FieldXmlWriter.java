package gov.nist.secauto.metaschema.datatype.writer.xml;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.FieldClassBinding;

public class FieldXmlWriter<CLASS> extends AbstractXmlWriter<CLASS, FieldClassBinding<CLASS>> {
	public FieldXmlWriter(FieldClassBinding<CLASS> classBinding, BindingContext bindingContext) {
		super(classBinding);
	}

	@Override
	protected void writeBody(CLASS obj, XmlWritingContext writingContext) {
	}

}
