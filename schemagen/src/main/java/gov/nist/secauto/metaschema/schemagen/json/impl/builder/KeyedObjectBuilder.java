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

package gov.nist.secauto.metaschema.schemagen.json.impl.builder;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.json.IDataTypeJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KeyedObjectBuilder
    extends AbstractCollectionBuilder<KeyedObjectBuilder> {

  @Override
  protected KeyedObjectBuilder thisBuilder() {
    return this;
  }

  @Override
  public void build(
      ObjectNode object,
      IJsonGenerationState state) {
    object.put("type", "object");

    if (getMinOccurrence() > 0) {
      object.put("minProperties", getMinOccurrence());
    }

    if (getMaxOccurrence() != -1) {
      object.put("maxProperties", getMaxOccurrence());
    }

    Set<IDataTypeJsonSchema> jsonKeyDataTypeSchemas = new LinkedHashSet<>();
    List<IType> types = getTypes();
    for (IType type : types) {
      // handle json key
      IDataTypeJsonSchema schema = type.getJsonKeyDataTypeSchema(state);
      jsonKeyDataTypeSchemas.add(schema);
    }

    if (!types.isEmpty()) {
      ObjectNode propertyNames = ObjectUtils.notNull(object.putObject("propertyNames"));
      if (types.size() == 1) {
        types.iterator().next().build(propertyNames, state);
      } else {
        ArrayNode anyOf = propertyNames.putArray("anyOf");
        for (IType type : types) {
          type.build(ObjectUtils.notNull(anyOf.objectNode()), state);
        }
      }
    }

    ObjectNode patternProperties = ObjectUtils.notNull(object.putObject("patternProperties"));
    ObjectNode wildcard = patternProperties.putObject("^.*$");
    if (types.size() == 1) {
      types.iterator().next().build(wildcard, state);
    } else {
      ArrayNode oneOf = wildcard.putArray("anyOf");
      for (IType type : types) {
        type.build(oneOf, state);
      }
    }
  }

}
