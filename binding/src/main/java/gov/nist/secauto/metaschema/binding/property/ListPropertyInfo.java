package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

import gov.nist.secauto.metaschema.binding.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class ListPropertyInfo extends AbstractCollectionPropertyInfo implements CollectionPropertyInfo {

	public ListPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor, groupAs);
	}

	@Override
	public Class<?> getItemType() {
		ParameterizedType actualType = getType();
		// this is a List so there is only a single generic type
		return (Class<?>)actualType.getActualTypeArguments()[0];
	}

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public ListPropertyCollector newPropertyCollector() {
		return new ListPropertyCollector(this);
	}

	private static class ListPropertyCollector extends AbstractPropertyCollector<ListPropertyInfo> {
		@SuppressWarnings("rawtypes")
		private List collection;

		@SuppressWarnings("rawtypes")
		protected ListPropertyCollector(ListPropertyInfo propertyInfo) {
			super(propertyInfo);
			this.collection = new LinkedList();
		}

		@Override
		public void applyCollection(Object obj) throws BindingException {
			try {
				getPropertyInfo().getPropertyAccessor().setValue(obj, collection);
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new BindingException(ex);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void add(Object item) {
			collection.add(item);
		}

		@Override
		protected Object getCollection() {
			return collection;
		}

	}

}
