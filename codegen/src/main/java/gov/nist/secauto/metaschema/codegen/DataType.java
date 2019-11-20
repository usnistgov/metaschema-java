package gov.nist.secauto.metaschema.codegen;

import java.util.HashMap;
import java.util.Map;

public enum DataType {
	NCNAME(gov.nist.secauto.metaschema.model.DataType.NCNAME, DataType.DATATYPE_PACKAGE_NAME, "NCName", true),
	DECIMAL(gov.nist.secauto.metaschema.model.DataType.DECIMAL, DataType.DATATYPE_PACKAGE_NAME, "Decimal", true),
	INTEGER(gov.nist.secauto.metaschema.model.DataType.INTEGER, DataType.DATATYPE_PACKAGE_NAME, "Integer", true),
	NON_NEGATIVE_INTEGER(gov.nist.secauto.metaschema.model.DataType.NON_NEGATIVE_INTEGER, DataType.DATATYPE_PACKAGE_NAME, "NonNegativeInteger", true),
	POSITIVE_INTEGER(gov.nist.secauto.metaschema.model.DataType.POSITIVE_INTEGER, DataType.DATATYPE_PACKAGE_NAME, "PositiveInteger", true),
	DATE(gov.nist.secauto.metaschema.model.DataType.DATE, DataType.DATATYPE_PACKAGE_NAME, "Date", true),
	DATE_TIME(gov.nist.secauto.metaschema.model.DataType.DATE_TIME, DataType.DATATYPE_PACKAGE_NAME, "DateTime", true),
	DATE_WITH_TZ(gov.nist.secauto.metaschema.model.DataType.DATE_WITH_TZ, DataType.DATATYPE_PACKAGE_NAME, "DateTimeZone", true),
	DATE_TIME_WITH_TZ(gov.nist.secauto.metaschema.model.DataType.DATE_TIME_WITH_TZ, DataType.DATATYPE_PACKAGE_NAME, "DateTimeTimeZone", true),
	BASE64(gov.nist.secauto.metaschema.model.DataType.BASE64, DataType.DATATYPE_PACKAGE_NAME, "Base64", true),
	EMAIL_ADDRESS(gov.nist.secauto.metaschema.model.DataType.EMAIL_ADDRESS, DataType.DATATYPE_PACKAGE_NAME, "EmailAddress", true),
	HOSTNAME(gov.nist.secauto.metaschema.model.DataType.HOSTNAME, DataType.DATATYPE_PACKAGE_NAME, "Hostname", true),
	IP_V4_ADDRESS(gov.nist.secauto.metaschema.model.DataType.IP_V4_ADDRESS, DataType.DATATYPE_PACKAGE_NAME, "IPv4", true),
	IP_V6_ADDRESS(gov.nist.secauto.metaschema.model.DataType.IP_V6_ADDRESS, DataType.DATATYPE_PACKAGE_NAME, "IPv6", true),
	URI(gov.nist.secauto.metaschema.model.DataType.URI, DataType.DATATYPE_PACKAGE_NAME, "URI", true),
	URI_REFERENCE(gov.nist.secauto.metaschema.model.DataType.URI_REFERENCE, DataType.DATATYPE_PACKAGE_NAME, "URIReference", true),
	MARKUP_LINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_LINE, DataType.DATATYPE_PACKAGE_NAME,"MarkupString", true),
	MARKUP_MULTILINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_MULTILINE, DataType.DATATYPE_PACKAGE_NAME,"MarkupString", true),
	EMPTY(gov.nist.secauto.metaschema.model.DataType.EMPTY, null,null, false),
	BOOLEAN(gov.nist.secauto.metaschema.model.DataType.BOOLEAN, null, "boolean", false),
	STRING(gov.nist.secauto.metaschema.model.DataType.STRING, String.class.getPackageName(), "String", false);

	private static final Map<gov.nist.secauto.metaschema.model.DataType, DataType> datatypeMap;

	static {
		datatypeMap = new HashMap<>();
		for (DataType e : values()) {
			datatypeMap.put(e.getDataType(), e);
		}
	}
	public static DataType lookupByDatatype(gov.nist.secauto.metaschema.model.DataType type) {
		return datatypeMap.get(type);
	}
	private static final String DATATYPE_PACKAGE_NAME = "gov.nist.secauto.metaschema.datatype";


	private final gov.nist.secauto.metaschema.model.DataType dataType;
	private final String javaTypePackage;
	private final String javaType;
	private final boolean importType;

	private DataType(gov.nist.secauto.metaschema.model.DataType dataType, String javaTypePackage, String javaType, boolean importType) {
		this.dataType = dataType;
		this.javaTypePackage = javaTypePackage;
		this.javaType = javaType;
		this.importType = importType;
	}

	public gov.nist.secauto.metaschema.model.DataType getDataType() {
		return dataType;
	}

	public String getJavaType() {
		return javaType;
	}

	protected String getJavaTypePackage() {
		return javaTypePackage;
	}

	public boolean isImportType() {
		return importType;
	}

	public String getImport() {
		String retval = null;
		if (isImportType()) {
			retval = DATATYPE_PACKAGE_NAME+"."+getJavaType();
		}
		return retval;
	}
}
