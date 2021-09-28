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

package gov.nist.secauto.metaschema.datatypes.adapter.types;

import gov.nist.secauto.metaschema.model.common.datatype.AbstractDataTypeProvider;

public class MetaschemaDataTypeProvider extends AbstractDataTypeProvider {
  public static final NcNameAdapter NCNAME = new NcNameAdapter();
  public static final TokenAdapter TOKEN = new TokenAdapter();
  public static final DecimalAdapter DECIMAL = new DecimalAdapter();
  public static final IntegerAdapter INTEGER = new IntegerAdapter();
  public static final NonNegativeIntegerAdapter NON_NEGATIVE_INTEGER = new NonNegativeIntegerAdapter();
  public static final PositiveIntegerAdapter POSITIVE_INTEGER = new PositiveIntegerAdapter();
  public static final DateAdapter DATE = new DateAdapter();
  public static final DateTimeAdapter DATE_TIME = new DateTimeAdapter();
  public static final DateWithTZAdapter DATE_WITH_TZ = new DateWithTZAdapter();
  public static final DateTimeWithTZAdapter DATE_TIME_WITH_TZ = new DateTimeWithTZAdapter();
  public static final Base64Adapter BASE64 = new Base64Adapter();
  public static final EmailAddressAdapter EMAIL_ADDRESS = new EmailAddressAdapter();
  public static final HostnameAdapter HOSTNAME = new HostnameAdapter();
  public static final IPv4AddressAdapter IP_V4_ADDRESS = new IPv4AddressAdapter();
  public static final IPv6AddressAdapter IP_V6_ADDRESS = new IPv6AddressAdapter();
  public static final UriAdapter URI = new UriAdapter();
  public static final UriReferenceAdapter URI_REFERENCE = new UriReferenceAdapter();
  public static final UuidAdapter UUID = new UuidAdapter();
  public static final MarkupLineAdapter MARKUP_LINE = new MarkupLineAdapter();
  public static final MarkupMultilineAdapter MARKUP_MULTILINE = new MarkupMultilineAdapter();
  public static final BooleanAdapter BOOLEAN = new BooleanAdapter();
  public static final StringAdapter STRING = new StringAdapter();

  public static final StringAdapter DEFAULT_DATA_TYPE = STRING;
  
  public MetaschemaDataTypeProvider() {
    registerJavaTypeAdapter(NCNAME);
    registerJavaTypeAdapter(TOKEN);
    registerJavaTypeAdapter(DECIMAL);
    registerJavaTypeAdapter(INTEGER);
    registerJavaTypeAdapter(NON_NEGATIVE_INTEGER);
    registerJavaTypeAdapter(POSITIVE_INTEGER);
    registerJavaTypeAdapter(DATE);
    registerJavaTypeAdapter(DATE_TIME);
    registerJavaTypeAdapter(DATE_WITH_TZ);
    registerJavaTypeAdapter(DATE_TIME_WITH_TZ);
    registerJavaTypeAdapter(BASE64);
    registerJavaTypeAdapter(EMAIL_ADDRESS);
    registerJavaTypeAdapter(HOSTNAME);
    registerJavaTypeAdapter(IP_V4_ADDRESS);
    registerJavaTypeAdapter(IP_V6_ADDRESS);
    registerJavaTypeAdapter(URI);
    registerJavaTypeAdapter(URI_REFERENCE);
    registerJavaTypeAdapter(UUID);
    registerJavaTypeAdapter(MARKUP_LINE);
    registerJavaTypeAdapter(MARKUP_MULTILINE);
    registerJavaTypeAdapter(BOOLEAN);
    registerJavaTypeAdapter(STRING);
  }
}
