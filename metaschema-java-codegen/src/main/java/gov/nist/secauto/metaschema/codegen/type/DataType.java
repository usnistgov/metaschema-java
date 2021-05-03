/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.codegen.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.datatypes.Base64;
import gov.nist.secauto.metaschema.datatypes.Date;
import gov.nist.secauto.metaschema.datatypes.DateTime;
import gov.nist.secauto.metaschema.datatypes.IPv4;
import gov.nist.secauto.metaschema.datatypes.IPv6;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.Base64Adapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.BooleanAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.DateAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.DateTimeAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.DateTimeWithTZAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.DateWithTZAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.DecimalAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.EmailAddressAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.HostnameAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.IPv6AddressAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.IntegerAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.Ipv4AddressAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.MarkupLineAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.MarkupMultilineAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.NcNameAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.NegativeIntegerAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.PositiveIntegerAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.StringAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.UriAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.UriReferenceAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.UuidAdapter;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum DataType {
  NCNAME(
      gov.nist.secauto.metaschema.model.definitions.DataType.NCNAME,
      String.class,
      NcNameAdapter.class),
  DECIMAL(
      gov.nist.secauto.metaschema.model.definitions.DataType.DECIMAL,
      BigDecimal.class,
      DecimalAdapter.class),
  INTEGER(
      gov.nist.secauto.metaschema.model.definitions.DataType.INTEGER,
      Integer.class,
      IntegerAdapter.class),
  NON_NEGATIVE_INTEGER(
      gov.nist.secauto.metaschema.model.definitions.DataType.NON_NEGATIVE_INTEGER,
      BigInteger.class,
      NegativeIntegerAdapter.class),
  POSITIVE_INTEGER(
      gov.nist.secauto.metaschema.model.definitions.DataType.POSITIVE_INTEGER,
      BigInteger.class,
      PositiveIntegerAdapter.class),
  DATE(
      gov.nist.secauto.metaschema.model.definitions.DataType.DATE,
      Date.class,
      DateAdapter.class),
  DATE_TIME(
      gov.nist.secauto.metaschema.model.definitions.DataType.DATE_TIME,
      DateTime.class,
      DateTimeAdapter.class),
  DATE_WITH_TZ(
      gov.nist.secauto.metaschema.model.definitions.DataType.DATE_WITH_TZ,
      ZonedDateTime.class,
      DateWithTZAdapter.class),
  DATE_TIME_WITH_TZ(
      gov.nist.secauto.metaschema.model.definitions.DataType.DATE_TIME_WITH_TZ,
      ZonedDateTime.class,
      DateTimeWithTZAdapter.class),
  BASE64(
      gov.nist.secauto.metaschema.model.definitions.DataType.BASE64,
      Base64.class,
      Base64Adapter.class),
  EMAIL_ADDRESS(
      gov.nist.secauto.metaschema.model.definitions.DataType.EMAIL_ADDRESS,
      String.class,
      EmailAddressAdapter.class),
  HOSTNAME(
      gov.nist.secauto.metaschema.model.definitions.DataType.HOSTNAME,
      String.class,
      HostnameAdapter.class),
  IP_V4_ADDRESS(
      gov.nist.secauto.metaschema.model.definitions.DataType.IP_V4_ADDRESS,
      IPv4.class,
      Ipv4AddressAdapter.class),
  IP_V6_ADDRESS(
      gov.nist.secauto.metaschema.model.definitions.DataType.IP_V6_ADDRESS,
      IPv6.class,
      IPv6AddressAdapter.class),
  URI(
      gov.nist.secauto.metaschema.model.definitions.DataType.URI,
      URI.class,
      UriAdapter.class),
  URI_REFERENCE(
      gov.nist.secauto.metaschema.model.definitions.DataType.URI_REFERENCE,
      URI.class,
      UriReferenceAdapter.class),
  UUID(
      gov.nist.secauto.metaschema.model.definitions.DataType.UUID,
      UUID.class,
      UuidAdapter.class),
  MARKUP_LINE(
      gov.nist.secauto.metaschema.model.definitions.DataType.MARKUP_LINE,
      MarkupLine.class,
      MarkupLineAdapter.class),
  MARKUP_MULTILINE(
      gov.nist.secauto.metaschema.model.definitions.DataType.MARKUP_MULTILINE,
      MarkupMultiline.class,
      MarkupMultilineAdapter.class),
  BOOLEAN(
      gov.nist.secauto.metaschema.model.definitions.DataType.BOOLEAN,
      Boolean.class,
      BooleanAdapter.class),
  STRING(
      gov.nist.secauto.metaschema.model.definitions.DataType.STRING,
      String.class,
      StringAdapter.class);

  private static final Map<gov.nist.secauto.metaschema.model.definitions.DataType, DataType> datatypeMap;

  static {
    datatypeMap = new HashMap<>();
    for (DataType e : values()) {
      datatypeMap.put(e.getDataType(), e);
    }
  }

  public static DataType lookupByDatatype(gov.nist.secauto.metaschema.model.definitions.DataType type) {
    return datatypeMap.get(type);
  }

  private final gov.nist.secauto.metaschema.model.definitions.DataType dataType;
  private final Class<?> javaClass;
  private final TypeName typeName;
  private final Class<? extends JavaTypeAdapter<?>> javaTypeAdapterClass;

  private DataType(gov.nist.secauto.metaschema.model.definitions.DataType dataType,
      Class<?> javaClass,
      Class<? extends JavaTypeAdapter<?>> javaTypeAdapterClass) {
    this.dataType = dataType;
    this.javaClass = javaClass;
    this.typeName = ClassName.get(getJavaClass());
    this.javaTypeAdapterClass = javaTypeAdapterClass;
  }

  public gov.nist.secauto.metaschema.model.definitions.DataType getDataType() {
    return dataType;
  }

  protected Class<?> getJavaClass() {
    return javaClass;
  }

  public TypeName getTypeName() {
    return typeName;
  }

  public Class<? extends JavaTypeAdapter<?>> getJavaTypeAdapterClass() {
    return javaTypeAdapterClass;
  }
}
