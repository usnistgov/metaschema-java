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

package gov.nist.secauto.metaschema.model.common.metapath.type;

import gov.nist.secauto.metaschema.datatypes.DataTypes;

import java.util.EnumMap;

public class TypeFactory {
  public static final IBase64BinaryType BASE64_TYPE = new Base64BinaryTypeImpl();
  public static final IBooleanType BOOLEAN_TYPE = new BooleanTypeImpl();
  public static final IDateType DATE_WITHOUT_TZ_TYPE = new DateWithoutTimeZoneTypeImpl();
  public static final IDateTimeType DATETIME_WITHOUT_TZ_TYPE = new DateTimeWithoutTimeZoneTypeImpl();
  public static final IDateTimeType DATETIME_WITH_TZ_TYPE = new DateTimeWithTimeZoneTypeImpl();
  public static final IDateType DATE_WITH_TZ_TYPE = new DateWithTimeZoneTypeImpl();
  public static final IDecimalType DECIMAL_TYPE = new DecimalTypeImpl();
  public static final IEmailAddressType EMAIL_ADDRESS_TYPE = new EmailAddressTypeImpl();
  public static final IHostnameType HOSTNAME_TYPE = new HostnameTypeImpl();
  public static final IIntegerType INTEGER_TYPE = new IntegerTypeImpl();
  public static final IIPv4AddressType IP_V4_ADDRESS_TYPE = new IPv4AddressTypeImpl();
  public static final IIPv6AddressType IP_V6_ADDRESS_TYPE = new IPv6AddressTypeImpl();
  public static final IMarkupType MARKUP_LINE_TYPE = new MarkupLineTypeImpl();
  public static final IMarkupType MARKUP_MULTILINE_TYPE = new MarkupMultiLineTypeImpl();

  public static final INcNameType NCNAME_TYPE = new NcNameTypeImpl();
  public static final INonNegativeIntegerType NON_NEGATIVE_INTEGER_TYPE = new NonNegativeIntegerTypeImpl();
  public static final IPositiveIntegerType POSITIVE_INTEGER_TYPE = new PositiveIntegerTypeImpl();
  public static final IStringType STRING_TYPE = new StringTypeImpl();
  public static final ITokenType TOKEN_TYPE = new TokenTypeImpl();
  public static final IAnyUriType URI_TYPE = new AnyUriTypeImpl();
  public static final IUriReferenceType URI_REFERENCE_TYPE = new UriReferenceTypeImpl();
  public static final IUuidType UUID_TYPE = new UuidTypeImpl();

  private static final EnumMap<DataTypes, IAnyAtomicType> dataTypeToTypeMap = new EnumMap<>(DataTypes.class);
  private static final TypeFactory INSTANCE = new TypeFactory();

  static {
    dataTypeToTypeMap.put(DataTypes.BASE64, BASE64_TYPE);
    dataTypeToTypeMap.put(DataTypes.BOOLEAN, BOOLEAN_TYPE);
    dataTypeToTypeMap.put(DataTypes.DATE, DATE_WITHOUT_TZ_TYPE);
    dataTypeToTypeMap.put(DataTypes.DATE_TIME, DATETIME_WITHOUT_TZ_TYPE);
    dataTypeToTypeMap.put(DataTypes.DATE_TIME_WITH_TZ, DATETIME_WITH_TZ_TYPE);
    dataTypeToTypeMap.put(DataTypes.DATE_WITH_TZ, DATE_WITH_TZ_TYPE);
    dataTypeToTypeMap.put(DataTypes.DECIMAL, DECIMAL_TYPE);
    dataTypeToTypeMap.put(DataTypes.EMAIL_ADDRESS, EMAIL_ADDRESS_TYPE);
    dataTypeToTypeMap.put(DataTypes.HOSTNAME, HOSTNAME_TYPE);
    dataTypeToTypeMap.put(DataTypes.INTEGER, INTEGER_TYPE);
    dataTypeToTypeMap.put(DataTypes.IP_V4_ADDRESS, IP_V4_ADDRESS_TYPE);
    dataTypeToTypeMap.put(DataTypes.IP_V6_ADDRESS, IP_V6_ADDRESS_TYPE);
    dataTypeToTypeMap.put(DataTypes.MARKUP_LINE, MARKUP_LINE_TYPE);
    dataTypeToTypeMap.put(DataTypes.MARKUP_MULTILINE, MARKUP_MULTILINE_TYPE);
    dataTypeToTypeMap.put(DataTypes.NCNAME, NCNAME_TYPE);
    dataTypeToTypeMap.put(DataTypes.NON_NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER_TYPE);
    dataTypeToTypeMap.put(DataTypes.POSITIVE_INTEGER, POSITIVE_INTEGER_TYPE);
    dataTypeToTypeMap.put(DataTypes.STRING, STRING_TYPE);
    dataTypeToTypeMap.put(DataTypes.TOKEN, TOKEN_TYPE);
    dataTypeToTypeMap.put(DataTypes.URI, URI_TYPE);
    dataTypeToTypeMap.put(DataTypes.URI_REFERENCE, URI_REFERENCE_TYPE);
    dataTypeToTypeMap.put(DataTypes.UUID, UUID_TYPE);
  }

  public static TypeFactory instance() {
    return INSTANCE;
  }

  protected TypeFactory() {
  }

  /**
   * Retrieves the Metapath type of the provided data type.
   * 
   * @param dataType
   *          the data type
   * @return the Metaschema type
   */
  public IAnyAtomicType getTypeForDataType(DataTypes dataType) {
    IAnyAtomicType retval = dataTypeToTypeMap.get(dataType);
    if (dataType == null) {
      retval = new UnknownUntypedAtomicType(dataType);
    }
    return retval;
  }
}
