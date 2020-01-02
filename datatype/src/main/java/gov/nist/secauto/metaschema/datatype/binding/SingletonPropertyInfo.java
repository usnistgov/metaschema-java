package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.parser.AbstractPropertyCollector;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class SingletonPropertyInfo extends AbstractCollectionPropertyInfo<Type> implements CollectionPropertyInfo  {

	public SingletonPropertyInfo(Type type, PropertyAccessor propertyAccessor) {
		super(type, propertyAccessor, null);
	}

	@Override
	public SingletonPropertyCollector newPropertyCollector() {
		return new SingletonPropertyCollector(this);
	}

	private static class SingletonPropertyCollector extends AbstractPropertyCollector<SingletonPropertyInfo> {

		private Object object;

		protected SingletonPropertyCollector(SingletonPropertyInfo propertyInfo) {
			super(propertyInfo);
		}

		@Override
		public void add(Object item) throws BindingException {
			if (object != null) {
				throw new IllegalStateException("A value has already been set for this singleton");
			}
			object = item;
		}

		@Override
		protected Object getCollection() {
			return object;
		}

	}

}
