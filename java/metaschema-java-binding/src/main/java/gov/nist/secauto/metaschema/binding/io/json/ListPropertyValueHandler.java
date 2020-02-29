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

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ListPropertyValueHandler extends AbstractPropertyValueHandler {
  private final boolean allowSingletonValue;
  private final List<Object> values = new LinkedList<>();
  private boolean firstValueParsed = false;

  public ListPropertyValueHandler(ClassBinding<?> classBinding, PropertyItemHandler propertyItemHandler,
      boolean requireSingletonValue) {
    super(classBinding, propertyItemHandler);
    this.allowSingletonValue = requireSingletonValue;
  }

  protected boolean isRequireSingletonValue() {
    return allowSingletonValue;
  }

  @Override
  public boolean parseNextFieldValue(Object parent, JsonParsingContext parsingContext) throws BindingException, IOException {
    JsonParser parser = parsingContext.getEventReader();
    JsonToken currentToken = parser.currentToken();

    if (!firstValueParsed) {
      if (JsonToken.START_ARRAY.equals(currentToken)) {
        // advance to the start of the first value item
        currentToken = parser.nextToken();
      } else if (!allowSingletonValue) {
        throw new BindingException(
            String.format("Found unexpected '%s' token when parsing %s. This list doesn't allow a singleton object.",
                currentToken, JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
      }
      firstValueParsed = true;
    }

    // Parse the value at the current token; after parsing the current token is
    // expected to be at the next START_OBJECT, END_ARRAY, or FIELD_NAME
    PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
    List<Object> parsedValues = propertyItemHandler.parse(null, parent, parsingContext);

    // if (!JsonUtil.checkEndOfValue(parser, currentToken)) {
    // throw new BindingException(String.format("Unexpected end state token '%s' after parsing %s.",
    // currentToken,
    // JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
    // }
    //
    // // advance past the end
    currentToken = parser.currentToken();

    boolean retval;
    if (parsedValues != null) {
      values.addAll(parsedValues);
    }

    if (JsonToken.END_ARRAY.equals(currentToken)) {
      if (!firstValueParsed) {
        throw new BindingException(String.format("Found unexpected END_ARRAY token when parsing %s.",
            JsonUtil.toLocationContext(parser, getClassBinding(), getPropertyBinding())));
      } else {
        retval = false;
        // skip past the END_ARRAY
        currentToken = parser.nextToken();
      }
    } else if (JsonToken.END_OBJECT.equals(currentToken) || JsonToken.FIELD_NAME.equals(currentToken)) {
      retval = false;
    } else {
      retval = true;
    }
    return retval;
  }

  @Override
  public Supplier<List<Object>> getObjectSupplier() {
    return () -> values;
  }
}
