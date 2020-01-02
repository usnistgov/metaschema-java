package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nist.secauto.metaschema.datatype.annotations.GroupAs;
import gov.nist.secauto.metaschema.datatype.parser.AbstractPropertyCollector;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class MapPropertyInfo extends AbstractCollectionPropertyInfo<ParameterizedType> implements CollectionPropertyInfo {

	public MapPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor, groupAs);
	}

	public Type getKeyType() {
		ParameterizedType actualType = getType();
		// this is a Map so the first generic type is the key
		return actualType.getActualTypeArguments()[0];
	}

	@Override
	public Type getItemType() {
		return getValueType();
	}

	public Type getValueType() {
		ParameterizedType actualType = getType();
		// this is a Map so the second generic type is the value
		return actualType.getActualTypeArguments()[1];
	}

	@Override
	public Type getRawType() {
		return getType().getRawType();
	}

	@Override
	public boolean isMap() {
		return true;
	}

	@Override
	public MapPropertyCollector newPropertyCollector() {
		return new MapPropertyCollector(this);
	}

	public PropertyAccessor getJsonKey(Object item) {
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
			PropertyAccessor keyAccessor = getPropertyInfo().getJsonKey(item);
			if (keyAccessor == null) {
				throw new BindingException("No JSON key found");
			}
			
			String key;
			try {
				key = keyAccessor.getValue(item).toString();
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new BindingException(ex);
			}
			map.put(key, item);
		}

		@Override
		protected Object getCollection() {
			return map;
		}
		
	}
}
