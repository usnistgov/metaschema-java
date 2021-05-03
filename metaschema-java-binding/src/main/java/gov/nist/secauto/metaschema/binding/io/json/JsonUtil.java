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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.Property;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class JsonUtil {
  private static final Logger logger = LogManager.getLogger(JsonUtil.class);

  private JsonUtil() {
    // disable construction
  }

  public static String toString(JsonParser parser) throws IOException {
    StringBuilder builder = new StringBuilder();
    JsonToken token = parser.currentToken();
    builder.append(token.name());
    builder.append(" '").append(parser.getText()).append("'");
    builder.append("' at location '");
    JsonLocation location = parser.getCurrentLocation();
    builder.append(toString(location));
    builder.append("'");
    return builder.toString();
  }

  public static String toString(JsonLocation location) {
    StringBuilder builder = new StringBuilder();
    builder.append(location.getLineNr());
    builder.append(':');
    builder.append(location.getColumnNr());
    return builder.toString();
  }

  public static JsonToken advanceTo(JsonParser parser, JsonToken token) throws IOException {
    JsonToken currentToken = null;
    while (parser.hasCurrentToken() && !token.equals(currentToken = parser.currentToken())) {
      currentToken = parser.nextToken();
      logger.warn("skipping over: {}", toString(parser));
    }
    return currentToken;
  }

  public static JsonToken skipNextValue(JsonParser parser) throws IOException {

    JsonToken currentToken = parser.currentToken();
    // skip the field name
    if (JsonToken.FIELD_NAME.equals(currentToken)) {
      currentToken = parser.nextToken();
    }

    switch (currentToken) {
    case START_ARRAY:
    case START_OBJECT:
      parser.skipChildren();
      break;
    case VALUE_FALSE:
    case VALUE_NULL:
    case VALUE_NUMBER_FLOAT:
    case VALUE_NUMBER_INT:
    case VALUE_STRING:
    case VALUE_TRUE:
      // do nothing
      break;
    default:
      // error
      String msg = String.format("Unhandled JsonToken %s", toString(parser));
      logger.error(msg);
      throw new UnsupportedOperationException(msg);
    }

    // advance past the value
    return parser.nextToken();
  }

  public static boolean checkEndOfValue(JsonParser parser, JsonToken startToken) {
    JsonToken currentToken = parser.getCurrentToken();

    boolean retval;
    switch (startToken) {
    case START_OBJECT:
      retval = JsonToken.END_OBJECT.equals(currentToken);
      break;
    case START_ARRAY:
      retval = JsonToken.END_ARRAY.equals(currentToken);
      break;
    case VALUE_EMBEDDED_OBJECT:
    case VALUE_FALSE:
    case VALUE_NULL:
    case VALUE_NUMBER_FLOAT:
    case VALUE_NUMBER_INT:
    case VALUE_STRING:
    case VALUE_TRUE:
      retval = true;
      break;
    default:
      retval = false;
    }
    return retval;
  }

  public static void assertCurrent(JsonParser parser, JsonToken expectedToken) {
    JsonToken token = parser.currentToken();
    assert expectedToken.equals(token) : getAssertMessage(expectedToken, token, parser.getCurrentLocation());
  }

  public static JsonToken assertAndAdvance(JsonParser parser, JsonToken expectedToken) throws IOException {
    JsonToken token = parser.currentToken();
    assert expectedToken.equals(token) : getAssertMessage(expectedToken, token, parser.getCurrentLocation());
    return parser.nextToken();
  }

  public static JsonToken consumeAndAssert(JsonParser parser, JsonToken expectedToken)
      throws IOException {
    JsonToken token = parser.nextToken();
    assert expectedToken.equals(token) : getAssertMessage(expectedToken, token, parser.getCurrentLocation());
    return token;
  }

  public static String getAssertMessage(JsonToken expected, JsonToken actual, JsonLocation location) {
    return String.format("Expected JsonToken '%s', but found JsonToken '%s' at '%s'.",
        expected,
        actual,
        JsonUtil.toString(location));
  }

  public static String toLocationContext(JsonParser parser, ClassBinding classBinding,
      Property property) {
    StringBuilder builder = new StringBuilder();
    builder.append("property '");
    builder.append(property.getJavaPropertyName());
    builder.append("' on class '");
    builder.append(classBinding.getBoundClass().getName());
    builder.append("' at location '");
    JsonLocation location = parser.getCurrentLocation();
    builder.append(location.getLineNr());
    builder.append(':');
    builder.append(location.getColumnNr());
    builder.append("'");
    return builder.toString();
  }

}
