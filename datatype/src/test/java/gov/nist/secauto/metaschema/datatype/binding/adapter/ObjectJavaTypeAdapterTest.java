package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.stream.XMLStreamException;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.ClassBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;

class ObjectJavaTypeAdapterTest {
	@RegisterExtension
    JUnit5Mockery context = new JUnit5Mockery();

	@Mock
	private BindingContext bindingContext;
	@Mock
	private XmlParsingContext parsingContext;
	@Mock
	private XmlParsePlan<Value> plan;
	@Mock
	private ClassBinding<Value> classBinding;
	@Mock
	private Value object;

	@Test
	void test() throws BindingException, XMLStreamException {

		context.checking(new Expectations() {{
			allowing(classBinding).getClazz(); will(returnValue(Value.class));
			oneOf(bindingContext).getXmlParsePlan(with(same(Value.class))); will(returnValue(plan));
			oneOf(plan).parse(with(same(parsingContext))); will(returnValue(object));
        }});

		ObjectJavaTypeAdapter<Value> adapter = new ObjectJavaTypeAdapter<>(classBinding, bindingContext);
		adapter.parseType(parsingContext);
		context.assertIsSatisfied();
	}

	private interface Value {
		
	}
}
