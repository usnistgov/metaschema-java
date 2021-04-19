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

package gov.nist.secauto.metaschema.model.definitions;

import gov.nist.itl.metaschema.model.m4.xml.FieldTypes;
import gov.nist.itl.metaschema.model.m4.xml.SimpleDatatypes;

import java.util.HashMap;
import java.util.Map;

public enum DataType {
  BOOLEAN(SimpleDatatypes.BOOLEAN.toString(), "STRVALUE"),
  STRING(SimpleDatatypes.STRING.toString(), "STRVALUE"),
  NCNAME(SimpleDatatypes.NC_NAME.toString(), "STRVALUE"),
  DECIMAL(SimpleDatatypes.DECIMAL.toString(), "STRVALUE"),
  INTEGER(SimpleDatatypes.INTEGER.toString(), "STRVALUE"),
  NON_NEGATIVE_INTEGER(SimpleDatatypes.NON_NEGATIVE_INTEGER.toString(), "STRVALUE"),
  POSITIVE_INTEGER(SimpleDatatypes.POSITIVE_INTEGER.toString(), "STRVALUE"),
  DATE(SimpleDatatypes.DATE.toString(), "STRVALUE"),
  DATE_TIME(SimpleDatatypes.DATE_TIME.toString(), "STRVALUE"),
  DATE_WITH_TZ(SimpleDatatypes.DATE_WITH_TIMEZONE.toString(), "STRVALUE"),
  DATE_TIME_WITH_TZ(SimpleDatatypes.DATE_TIME_WITH_TIMEZONE.toString(), "STRVALUE"),
  BASE64(SimpleDatatypes.BASE_64_BINARY.toString(), "STRVALUE"),
  EMAIL_ADDRESS(SimpleDatatypes.EMAIL.toString(), "STRVALUE"),
  HOSTNAME(SimpleDatatypes.HOSTNAME.toString(), "STRVALUE"),
  IP_V4_ADDRESS(SimpleDatatypes.IP_V_4_ADDRESS.toString(), "STRVALUE"),
  IP_V6_ADDRESS(SimpleDatatypes.IP_V_6_ADDRESS.toString(), "STRVALUE"),
  URI(SimpleDatatypes.URI.toString(), "STRVALUE"),
  URI_REFERENCE(SimpleDatatypes.URI_REFERENCE.toString(), "STRVALUE"),
  UUID(SimpleDatatypes.UUID.toString(), "STRVALUE"),
  MARKUP_LINE(FieldTypes.Member.MARKUP_LINE.toString(), "RICHTEXT"),
  MARKUP_MULTILINE(FieldTypes.Member.MARKUP_MULTILINE.toString(), "PROSE"),
  EMPTY(FieldTypes.Member.EMPTY.toString(), null);

  private static final Map<String, DataType> nameToEnumMap;

  static {
    nameToEnumMap = new HashMap<>();
    for (DataType e : values()) {
      nameToEnumMap.put(e.getName(), e);
    }
  }

  /**
   * Lookup the data type using a data type's name.
   * 
   * @param name
   *          the name of the data type
   * @return the matching data type, or {@code null} if a match was not found
   */
  public static DataType lookup(String name) {
    return nameToEnumMap.get(name);
  }

  /**
   * Lookup the data type using the bound XML schema emum value.
   * 
   * @param xmlEnum
   *          the bound XML schema emum value
   * @return the matching data type, or {@code null} if a match was not found
   */
  public static DataType lookup(SimpleDatatypes.Enum xmlEnum) {
    return nameToEnumMap.get(xmlEnum.toString());
  }

  private final String name;
  private final String valueKey;

  DataType(String name, String valueKey) {
    this.name = name;
    this.valueKey = valueKey;
  }

  /**
   * Retrieve the data type's name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieve the data type's default value key.
   * 
   * @return the value key
   */
  public String getDefaultValueKey() {
    return valueKey;
  }
}
