/**
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

package gov.nist.secauto.metaschema.binding.io.json.parser;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class JsonUtil {
  private static final Logger logger = LogManager.getLogger(JsonUtil.class);

  private JsonUtil() {
    // disable construction
  }

  public static void skipValue(JsonParser parser) throws IOException {

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
      String msg = String.format("Unhandled JsonToken '%s'", currentToken.toString());
      logger.error(msg);
      throw new UnsupportedOperationException(msg);
    }
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

  public static JsonToken readNextToken(JsonParser parser, JsonToken expectedToken)
      throws IOException, BindingException {
    JsonToken token = parser.nextToken();
    if (!expectedToken.equals(token)) {
      throw new BindingException(
          String.format("Expected JsonToken '%s', but found JsonToken '%s.", expectedToken, token));
    }
    return token;
  }

  public static void expectCurrentToken(JsonParser parser, JsonToken expectedToken) {
    JsonToken token = parser.currentToken();
    assert (expectedToken != null && expectedToken.equals(token)) || (expectedToken == null && token == null) : String
        .format("Expected JsonToken '%s', but found JsonToken '%s.", expectedToken, token);
  }

  public static String toLocationContext(JsonParser parser, ClassBinding<?> classBinding,
      PropertyBinding propertyBinding) {
    StringBuilder builder = new StringBuilder();
    builder.append("property '");
    builder.append(propertyBinding.getPropertyInfo().getSimpleName());
    builder.append("' on class '");
    builder.append(classBinding.getClazz().getName());
    builder.append("' at location '");
    JsonLocation location = parser.getCurrentLocation();
    builder.append(location.getLineNr());
    builder.append(':');
    builder.append(location.getColumnNr());
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

}
