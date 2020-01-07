package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import gov.nist.secauto.metaschema.datatype.annotations.GroupAs;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class ListPropertyInfo extends AbstractCollectionPropertyInfo<ParameterizedType> implements CollectionPropertyInfo {

	public ListPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor, groupAs);
	}

	@Override
	public Type getItemType() {
		ParameterizedType actualType = getType();
		// this is a List so there is only a single generic type
		return actualType.getActualTypeArguments()[0];
	}

	@Override
	public Type getRawType() {
		return getType().getRawType();
	}

	@Override
	public boolean isList() {
		return true;
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
