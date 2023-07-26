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

package gov.nist.secauto.metaschema.model.common.datatype.adapter;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;

import gov.nist.secauto.metaschema.model.common.datatype.AbstractDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IIPv4AddressItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;
import inet.ipaddr.IncompatibleAddressException;
import inet.ipaddr.ipv4.IPv4Address;

public class IPv4AddressAdapter
    extends AbstractDataTypeAdapter<IPv4Address, IIPv4AddressItem> {
  @NonNull
  private static final List<String> NAMES = ObjectUtils.notNull(
      List.of("ip-v4-address"));
  private static final IPAddressStringParameters IP_V_4;

  static {
    IP_V_4 = new IPAddressStringParameters.Builder().allowIPv6(false).allowEmpty(false).allowSingleSegment(false)
        .allowWildcardedSeparator(false).getIPv4AddressParametersBuilder().allowBinary(false).allowLeadingZeros(false)
        .allowPrefixesBeyondAddressSize(false).getParentBuilder().toParams();
  }

  IPv4AddressAdapter() {
    super(IPv4Address.class);
  }

  @Override
  public List<String> getNames() {
    return NAMES;
  }

  @Override
  public JsonFormatTypes getJsonRawType() {
    return JsonFormatTypes.STRING;
  }

  @SuppressWarnings("null")
  @Override
  public IPv4Address parse(String value) {
    try {
      return (IPv4Address) new IPAddressString(value, IP_V_4).toAddress();
    } catch (AddressStringException | IncompatibleAddressException ex) {
      throw new IllegalArgumentException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  public IPv4Address copy(Object obj) {
    // value is immutable
    return (IPv4Address) obj;
  }

  @Override
  public Class<IIPv4AddressItem> getItemClass() {
    return IIPv4AddressItem.class;
  }

  @Override
  public IIPv4AddressItem newItem(Object value) {
    IPv4Address item = toValue(value);
    return IIPv4AddressItem.valueOf(item);
  }
}
