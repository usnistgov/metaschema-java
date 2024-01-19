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

package gov.nist.secauto.metaschema.schemagen.json;

import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.json.impl.AbstractDefinitionJsonSchema.SimpleKey;
import gov.nist.secauto.metaschema.schemagen.json.impl.AbstractModelDefinitionJsonSchema.ComplexKey;

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a JSON schema that is a global definition or an inline schema.
 * <p>
 * A schema of this type will be a global definition if
 * {@link #isInline(IJsonGenerationState)} is {@code false}.
 */
public interface IDefineableJsonSchema {

  /**
   * Determine if the schema is defined inline or as a global definition.
   *
   * @param state
   *          the schema generation state used for context
   * @return {@code true} if the schema is to be defined inline or {@code false}
   *         if the schema is to be defined globally
   */
  boolean isInline(@NonNull IJsonGenerationState state);

  default void generateSchemaOrRef(
      @NonNull ObjectNode obj,
      @NonNull IJsonGenerationState state) {
    if (isInline(state)) {
      generateInlineSchema(obj, state);
    } else {
      generateRef(obj, state);
    }
  }

  /**
   * Generate the schema within the provided JSON object node.
   *
   * @param obj
   *          the JSON object to populate
   * @param state
   *          the schema generation state used for context and writing
   * @throws SchemaGenerationException
   *           if an error occurred while writing the type
   */
  void generateInlineSchema(
      @NonNull ObjectNode obj,
      @NonNull IJsonGenerationState state);

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
  String getDefinitionName(@NonNull IJsonGenerationState state);

  /**
   * Get the definition's reference URI.
   *
   * @param state
   *          the schema generation state used for context and writing
   * @return the definition's reference URI
   * @throws IllegalStateException
   *           if the JSON schema object is not a definition
   */
  default String getDefinitionRef(@NonNull IJsonGenerationState state) {
    return ObjectUtils.notNull(new StringBuilder()
        .append("#/definitions/")
        .append(getDefinitionName(state))
        .toString());
  }

  /**
   * Generate a reference to a globally defined schema, within the provided JSON
   * object node.
   *
   * @param obj
   *          the JSON object to populate
   * @param state
   *          the schema generation state used for context and writing
   * @throws SchemaGenerationException
   *           if an error occurred while writing the type
   */
  default void generateRef(
      @NonNull ObjectNode obj,
      @NonNull IJsonGenerationState state) {
    obj.put("$ref", getDefinitionRef(state));
  }

  // /**
  // * Determine if the JSON schema object is a JSON definition.
  // *
  // * @param state
  // * the schema generation state used for context and writing
  // * @return {@code true} if the SON schema object is a definition or
  // * {@code false} otherwise
  // */
  // default boolean isDefinition(@NonNull IJsonGenerationState state) {
  // return !isInline(state);
  // }

  // REFACTOR: move to abstract implementation?
  default void generateDefinition(@NonNull IJsonGenerationState state, @NonNull ObjectNode definitionsObject) {

    // create the definition property
    ObjectNode definitionObj = ObjectUtils.notNull(
        definitionsObject.putObject(getDefinitionName(state)));

    // Add identifier, see usnistgov/metaschema#160
    definitionObj.put("$id", getDefinitionRef(state));

    // generate the definition object contents
    generateInlineSchema(definitionObj, state);
  }

  interface IKey extends Comparable<IKey> {
    Comparator<IKey> KEY_COMPARATOR = Comparator
        .<IKey, String>comparing(key -> key.getDefinition().getContainingModule().getShortName())
        .thenComparing(key -> key.getDefinition().getEffectiveName())
        .thenComparing(IKey::getJsonKeyFlagName, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(IKey::getDiscriminatorProperty, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(IKey::getDiscriminatorValue, Comparator.nullsFirst(Comparator.naturalOrder()));

    @NonNull
    static IKey of(@NonNull IDefinition definition) {
      return new SimpleKey(definition);
    }

    @NonNull
    static IKey of(
        @NonNull IDefinition definition,
        @Nullable String jsonKeyFlagName,
        @Nullable String discriminatorProperty,
        @Nullable String discriminatorValue) {
      IKey retval;
      if (jsonKeyFlagName != null || discriminatorProperty != null || discriminatorValue != null) {
        retval = new ComplexKey(definition, jsonKeyFlagName, discriminatorProperty, discriminatorValue);
      } else {
        retval = new SimpleKey(definition);
      }
      return retval;
    }

    @NonNull
    IDefinition getDefinition();

    @Nullable
    String getJsonKeyFlagName();

    @Nullable
    String getDiscriminatorProperty();

    @Nullable
    String getDiscriminatorValue();

    @Override
    default int compareTo(IKey other) {
      return KEY_COMPARATOR.compare(this, other);
    }
  }
}
