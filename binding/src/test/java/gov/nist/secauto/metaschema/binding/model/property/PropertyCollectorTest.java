/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
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
