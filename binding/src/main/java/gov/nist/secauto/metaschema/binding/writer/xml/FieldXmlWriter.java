package gov.nist.secauto.metaschema.binding.writer.xml;

import javax.xml.stream.events.StartElement;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FieldValuePropertyBinding;

public class FieldXmlWriter<CLASS> extends AbstractXmlWriter<CLASS, FieldClassBinding<CLASS>> {
	public FieldXmlWriter(FieldClassBinding<CLASS> classBinding, BindingContext bindingContext) {
		super(classBinding);
	}

	@Override
	protected void writeBody(Object obj, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		FieldValuePropertyBinding valueBinding = getClassBinding().getFieldValuePropertyBinding();
		JavaTypeAdapter<?> typeAdapter = writingContext.getBindingContext().getJavaTypeAdapter((Class<?>)valueBinding.getPropertyInfo().getItemType());

		try {
			Object value = valueBinding.getPropertyInfo().getPropertyAccessor().getValue(obj);
			// TODO: provide a value for the null below
			typeAdapter.write(value, null, parent, writingContext);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new BindingException(ex);
		}
	}
}
