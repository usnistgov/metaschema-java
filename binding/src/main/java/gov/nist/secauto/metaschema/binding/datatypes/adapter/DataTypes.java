package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
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
import gov.nist.secauto.metaschema.datatype.NCName;
import gov.nist.secauto.metaschema.datatype.NonNegativeInteger;
import gov.nist.secauto.metaschema.datatype.PositiveInteger;
import gov.nist.secauto.metaschema.datatype.URI;
import gov.nist.secauto.metaschema.datatype.URIReference;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;

public enum DataTypes {
	NCNAME(NCName.class, new NcNameAdapter()),
	DECIMAL(Decimal.class, new DecimalAdapter()),
	INTEGER(Integer.class, new IntegerAdapter()),
	NON_NEGATIVE_INTEGER(NonNegativeInteger.class, new NegativeIntegerAdapter()),
	POSITIVE_INTEGER(PositiveInteger.class, new PositiveIntegerAdapter()),
	DATE(Date.class, new DateAdapter()),
	DATE_TIME(DateTime.class, new DateTimeAdapter()),
	DATE_WITH_TZ(DateTimeZone.class, new DateWithTZAdapter()),
	DATE_TIME_WITH_TZ(DateTimeTimeZone.class, new DateTimeWithTZAdapter()),
	BASE64(Base64.class, new Base64Adapter()),
	EMAIL_ADDRESS(EmailAddress.class, new EmailAddressAdapter()),
	HOSTNAME(Hostname.class, new HostnameAdapter()),
	IP_V4_ADDRESS(IPv4.class, new Ipv4AddressAdapter()),
	IP_V6_ADDRESS(IPv6.class, new IPv6AddressAdapter()),
	URI(URI.class, new UriAdapter()),
	URI_REFERENCE(URIReference.class, new UriReferenceAdapter()),
	MARKUP_LINE(MarkupLine.class, new MarkupLineAdapter()),
	MARKUP_MULTILINE(MarkupMultiline.class, new MarkupMultilineAdapter()),
	EMPTY(Void.class, null),
	BOOLEAN(Boolean.class, new BooleanAdapter()),
	STRING(String.class, new StringAdapter());

	private final Class<?> javaClass;
	private final JavaTypeAdapter<?> javaTypeAdapter;

	private DataTypes(Class<?> javaClass, JavaTypeAdapter<?> javaTypeAdapter) {
		this.javaClass = javaClass;
		this.javaTypeAdapter = javaTypeAdapter;
	}

	public Class<?> getJavaClass() {
		return javaClass;
	}

	public JavaTypeAdapter<?> getJavaTypeAdapter() {
		return javaTypeAdapter;
	}

}
