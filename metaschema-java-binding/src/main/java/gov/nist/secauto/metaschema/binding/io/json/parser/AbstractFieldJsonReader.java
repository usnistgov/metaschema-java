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

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class AbstractFieldJsonReader<CLASS, OBJECT_PARSER extends BoundObjectParser<CLASS,
    FieldClassBinding<CLASS>>> extends AbstractJsonReader<CLASS, FieldClassBinding<CLASS>, OBJECT_PARSER>
    implements FieldJsonReader<CLASS> {

  public AbstractFieldJsonReader(FieldClassBinding<CLASS> classBinding) {
    super(classBinding);
  }

  protected Map<PropertyBinding, Supplier<?>> handleUnknownProperty(String fieldName, Set<String> unknownFieldNames,
      JsonParsingContext parsingContext) throws BindingException {
    FlagPropertyBinding jsonValueKeyFlagPropertyBinding = getClassBinding().getJsonValueKeyFlagPropertyBinding();

    Map<PropertyBinding, Supplier<?>> retval = Collections.emptyMap();
    if (jsonValueKeyFlagPropertyBinding != null) {
      if (unknownFieldNames.isEmpty()) {
        retval = new HashMap<>();
        // parse the first unknown property using JSON value key with flag semantics
        // first set the key
        {
          PropertyInfo propertyInfo = jsonValueKeyFlagPropertyBinding.getPropertyInfo();
          JavaTypeAdapter<?> javaTypeAdapter
              = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
          retval.put(jsonValueKeyFlagPropertyBinding, javaTypeAdapter.parseAndSupply(fieldName));
        }

        // now parse the value
        {
          FieldValuePropertyBinding fieldValuePropertyBinding = getClassBinding().getFieldValuePropertyBinding();
          PropertyInfo propertyInfo = fieldValuePropertyBinding.getPropertyInfo();
          JavaTypeAdapter<?> javaTypeAdapter
              = parsingContext.getBindingContext().getJavaTypeAdapter(propertyInfo.getItemType());
          retval.put(fieldValuePropertyBinding, javaTypeAdapter.parseAndSupply(parsingContext));
        }
      } else {
        JsonParser parser = parsingContext.getEventReader();
        JsonLocation location = parser.getCurrentLocation();
        throw new BindingException(String.format(
            "Unable to parse field '%s' for class '%s' at location %d:%d."
                + " This class expects a JSON value key mapped by a key flag."
                + " This feature cannot be used with multiple unbound fields.",
            fieldName, getClassBinding().getClazz().getName(), location.getLineNr(), location.getColumnNr()));
      }
    } else {
      JsonParser parser = parsingContext.getEventReader();
      try {
        JsonUtil.skipValue(parser);
      } catch (IOException ex) {
        throw new BindingException(ex);
      }
    }
    return retval;
  }

}
