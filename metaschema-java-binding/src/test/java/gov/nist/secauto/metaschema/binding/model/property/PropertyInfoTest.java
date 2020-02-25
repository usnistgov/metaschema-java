/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.MapPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

class PropertyInfoTest {
//	@RegisterExtension
//	JUnit5Mockery context = new JUnit5Mockery();
//	private PropertyAccessor propertyAccessor = context.mock(PropertyAccessor.class);

	@Test
	void testSington() throws NoSuchFieldException, SecurityException, BindingException {
		Field field = TestClass.class.getDeclaredField("singleton");
		
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);
		Assertions.assertEquals(String.class, propertyInfo.getRawType());

		TestClass testClass = new TestClass();
		propertyInfo.setValue(testClass, "test");
		Assertions.assertEquals("test", testClass.getSingleton());
	}

	@Test
	void testList() throws NoSuchFieldException, SecurityException, BindingException {
		Field field = TestClass.class.getDeclaredField("list");
		PropertyInfo propertyInfo = PropertyInfo.newPropertyInfo(field);
		Assertions.assertEquals(List.class, propertyInfo.getRawType());
		Assertions.assertEquals(String.class, propertyInfo.getItemType());

		TestClass testClass = new TestClass();
		propertyInfo.setValue(testClass, Collections.singletonList("test"));
		Assertions.assertEquals(Collections.singletonList("test"), testClass.getList());
	}

	@Test
	void testMap() throws NoSuchFieldException, SecurityException, BindingException {
		Field field = TestClass.class.getDeclaredField("map");
		MapPropertyInfo propertyInfo = (MapPropertyInfo)PropertyInfo.newPropertyInfo(field);
		Assertions.assertEquals(Map.class, propertyInfo.getRawType());
		Assertions.assertEquals(String.class, propertyInfo.getKeyType());
		Assertions.assertEquals(Integer.class, propertyInfo.getItemType());
		Assertions.assertEquals(Integer.class, propertyInfo.getValueType());

		TestClass testClass = new TestClass();
		propertyInfo.setValue(testClass, Collections.singletonMap("key",1));
		Assertions.assertEquals( Collections.singletonMap("key",1), testClass.getMap());
	}

	public static class TestClass {
		@gov.nist.secauto.metaschema.binding.model.annotations.Field
		private String singleton;
		@gov.nist.secauto.metaschema.binding.model.annotations.Field
		@GroupAs(maxOccurs = -1)
		private List<String> list;
		@gov.nist.secauto.metaschema.binding.model.annotations.Field
		@GroupAs(maxOccurs = -1, inJson = JsonGroupAsBehavior.KEYED)
		private Map<String, Integer> map;

		public String getSingleton() {
			return singleton;
		}
		public void setSingleton(String singleton) {
			this.singleton = singleton;
		}
		public List<String> getList() {
			return list;
		}
		public void setList(List<String> list) {
			this.list = list;
		}
		public Map<String, Integer> getMap() {
			return map;
		}
		public void setMap(Map<String, Integer> map) {
			this.map = map;
		}
	}
}
