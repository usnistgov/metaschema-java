package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.HashMap;
import java.util.Map;

import gov.nist.itl.metaschema.model.xml.FieldTypes;
import gov.nist.itl.metaschema.model.xml.SimpleDatatypes;

public enum DataType {
	BOOLEAN(SimpleDatatypes.BOOLEAN.toString()),
	STRING(SimpleDatatypes.STRING.toString()),
	NCNAME(SimpleDatatypes.NC_NAME.toString()),
	DECIMAL(SimpleDatatypes.DECIMAL.toString()),
	INTEGER(SimpleDatatypes.INTEGER.toString()),
	NON_NEGATIVE_INTEGER(SimpleDatatypes.NON_NEGATIVE_INTEGER.toString()),
	POSITIVE_INTEGER(SimpleDatatypes.POSITIVE_INTEGER.toString()),
	DATE(SimpleDatatypes.DATE.toString()),
	DATE_TIME(SimpleDatatypes.DATE_TIME.toString()),
	DATE_WITH_TZ(SimpleDatatypes.DATE_WITH_TIMEZONE.toString()),
	DATE_TIME_WITH_TZ(SimpleDatatypes.DATE_TIME_WITH_TIMEZONE.toString()),
	BASE64(SimpleDatatypes.BASE_64_BINARY.toString()),
	EMAIL_ADDRESS(SimpleDatatypes.EMAIL.toString()),
	HOSTNAME(SimpleDatatypes.HOSTNAME.toString()),
	IP_V4_ADDRESS(SimpleDatatypes.IP_V_4_ADDRESS.toString()),
	IP_V6_ADDRESS(SimpleDatatypes.IP_V_6_ADDRESS.toString()),
	URI(SimpleDatatypes.URI.toString()),
	URI_REFERENCE(SimpleDatatypes.URI_REFERENCE.toString()),
	MARKUP_LINE(FieldTypes.Member.MARKUP_LINE.toString()),
	MARKUP_MULTILINE(FieldTypes.Member.MARKUP_MULTILINE.toString()),
	EMPTY(FieldTypes.Member.EMPTY.toString());

	private static final Map<String, DataType> nameToEnumMap;

	static {
		nameToEnumMap = new HashMap<>();
		for (DataType e : values()) {
			nameToEnumMap.put(e.getName(), e);
		}
	}

	public static DataType lookup(String name) {
		return nameToEnumMap.get(name);
	}

	public static DataType lookup(SimpleDatatypes.Enum e) {
		return nameToEnumMap.get(e.toString());
	}

	private final String name;

	private DataType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
