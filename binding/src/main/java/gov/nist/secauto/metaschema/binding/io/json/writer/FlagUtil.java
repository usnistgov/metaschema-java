package gov.nist.secauto.metaschema.binding.io.json.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class FlagUtil {
	private FlagUtil() {
		// disable construction
	}

	public static List<FlagPropertyBinding> filterFlags(Object obj, List<FlagPropertyBinding> flags, PropertyBindingFilter filter)
			throws BindingException {
		List<FlagPropertyBinding> retval;
		if (flags.isEmpty()) {
			retval = Collections.emptyList();
		} else {
			retval = new ArrayList<>(flags.size());
			for (FlagPropertyBinding flag : flags) {
				if (filter == null || !filter.filter(flag)) {
					Object flagValue = flag.getPropertyInfo().getValue(obj);
					if (flagValue != null) {
						retval.add(flag);
					}
				}
			}
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	public static void writeFlags(Object obj, List<FlagPropertyBinding> flags, JsonWritingContext writingContext)
			throws BindingException, IOException {
		for (FlagPropertyBinding flagBinding : flags) {
			Object value = flagBinding.getPropertyInfo().getValue(obj);
			if (value != null) {
				writeFlag(flagBinding, value, writingContext);
			}
		}
	}

	public static void writeFlag(FlagPropertyBinding flagBinding, Object value, JsonWritingContext writingContext) throws IOException, BindingException {
		JsonGenerator generator = writingContext.getEventWriter();
		String name = flagBinding.getJsonFieldName(writingContext.getBindingContext());

		generator.writeFieldName(name);

		JavaTypeAdapter<?> adapter = writingContext.getBindingContext()
				.getJavaTypeAdapter(flagBinding.getPropertyInfo().getItemType());

		adapter.writeJsonFieldValue(value, null, writingContext);
	}

	public static PropertyBindingFilter adjustFilterToIncludeFlag(PropertyBindingFilter filter,
			FlagPropertyBinding jsonValueKeyFlag) {
		PropertyBindingFilter retval;
		if (filter == null) {
			retval = (PropertyBinding binding) -> jsonValueKeyFlag.equals(binding);
		} else {
			retval = (PropertyBinding binding) -> filter.filter(binding) || jsonValueKeyFlag.equals(binding);
		}
		return retval;
	}

}
