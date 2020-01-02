package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.ParsePlan;

class ObjectJavaTypeAdapterTest {
	private static final String START_ELEMENT_OBJECT = "start-element-object";
	@RegisterExtension
    JUnit5Mockery context = new JUnit5Mockery();
    private Runnable runnable = context.mock(Runnable.class);

	@Mock
	private XMLEventReader2 reader;
	@Mock
	private ParsePlan<XMLEventReader2, Object> plan;
	@Mock
	private XMLEvent startObject;
	@Mock
	private Value object;
	@Auto
	private States readerState;

	@Test
	void test() throws BindingException, XMLStreamException {
		readerState.startsAs(START_ELEMENT_OBJECT);

		context.checking(new Expectations() {{
			allowing(reader).peek(); will(returnValue(startObject)); when(readerState.is(START_ELEMENT_OBJECT));
			oneOf(plan).parse(reader); will(returnValue(object));
        }});

		ObjectJavaTypeAdapter adapter = new ObjectJavaTypeAdapter(Object.class, plan);
		Object value = adapter.parseType(reader);

		context.assertIsSatisfied();
	}

	private interface Value {
		
	}
}
