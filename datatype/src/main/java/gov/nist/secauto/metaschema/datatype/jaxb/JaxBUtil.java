package gov.nist.secauto.metaschema.datatype.jaxb;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class JaxBUtil {

	private JaxBUtil() {
		// disable construction
	}

	public static <T> T parse(Reader reader, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		@SuppressWarnings("unchecked")
		T result = (T) jaxbUnmarshaller.unmarshal(reader);
		return result;
	}

	public static <ITEM, KEY> LinkedHashMap<KEY, ITEM> listToMap(List<ITEM> list, Function<ITEM, KEY> keyFunction) {
		LinkedHashMap<KEY, ITEM> retval = new LinkedHashMap<>();
		for (ITEM item : list) {
			KEY key = keyFunction.apply(item);
			retval.put(key, item);
		}
		return retval;
	}

	public static <ITEM, KEY> List<ITEM> mapToList(LinkedHashMap<KEY, ITEM> map) {
		List<ITEM> retval = new LinkedList<>();
		for (ITEM item : map.values()) {
			retval.add(item);
		}
		return retval;
	}
}
