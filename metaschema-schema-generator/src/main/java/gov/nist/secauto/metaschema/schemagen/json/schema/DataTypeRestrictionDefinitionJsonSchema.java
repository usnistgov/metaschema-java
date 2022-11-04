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

package gov.nist.secauto.metaschema.schemagen.json.schema;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.common.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractGenerationState.AllowedValueCollection;
import gov.nist.secauto.metaschema.schemagen.json.JsonGenerationState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DataTypeRestrictionDefinitionJsonSchema
    extends AbstractDefineableJsonSchema {
  @NonNull
  private final IValuedDefinition definition;
  @NonNull
  private final AllowedValueCollection allowedValuesCollection;

  public DataTypeRestrictionDefinitionJsonSchema(
      @NonNull IValuedDefinition definition,
      @NonNull AllowedValueCollection allowedValuesCollection) {
    this.definition = definition;
    CollectionUtil.requireNonEmpty(allowedValuesCollection.getValues());
    this.allowedValuesCollection = allowedValuesCollection;
  }

  @Override
  public void resolveSubSchemas(JsonGenerationState state) {
    // do nothing
  }

  @NonNull
  protected IValuedDefinition getDefinition() {
    return definition;
  }

  @NonNull
  protected AllowedValueCollection getAllowedValuesCollection() {
    return allowedValuesCollection;
  }

  @Override
  public boolean isInline(JsonGenerationState state) {
    return state.isInline(getDefinition());
  }

  @Override
  protected String generateDefinitionName(JsonGenerationState state) {
    return state.getTypeNameForDefinition(definition, "Value");
  }

  @Override
  public void generateSchema(JsonGenerationState state, ObjectNode obj) {
    // generate a restriction on the built-in type for the enumerated values
    ArrayNode enumArray = JsonNodeFactory.instance.arrayNode();

    AllowedValueCollection allowedValuesCollection = getAllowedValuesCollection();
    for (IAllowedValue allowedValue : allowedValuesCollection.getValues()) {
      switch (getDefinition().getJavaTypeAdapter().getJsonRawType()) {
      case STRING:
        enumArray.add(allowedValue.getValue());
        break;
      case BOOLEAN:
        enumArray.add(Boolean.parseBoolean(allowedValue.getValue()));
        break;
      case INTEGER:
        enumArray.add(new BigInteger(allowedValue.getValue())); // NOPMD unavoidable
        break;
      case NUMBER:
        enumArray.add(new BigDecimal(allowedValue.getValue(), MathContext.DECIMAL64)); // NOPMD unavoidable
        break;
      default:
        throw new UnsupportedOperationException(getDefinition().getJavaTypeAdapter().getJsonRawType().toString());
      }
    }

    if (allowedValuesCollection.isClosed()) {
      obj.set("enum", enumArray);
    } else {
      // get schema for the built-in type
      IJsonSchema dataTypeSchema = state.getSchema(getDefinition().getJavaTypeAdapter());

      // if other values are allowed, we need to make a union of the restriction type and the base
      // built-in type
      ArrayNode anyOfArray = obj.putArray("anyOf");

      // add the data type reference
      dataTypeSchema.generateSchemaOrRef(state, ObjectUtils.notNull(anyOfArray.addObject()));
      // add the enumeration
      anyOfArray.addObject()
          .set("enum", enumArray);
    }
  }
}
