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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class MapPropertyValueHandler extends AbstractPropertyValueHandler {
  private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
  private final PropertyInfo jsonKeyPropertyInfo;
  private boolean parsedStartObject = false;

  public MapPropertyValueHandler(ClassBinding<?> classBinding, PropertyItemHandler propertyItemHandler,
      BindingContext bindingContext) throws BindingException {
    super(classBinding, propertyItemHandler);

    PropertyInfo propertyInfo = getPropertyBinding().getPropertyInfo();
    ClassBinding<?> itemClassBinding = bindingContext.getClassBinding(propertyInfo.getItemType());
    this.jsonKeyPropertyInfo = itemClassBinding.getJsonKeyFlagPropertyBinding().getPropertyInfo();
  }

  protected PropertyInfo getJsonKeyPropertyInfo() {
    return jsonKeyPropertyInfo;
  }

  @Override
  public boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException {
    /*
     * JSON will look like this:
     * 
     * { "keyValue1": VALUE, "keyValue2": VALUE }
     */
    JsonParser parser = parsingContext.getEventReader();

    JsonToken currentToken;
    if (!parsedStartObject) {
      // a map will always start with a START_OBJECT. Advance past this start object.
      JsonUtil.expectCurrentToken(parser, JsonToken.START_OBJECT);
      currentToken = parser.nextToken();
      parsedStartObject = true;
    } else {
      currentToken = parser.currentToken();
      if (JsonToken.END_OBJECT.equals(currentToken)) {
        // we parsed the last value item
        return false;
      }
    }

    // get the map key
    String jsonKeyName = parser.currentName();

    // parse to the value
    currentToken = parser.nextToken();

    // Parse the value(s) at the current token; after this the current token is
    // expected to be the end of the value (e.g., VALUE, END_OBJECT
    PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
    List<Object> parsedValues = propertyItemHandler.parse(parsingContext, null);

    // // Check end of parsed value
    // JsonUtil.checkEndOfValue(parser, currentToken);

    if (parsedValues != null) {
      // post process the values to append the key, and add to the map
      PropertyInfo jsonKeyPropertyInfo = getJsonKeyPropertyInfo();
      for (Object obj : parsedValues) {
        JavaTypeAdapter<?> adapter
            = parsingContext.getBindingContext().getJavaTypeAdapter(jsonKeyPropertyInfo.getItemType());
        Object value = adapter.parse(jsonKeyName);
        jsonKeyPropertyInfo.setValue(obj, value);
        values.put(jsonKeyName, obj);
      }
    }

    currentToken = parser.currentToken();
    if (JsonToken.END_OBJECT.equals(currentToken)) {
      // we parsed the last value item
      // now parse past the END_OBJECT for the map
      parser.nextToken();
      return false;
    }

    return true;
  }

  @Override
  public Supplier<LinkedHashMap<String, Object>> getObjectSupplier() {
    return () -> values;
  }
}
