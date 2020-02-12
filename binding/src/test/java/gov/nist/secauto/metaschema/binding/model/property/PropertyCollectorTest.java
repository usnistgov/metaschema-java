package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyCollector;

// TODO: test map
class PropertyCollectorTest {

	@Test
	void testSingleton() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, BindingException {
		String value = "test1";

		Field field = TestClass.class.getDeclaredField("singleton");
		FieldPropertyBinding binding = FieldPropertyBinding.fromJavaField(field, field.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.Field.class));

		PropertyCollector collector = binding.getPropertyInfo().newPropertyCollector();
		collector.add(value);

		TestClass obj = TestClass.class.getDeclaredConstructor().newInstance();
		collector.applyCollection(obj);
		Assertions.assertEquals(value, obj.singleton);
	}

	@Test
	void testList() throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, BindingException {
		List<String> values = new LinkedList<>();
		values.add("test1");
		values.add("test2");

		Field field = TestClass.class.getDeclaredField("list");
		FieldPropertyBinding binding = FieldPropertyBinding.fromJavaField(field, field.getAnnotation(gov.nist.secauto.metaschema.binding.model.annotations.Field.class));

		PropertyCollector collector = binding.getPropertyInfo().newPropertyCollector();
		for (String value : values) {
			collector.add(value);
		}

		TestClass obj = TestClass.class.getDeclaredConstructor().newInstance();
		collector.applyCollection(obj);
		Assertions.assertEquals(values, obj.list);
	}

	private static class TestClass {
		@gov.nist.secauto.metaschema.binding.model.annotations.Field
		private String singleton;

		@gov.nist.secauto.metaschema.binding.model.annotations.Field
		@GroupAs(maxOccurs = -1)
		private List<String> list;
	}
}
