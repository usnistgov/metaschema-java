package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassIntrospector;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;

public class MapPropertyInfo extends AbstractCollectionPropertyInfo implements CollectionPropertyInfo {

	public MapPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor, groupAs);
	}

	public Class<?> getKeyType() {
		ParameterizedType actualType = getType();
		// this is a Map so the first generic type is the key
		return (Class<?>)actualType.getActualTypeArguments()[0];
	}

	@Override
	public Class<?> getItemType() {
		return getValueType();
	}

	public Class<?> getValueType() {
		ParameterizedType actualType = getType();
		// this is a Map so the second generic type is the value
		return (Class<?>)actualType.getActualTypeArguments()[1];
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isMap() {
		return true;
	}

	@Override
	public MapPropertyCollector newPropertyCollector() {
		return new MapPropertyCollector(this);
	}

	public PropertyAccessor getJsonKey() {
		Class<?> itemClass = (Class<?>)getItemType();
		return ClassIntrospector.getJsonKey(itemClass);
	}


	private static class MapPropertyCollector extends AbstractPropertyCollector<MapPropertyInfo> {
		private final Map<String, Object> map = new LinkedHashMap<>();

		protected MapPropertyCollector(MapPropertyInfo propertyInfo) {
			super(propertyInfo);
		}

		@Override
		public void add(Object item) throws BindingException {
			PropertyAccessor keyAccessor = getPropertyInfo().getJsonKey();
			if (keyAccessor == null) {
				throw new BindingException("No JSON key found");
			}
			
			String key = keyAccessor.getValue(item).toString();
			map.put(key, item);
		}

		@Override
		protected Object getCollection() {
			return map;
		}
		
	}
}
