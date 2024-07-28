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

package gov.nist.secauto.metaschema.schemagen.datatype;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractDatatypeManager implements IDatatypeManager {
  @NonNull
  private static final Map<String, String> DATATYPE_TRANSLATION_MAP // NOPMD - intentional
      = new LinkedHashMap<>();

  static {
    DATATYPE_TRANSLATION_MAP.put("base64", "Base64Datatype");
    DATATYPE_TRANSLATION_MAP.put("boolean", "BooleanDatatype");
    DATATYPE_TRANSLATION_MAP.put("date", "DateDatatype");
    DATATYPE_TRANSLATION_MAP.put("date-with-timezone", "DateWithTimezoneDatatype");
    DATATYPE_TRANSLATION_MAP.put("date-time", "DateTimeDatatype");
    DATATYPE_TRANSLATION_MAP.put("date-time-with-timezone", "DateTimeWithTimezoneDatatype");
    DATATYPE_TRANSLATION_MAP.put("day-time-duration", "DayTimeDurationDatatype");
    DATATYPE_TRANSLATION_MAP.put("decimal", "DecimalDatatype");
    DATATYPE_TRANSLATION_MAP.put("email-address", "EmailAddressDatatype");
    DATATYPE_TRANSLATION_MAP.put("hostname", "HostnameDatatype");
    DATATYPE_TRANSLATION_MAP.put("integer", "IntegerDatatype");
    DATATYPE_TRANSLATION_MAP.put("ip-v4-address", "IPV4AddressDatatype");
    DATATYPE_TRANSLATION_MAP.put("ip-v6-address", "IPV6AddressDatatype");
    DATATYPE_TRANSLATION_MAP.put("markup-line", "MarkupLineDatatype");
    DATATYPE_TRANSLATION_MAP.put("markup-multiline", "MarkupMultilineDatatype");
    DATATYPE_TRANSLATION_MAP.put("non-negative-integer", "NonNegativeIntegerDatatype");
    DATATYPE_TRANSLATION_MAP.put("positive-integer", "PositiveIntegerDatatype");
    DATATYPE_TRANSLATION_MAP.put("string", "StringDatatype");
    DATATYPE_TRANSLATION_MAP.put("token", "TokenDatatype");
    DATATYPE_TRANSLATION_MAP.put("uri", "URIDatatype");
    DATATYPE_TRANSLATION_MAP.put("uri-reference", "URIReferenceDatatype");
    DATATYPE_TRANSLATION_MAP.put("uuid", "UUIDDatatype");
    DATATYPE_TRANSLATION_MAP.put("year-month-duration", "YearMonthDurationDatatype");
  }

  @NonNull
  private final Map<IDataTypeAdapter<?>, String> datatypeToTypeMap = new HashMap<>(); // NOPMD - intentional

  @SuppressWarnings("null")
  @NonNull
  protected static Map<String, String> getDatatypeTranslationMap() {
    return Collections.unmodifiableMap(DATATYPE_TRANSLATION_MAP);
  }

  @Override
  public Set<String> getUsedTypes() {
    return new HashSet<>(datatypeToTypeMap.values());
  }

  @SuppressWarnings("null")
  @Override
  @NonNull
  public String getTypeNameForDatatype(@NonNull IDataTypeAdapter<?> datatype) {
    synchronized (this) {
      String name = datatypeToTypeMap.get(datatype);
      if (name == null) {
        name = getDatatypeTranslationMap().get(datatype.getPreferredName().getLocalPart());
        datatypeToTypeMap.put(datatype, name);
      }
      return name;
    }
  }
}
