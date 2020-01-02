package gov.nist.secauto.metaschema.datatype.util;

import java.util.Iterator;

public class Util {
	public static <T> Iterable<T> toIterable(Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
}
