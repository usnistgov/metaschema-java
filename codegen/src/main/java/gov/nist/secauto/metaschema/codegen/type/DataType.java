package gov.nist.secauto.metaschema.codegen.type;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

import gov.nist.secauto.metaschema.datatype.Base64;
import gov.nist.secauto.metaschema.datatype.Date;
import gov.nist.secauto.metaschema.datatype.DateTime;
import gov.nist.secauto.metaschema.datatype.DateTimeTimeZone;
import gov.nist.secauto.metaschema.datatype.DateTimeZone;
import gov.nist.secauto.metaschema.datatype.Decimal;
import gov.nist.secauto.metaschema.datatype.EmailAddress;
import gov.nist.secauto.metaschema.datatype.Hostname;
import gov.nist.secauto.metaschema.datatype.IPv4;
import gov.nist.secauto.metaschema.datatype.IPv6;
import gov.nist.secauto.metaschema.datatype.Integer;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.datatype.NCName;
import gov.nist.secauto.metaschema.datatype.NonNegativeInteger;
import gov.nist.secauto.metaschema.datatype.PositiveInteger;
import gov.nist.secauto.metaschema.datatype.URI;
import gov.nist.secauto.metaschema.datatype.URIReference;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringDeserializer;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringSerializer;

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
	MARKUP_LINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_LINE, MarkupString.class, MarkupStringSerializer.class, MarkupStringDeserializer.class),
	MARKUP_MULTILINE(gov.nist.secauto.metaschema.model.DataType.MARKUP_MULTILINE, MarkupString.class, MarkupStringSerializer.class, MarkupStringDeserializer.class),
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
	private final Class<? extends JsonSerializer<?>> jsonSerializerClass;
	private final Class<? extends JsonDeserializer<?>> jsonDeserializerClass;

	private DataType(gov.nist.secauto.metaschema.model.DataType dataType, Class<?> javaClass) {
		this(dataType, javaClass, null, null);
	}

	private DataType(gov.nist.secauto.metaschema.model.DataType dataType, Class<?> javaClass, Class<? extends JsonSerializer<?>> jsonSerializerClass, Class<? extends JsonDeserializer<?>> jsonDeserializerClass) {
		this.dataType = dataType;
		this.javaClass = javaClass;
		this.javaType = new ClassJavaType(getJavaClass());
		this.jsonSerializerClass = jsonSerializerClass;
		this.jsonDeserializerClass = jsonDeserializerClass;
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

	public Class<? extends JsonSerializer<?>> getJsonSerializerClass() {
		return jsonSerializerClass;
	}

	public Class<? extends JsonDeserializer<?>> getJsonDeserializerClass() {
		return jsonDeserializerClass;
	}
}
