package gov.nist.secauto.metaschema.model.common.datatype.adapter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IPv6AddressAdapterTest {


  @ParameterizedTest
  @ValueSource(strings = { "2001:0000:0000:0000:0000:ffff:0a02:0202" })
  void testValues(String value) {
    new IPv6AddressAdapter().parse(value);
  }
}
