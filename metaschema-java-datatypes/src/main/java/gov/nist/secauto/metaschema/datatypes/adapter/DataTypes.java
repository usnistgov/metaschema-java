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

package gov.nist.secauto.metaschema.datatypes.adapter;

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
import gov.nist.secauto.metaschema.datatypes.types.Base64;
import gov.nist.secauto.metaschema.datatypes.types.Date;
import gov.nist.secauto.metaschema.datatypes.types.DateTime;
import gov.nist.secauto.metaschema.datatypes.types.IPv4;
import gov.nist.secauto.metaschema.datatypes.types.IPv6;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupMultiline;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Provides an enumeration of all data types supported by this library. The Java type and
 * {@link JavaTypeAdapter} implementation are provided for each type.
 */
public enum DataTypes {
  NCNAME(String.class, new NcNameAdapter()),
  DECIMAL(BigDecimal.class, new DecimalAdapter()),
  INTEGER(BigInteger.class, new IntegerAdapter()),
  NON_NEGATIVE_INTEGER(BigInteger.class, new NegativeIntegerAdapter()),
  POSITIVE_INTEGER(BigInteger.class, new PositiveIntegerAdapter()),
  DATE(Date.class, new DateAdapter()),
  DATE_TIME(DateTime.class, new DateTimeAdapter()),
  DATE_WITH_TZ(ZonedDateTime.class, new DateWithTZAdapter()),
  DATE_TIME_WITH_TZ(ZonedDateTime.class, new DateTimeWithTZAdapter()),
  BASE64(Base64.class, new Base64Adapter()),
  EMAIL_ADDRESS(String.class, new EmailAddressAdapter()),
  HOSTNAME(String.class, new HostnameAdapter()),
  IP_V4_ADDRESS(IPv4.class, new Ipv4AddressAdapter()),
  IP_V6_ADDRESS(IPv6.class, new IPv6AddressAdapter()),
  URI(URI.class, new UriAdapter()),
  URI_REFERENCE(URI.class, new UriReferenceAdapter()),
  UUID(UUID.class, new UuidAdapter()),
  MARKUP_LINE(MarkupLine.class, new MarkupLineAdapter()),
  MARKUP_MULTILINE(MarkupMultiline.class, new MarkupMultilineAdapter()),
  EMPTY(Void.class, null),
  BOOLEAN(Boolean.class, new BooleanAdapter()),
  STRING(String.class, new StringAdapter());

  private final Class<?> javaClass;
  private final JavaTypeAdapter<?> javaTypeAdapter;

  DataTypes(Class<?> javaClass, JavaTypeAdapter<?> javaTypeAdapter) {
    this.javaClass = javaClass;
    this.javaTypeAdapter = javaTypeAdapter;
  }

  /**
   * Retrieves the Java type associated with the data type.
   * 
   * @return the class for the Java type
   */
  public Class<?> getJavaClass() {
    return javaClass;
  }

  /**
   * Get the Java type adapter for the data type.
   * 
   * @return the Java type adapter, or {@code null} if there is no associated adapter
   */
  public JavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

}
