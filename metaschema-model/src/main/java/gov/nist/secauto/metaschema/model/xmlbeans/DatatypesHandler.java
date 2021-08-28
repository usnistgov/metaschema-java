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

package gov.nist.secauto.metaschema.model.xmlbeans;

import gov.nist.secauto.metaschema.datatypes.DataTypes;

import org.apache.xmlbeans.SimpleValue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DatatypesHandler {
  private static final EnumMap<DataTypes, String> dataTypeToNameMap;
  private static final Map<String, DataTypes> nameToDataTypeMap;

  static {
    dataTypeToNameMap = new EnumMap<DataTypes, String>(DataTypes.class);
    dataTypeToNameMap.put(DataTypes.BOOLEAN, "boolean");
    dataTypeToNameMap.put(DataTypes.STRING, "string");
    dataTypeToNameMap.put(DataTypes.NCNAME, "NCName");
    dataTypeToNameMap.put(DataTypes.TOKEN, "token");
    dataTypeToNameMap.put(DataTypes.DECIMAL, "decimal");
    dataTypeToNameMap.put(DataTypes.INTEGER, "integer");
    dataTypeToNameMap.put(DataTypes.NON_NEGATIVE_INTEGER, "nonNegativeInteger");
    dataTypeToNameMap.put(DataTypes.POSITIVE_INTEGER, "positiveInteger");
    dataTypeToNameMap.put(DataTypes.DATE_TIME, "dateTime");
    dataTypeToNameMap.put(DataTypes.DATE, "date");
    dataTypeToNameMap.put(DataTypes.BASE64, "base64Binary");
    dataTypeToNameMap.put(DataTypes.DATE_TIME_WITH_TZ, "dateTime-with-timezone");
    dataTypeToNameMap.put(DataTypes.DATE_WITH_TZ, "date-with-timezone");
    dataTypeToNameMap.put(DataTypes.EMAIL_ADDRESS, "email");
    dataTypeToNameMap.put(DataTypes.HOSTNAME, "hostname");
    dataTypeToNameMap.put(DataTypes.IP_V4_ADDRESS, "ip-v4-address");
    dataTypeToNameMap.put(DataTypes.IP_V6_ADDRESS, "ip-v6-address");
    dataTypeToNameMap.put(DataTypes.URI, "uri");
    dataTypeToNameMap.put(DataTypes.URI_REFERENCE, "uri-reference");
    dataTypeToNameMap.put(DataTypes.UUID, "uuid");
    dataTypeToNameMap.put(DataTypes.MARKUP_LINE, "markup-line");
    dataTypeToNameMap.put(DataTypes.MARKUP_MULTILINE, "markup-multiline");

    nameToDataTypeMap = new HashMap<String, DataTypes>();
    for (Map.Entry<DataTypes, String> entry : dataTypeToNameMap.entrySet()) {
      DataTypes dataType = entry.getKey();
      String name = entry.getValue();

      nameToDataTypeMap.put(name, dataType);
    }
  }

  public static DataTypes decodeFieldDatatypesType(SimpleValue target) {
    return decode(target);
  }

  public static void encodeFieldDatatypesType(DataTypes asType, SimpleValue target) {
    encode(asType, target);
  }

  public static DataTypes decodeSimpleDatatypesType(SimpleValue target) {
    return decode(target);
  }

  public static void encodeSimpleDatatypesType(DataTypes asType, SimpleValue target) {
    encode(asType, target);
  }

  private static void encode(DataTypes asType, SimpleValue target) {
    if (asType != null) {
      String value = dataTypeToNameMap.get(asType);
      if (value == null) {
        throw new RuntimeException(String.format("Unknown data type '%s'", asType.name()));
      }
      target.setStringValue(value);
    }
  }

  private static DataTypes decode(SimpleValue target) {
    String name = target.getStringValue();
    DataTypes retval = nameToDataTypeMap.get(name);
    if (retval == null) {
      throw new RuntimeException(String.format("Unknown data type name '%s'", name));
    }
    return retval;
  }
}
