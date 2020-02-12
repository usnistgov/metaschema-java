package gov.nist.secauto.metaschema.binding;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

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
	void test() throws BindingException {

		context.checking(new Expectations() {{
			allowing(parsingContext).getBindingContext(); will(returnValue(bindingContext));
			allowing(classBinding).getClazz(); will(returnValue(Value.class));
			oneOf(classBinding).getXmlParsePlan(with(same(bindingContext))); will(returnValue(plan));
			oneOf(plan).parse(with(same(parsingContext))); will(returnValue(object));
        }});

		ObjectJavaTypeAdapter<Value> adapter = new ObjectJavaTypeAdapter<>(classBinding);
		adapter.parse(parsingContext);
		context.assertIsSatisfied();
	}

	private interface Value {
		
	}
}
