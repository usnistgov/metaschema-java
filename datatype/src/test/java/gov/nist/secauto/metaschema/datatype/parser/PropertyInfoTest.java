package gov.nist.secauto.metaschema.datatype.parser;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.nist.secauto.metaschema.datatype.annotations.GroupAs;
import gov.nist.secauto.metaschema.datatype.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.datatype.binding.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.datatype.binding.property.MapPropertyInfo;
import gov.nist.secauto.metaschema.datatype.binding.property.PropertyInfo;

class PropertyInfoTest {
//	@RegisterExtension
//	JUnit5Mockery context = new JUnit5Mockery();
//	private PropertyAccessor propertyAccessor = context.mock(PropertyAccessor.class);

	@Test
	void testSington() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, BindingException {
		Field field = TestClass.class.getDeclaredField("singleton");
		
		PropertyInfo propertyInfo = CollectionPropertyInfo.newCollectionPropertyInfo(field);
		Assertions.assertEquals(String.class, propertyInfo.getRawType());

		TestClass testClass = new TestClass();
		propertyInfo.getPropertyAccessor().setValue(testClass, "test");
		Assertions.assertEquals("test", testClass.getSingleton());
	}

	@Test
	void testList() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, BindingException {
		Field field = TestClass.class.getDeclaredField("list");
		PropertyInfo propertyInfo = CollectionPropertyInfo.newCollectionPropertyInfo(field);
		Assertions.assertEquals(List.class, propertyInfo.getRawType());
		Assertions.assertEquals(String.class, propertyInfo.getItemType());

		TestClass testClass = new TestClass();
		propertyInfo.getPropertyAccessor().setValue(testClass, Collections.singletonList("test"));
		Assertions.assertEquals(Collections.singletonList("test"), testClass.getList());
	}

	@Test
	void testMap() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, BindingException {
		Field field = TestClass.class.getDeclaredField("map");
		MapPropertyInfo propertyInfo = (MapPropertyInfo)CollectionPropertyInfo.newCollectionPropertyInfo(field);
		Assertions.assertEquals(Map.class, propertyInfo.getRawType());
		Assertions.assertEquals(String.class, propertyInfo.getKeyType());
		Assertions.assertEquals(Integer.class, propertyInfo.getItemType());
		Assertions.assertEquals(Integer.class, propertyInfo.getValueType());

		TestClass testClass = new TestClass();
		propertyInfo.getPropertyAccessor().setValue(testClass, Collections.singletonMap("key",1));
		Assertions.assertEquals( Collections.singletonMap("key",1), testClass.getMap());
	}

	public static class TestClass {
		@gov.nist.secauto.metaschema.datatype.annotations.Field
		private String singleton;
		@gov.nist.secauto.metaschema.datatype.annotations.Field
		@GroupAs(maxOccurs = -1)
		private List<String> list;
		@gov.nist.secauto.metaschema.datatype.annotations.Field
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
