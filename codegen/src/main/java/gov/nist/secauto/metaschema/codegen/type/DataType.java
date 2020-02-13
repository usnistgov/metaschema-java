/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.codegen.type;

import java.util.HashMap;
import java.util.Map;

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

public enum DataType {
	NCNAME(gov.nist.secauto.metaschema.model.info.definitions.DataType.NCNAME, NCName.class),
	DECIMAL(gov.nist.secauto.metaschema.model.info.definitions.DataType.DECIMAL, Decimal.class),
	INTEGER(gov.nist.secauto.metaschema.model.info.definitions.DataType.INTEGER, Integer.class),
	NON_NEGATIVE_INTEGER(gov.nist.secauto.metaschema.model.info.definitions.DataType.NON_NEGATIVE_INTEGER, NonNegativeInteger.class),
	POSITIVE_INTEGER(gov.nist.secauto.metaschema.model.info.definitions.DataType.POSITIVE_INTEGER, PositiveInteger.class),
	DATE(gov.nist.secauto.metaschema.model.info.definitions.DataType.DATE, Date.class),
	DATE_TIME(gov.nist.secauto.metaschema.model.info.definitions.DataType.DATE_TIME, DateTime.class),
	DATE_WITH_TZ(gov.nist.secauto.metaschema.model.info.definitions.DataType.DATE_WITH_TZ, DateTimeZone.class),
	DATE_TIME_WITH_TZ(gov.nist.secauto.metaschema.model.info.definitions.DataType.DATE_TIME_WITH_TZ, DateTimeTimeZone.class),
	BASE64(gov.nist.secauto.metaschema.model.info.definitions.DataType.BASE64, Base64.class),
	EMAIL_ADDRESS(gov.nist.secauto.metaschema.model.info.definitions.DataType.EMAIL_ADDRESS, EmailAddress.class),
	HOSTNAME(gov.nist.secauto.metaschema.model.info.definitions.DataType.HOSTNAME, Hostname.class),
	IP_V4_ADDRESS(gov.nist.secauto.metaschema.model.info.definitions.DataType.IP_V4_ADDRESS, IPv4.class),
	IP_V6_ADDRESS(gov.nist.secauto.metaschema.model.info.definitions.DataType.IP_V6_ADDRESS, IPv6.class),
	URI(gov.nist.secauto.metaschema.model.info.definitions.DataType.URI, URI.class),
	URI_REFERENCE(gov.nist.secauto.metaschema.model.info.definitions.DataType.URI_REFERENCE, URIReference.class),
	MARKUP_LINE(gov.nist.secauto.metaschema.model.info.definitions.DataType.MARKUP_LINE, MarkupLine.class),
	MARKUP_MULTILINE(gov.nist.secauto.metaschema.model.info.definitions.DataType.MARKUP_MULTILINE, MarkupMultiline.class),
	EMPTY(gov.nist.secauto.metaschema.model.info.definitions.DataType.EMPTY, Void.class),
	BOOLEAN(gov.nist.secauto.metaschema.model.info.definitions.DataType.BOOLEAN, Boolean.class),
	STRING(gov.nist.secauto.metaschema.model.info.definitions.DataType.STRING, String.class);

	private static final Map<gov.nist.secauto.metaschema.model.info.definitions.DataType, DataType> datatypeMap;

	static {
		datatypeMap = new HashMap<>();
		for (DataType e : values()) {
			datatypeMap.put(e.getDataType(), e);
		}
	}
	public static DataType lookupByDatatype(gov.nist.secauto.metaschema.model.info.definitions.DataType type) {
		return datatypeMap.get(type);
	}

	private final gov.nist.secauto.metaschema.model.info.definitions.DataType dataType;
	private final Class<?> javaClass;
	private final ClassJavaType javaType;

	private DataType(gov.nist.secauto.metaschema.model.info.definitions.DataType dataType, Class<?> javaClass) {
		this.dataType = dataType;
		this.javaClass = javaClass;
		this.javaType = new ClassJavaType(getJavaClass());
	}

	public gov.nist.secauto.metaschema.model.info.definitions.DataType getDataType() {
		return dataType;
	}

	protected Class<?> getJavaClass() {
		return javaClass;
	}

	public ClassJavaType getJavaType() {
		return javaType;
	}
}
