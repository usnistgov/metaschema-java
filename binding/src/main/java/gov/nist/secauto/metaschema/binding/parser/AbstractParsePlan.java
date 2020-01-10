package gov.nist.secauto.metaschema.binding.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public abstract class AbstractParsePlan<PARSER extends ParsingContext<READER>, READER, CLASS> implements ParsePlan<PARSER, READER, CLASS> {
	private final Class<CLASS> clazz;

	public AbstractParsePlan(Class<CLASS> clazz) {
		Objects.requireNonNull(clazz, "clazz");

		this.clazz = clazz;
	}

	protected Class<CLASS> getClazz() {
		return clazz;
	}
//
//	protected BindingContext getBindingContext() {
//		return bindingContext;
//	}

	protected CLASS newInstance() throws BindingException {
		Class<CLASS> clazz = getClazz();
		CLASS retval;
		try {
			Constructor<CLASS> constructor = (Constructor<CLASS>) clazz.getDeclaredConstructor();
			retval = constructor.newInstance();
		} catch (NoSuchMethodException e) {
			String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
			throw new BindingException(msg);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BindingException(e);
		}
		return retval;
	}

}
