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

package gov.nist.secauto.metaschema.core.datatype.adapter;

import com.google.auto.service.AutoService;

import gov.nist.secauto.metaschema.core.datatype.AbstractDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeProvider;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides for runtime discovery of built-in implementations of the core
 * Metaschema data types.
 */
@AutoService(IDataTypeProvider.class)
public final class MetaschemaDataTypeProvider // NOPMD - Used for service initialization
    extends AbstractDataTypeProvider {
  @NonNull
  public static final Base64Adapter BASE64 = new Base64Adapter();
  @NonNull
  public static final BooleanAdapter BOOLEAN = new BooleanAdapter();
  @NonNull
  public static final DateAdapter DATE = new DateAdapter();
  @NonNull
  public static final DateWithTZAdapter DATE_WITH_TZ = new DateWithTZAdapter();
  @NonNull
  public static final DateTimeAdapter DATE_TIME = new DateTimeAdapter();
  @NonNull
  public static final DateTimeWithTZAdapter DATE_TIME_WITH_TZ = new DateTimeWithTZAdapter();
  @NonNull
  public static final IPv4AddressAdapter IP_V4_ADDRESS = new IPv4AddressAdapter();
  @NonNull
  public static final IPv6AddressAdapter IP_V6_ADDRESS = new IPv6AddressAdapter();
  @NonNull
  public static final UriAdapter URI = new UriAdapter();
  @NonNull
  public static final UriReferenceAdapter URI_REFERENCE = new UriReferenceAdapter();
  @NonNull
  public static final UuidAdapter UUID = new UuidAdapter();

  @NonNull
  public static final DayTimeAdapter DAY_TIME_DURATION = new DayTimeAdapter();
  @NonNull
  public static final YearMonthAdapter YEAR_MONTH_DURATION = new YearMonthAdapter();

  @NonNull
  public static final DecimalAdapter DECIMAL = new DecimalAdapter();
  @NonNull
  public static final IntegerAdapter INTEGER = new IntegerAdapter();
  @NonNull
  public static final NonNegativeIntegerAdapter NON_NEGATIVE_INTEGER = new NonNegativeIntegerAdapter();
  @NonNull
  public static final PositiveIntegerAdapter POSITIVE_INTEGER = new PositiveIntegerAdapter();

  @NonNull
  public static final EmailAddressAdapter EMAIL_ADDRESS = new EmailAddressAdapter();
  @NonNull
  public static final HostnameAdapter HOSTNAME = new HostnameAdapter();
  @Deprecated(forRemoval = true, since = "0.7.0")
  @NonNull
  public static final NcNameAdapter NCNAME = new NcNameAdapter();
  @NonNull
  public static final StringAdapter STRING = new StringAdapter();
  @NonNull
  public static final TokenAdapter TOKEN = new TokenAdapter();

  @NonNull
  public static final StringAdapter DEFAULT_DATA_TYPE = STRING;

  /**
   * Initialize the built-in data types.
   */
  public MetaschemaDataTypeProvider() {
    // The data type "string" must be first since this is the default data type for
    // the {@link String}
    // Java type. This ensures that when a data type is resolved that this data type
    // is matched first
    // before other String-based data types.
    registerDatatype(STRING);

    registerDatatype(BASE64);
    registerDatatype(BOOLEAN);
    registerDatatype(DATE);
    registerDatatype(DATE_WITH_TZ);
    registerDatatype(DATE_TIME);
    registerDatatype(DATE_TIME_WITH_TZ);
    registerDatatype(DAY_TIME_DURATION);
    registerDatatype(DECIMAL);
    registerDatatype(EMAIL_ADDRESS);
    registerDatatype(HOSTNAME);
    registerDatatype(INTEGER);
    registerDatatype(IP_V4_ADDRESS);
    registerDatatype(IP_V6_ADDRESS);

    registerDatatype(NON_NEGATIVE_INTEGER);
    registerDatatype(POSITIVE_INTEGER);
    registerDatatype(TOKEN);
    registerDatatype(URI);
    registerDatatype(URI_REFERENCE);
    registerDatatype(UUID);
    registerDatatype(YEAR_MONTH_DURATION);
  }
}
