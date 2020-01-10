package gov.nist.secauto.metaschema.codegen.type;

import java.util.HashMap;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.datatypes.Base64;
import gov.nist.secauto.metaschema.binding.datatypes.Date;
import gov.nist.secauto.metaschema.binding.datatypes.DateTime;
import gov.nist.secauto.metaschema.binding.datatypes.DateTimeTimeZone;
import gov.nist.secauto.metaschema.binding.datatypes.DateTimeZone;
import gov.nist.secauto.metaschema.binding.datatypes.Decimal;
import gov.nist.secauto.metaschema.binding.datatypes.EmailAddress;
import gov.nist.secauto.metaschema.binding.datatypes.Hostname;
import gov.nist.secauto.metaschema.binding.datatypes.IPv4;
import gov.nist.secauto.metaschema.binding.datatypes.IPv6;
import gov.nist.secauto.metaschema.binding.datatypes.Integer;
import gov.nist.secauto.metaschema.binding.datatypes.NCName;
import gov.nist.secauto.metaschema.binding.datatypes.NonNegativeInteger;
import gov.nist.secauto.metaschema.binding.datatypes.PositiveInteger;
import gov.nist.secauto.metaschema.binding.datatypes.URI;
import gov.nist.secauto.metaschema.binding.datatypes.URIReference;
import gov.nist.secauto.metaschema.markup.MarkupLine;
import gov.nist.secauto.metaschema.markup.MarkupMultiline;

public enum DataType {
	NCNAME(gov.nist.secauto.metaschema.model.DataType.NCNAME, NCName.class),
	DECIMAL(gov.nist.secauto.metaschema.model.DataType.DECIMAL, Decimal.class),
	INTEGER(gov.nist.secauto.metaschema.model.DataType.INTEGER, Integer.class),
	NON_NEGATIVE_INTEGER(gov.nist.secauto.metaschema.model.DataType.NON_NEGATIVE_INTEGER, NonNegativeInteger.class),
	POSITIVE_INTEGER(gov.nist.secauto.metaschema.model.DataType.POSITIVE_INTEGER, PositiveInteger.class),
	DATE(gov.nist.secauto.metaschema.model.DataType.DATE, Date.class),
	DATE_TIME(gov.nist.secauto.metaschema.model.DataType.DATE_TIME, DateTime.class),
	DATE_WITH_TZ(gov.nist.secauto.metaschema.model.DataType.DATE_WITH_TZ, DateTimeZone.class),
	DATE_TIME_WITH_TZ(gov.nist.secauto.metaschema.model.DataType.DATE_TIME_WITH_TZ, DateTimeTimeZone.class),
	BASE64(gov.nist.secauto.metaschema.model.DataType.BASE64, Base64.class),
	EMAIL_ADDRESS(gov.nist.secauto.metaschema.model.DataType.EMAIL_ADDRESS, EmailAddress.class),
	HOSTNAME(gov.nist.secauto.metaschema.model.DataType.HOSTNAME, Hostname.class),
	IP_V4_ADDRESS(gov.nist.secauto.metaschema.model.DataType.IP_V4_ADDRESS, IPv4.class),
	IP_V6_ADDRESS(gov.nist.secauto.metaschema.model.DataType.IP_V6_ADDRESS, IPv6.class),
	URI(gov.nist.secauto.metaschema.model.DataType.URI, URI.class),
	URI_REFERENCE(gov.nist.secauto.metaschema.model.DataType.URI_REFERENCE, URIReference.class),
	MARKUP_LINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_LINE, MarkupLine.class),
	MARKUP_MULTILINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_MULTILINE, MarkupMultiline.class),
	EMPTY(gov.nist.secauto.metaschema.model.DataType.EMPTY, Void.class),
	BOOLEAN(gov.nist.secauto.metaschema.model.DataType.BOOLEAN, Boolean.class),
	STRING(gov.nist.secauto.metaschema.model.DataType.STRING, String.class);

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

	private final gov.nist.secauto.metaschema.model.DataType dataType;
	private final Class<?> javaClass;
	private final ClassJavaType javaType;

	private DataType(gov.nist.secauto.metaschema.model.DataType dataType, Class<?> javaClass) {
		this.dataType = dataType;
		this.javaClass = javaClass;
		this.javaType = new ClassJavaType(getJavaClass());
	}

	public gov.nist.secauto.metaschema.model.DataType getDataType() {
		return dataType;
	}

	protected Class<?> getJavaClass() {
		return javaClass;
	}

	public ClassJavaType getJavaType() {
		return javaType;
	}
}
