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

import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.json.JsonGenerationState;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A type of {@link IJsonSchema} that can represent a schema that is a global definition.
 * <p>
 * A schema of this type will be a global definition if {@link #isInline(JsonGenerationState)} is
 * {@code false}.
 */
public interface IDefineableJsonSchema extends IJsonSchema {
  /**
   * Determine if the JSON schema object is a definition.
   *
   * @param state
   *          the schema generation state used for context and writing
   * @return {@code true} if the SON schema object is a definition or {@code false} otherwise
   */
  default boolean isDefinition(@NonNull JsonGenerationState state) {
    return !isInline(state);
  }

  /**
   * Get the definition's name.
   *
   * @param state
   *          the schema generation state used for context and writing
   * @return the definition name
   * @throws IllegalStateException
   *           if the JSON schema object is not a definition
   */
  @NonNull
  String getDefinitionName(@NonNull JsonGenerationState state);

  /**
   * Get the definition's reference URI.
   *
   * @param state
   *          the schema generation state used for context and writing
   * @return the definition's reference URI
   * @throws IllegalStateException
   *           if the JSON schema object is not a definition
   */
  default String getDefinitionRef(@NonNull JsonGenerationState state) {
    if (!isDefinition(state)) {
      throw new IllegalStateException();
    }

    return ObjectUtils.notNull(new StringBuilder()
        .append("#/definitions/")
        .append(getDefinitionName(state))
        .toString());
  }

  @Override
  default void generateSchemaOrRef(@NonNull JsonGenerationState state, @NonNull ObjectNode obj) {
    if (isDefinition(state)) {
      obj.put("$ref", getDefinitionRef(state));
    } else {
      generateSchema(state, obj);
    }
  }

  default void generateDefinition(@NonNull JsonGenerationState state, @NonNull ObjectNode definitionsObject) {
    if (!isDefinition(state)) {
      throw new IllegalStateException();
    }

    // create the definition property
    ObjectNode definitionObj = ObjectUtils.notNull(
        definitionsObject.putObject(getDefinitionName(state)));
    // definitionObj.put("$id", state.getJsonDefinitionRefForDefinition(definition));

    // generate the definition object contents
    generateSchema(state, definitionObj);
  }
}
