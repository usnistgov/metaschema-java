package gov.nist.secauto.metaschema.datatype.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractParsePlan<READER, CLASS> implements ParsePlan<READER, CLASS> {
	private final Class<CLASS> clazz;

	public AbstractParsePlan(Class<CLASS> clazz) {
		this.clazz = clazz;
	}

	protected Class<CLASS> getClazz() {
		return clazz;
	}

	protected CLASS newInstance() throws BindingException {
		CLASS retval;
		try {
			Constructor<CLASS> constructor = (Constructor<CLASS>) getClazz().getDeclaredConstructor();
			retval = constructor.newInstance();
		} catch (NoSuchMethodException e) {
			String msg = String.format("Class '%s' does not have a required no-arg constructor.", getClazz());
			throw new BindingException(msg);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BindingException(e);
		}
		return retval;
	}

}
