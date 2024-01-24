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

package gov.nist.secauto.metaschema.schemagen.json.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.IInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.json.IDefineableJsonSchema.IKey;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IJsonProperty<I extends IInstance> {
  @NonNull
  I getInstance();

  @NonNull
  String getName();

  boolean isRequired();

  void gatherDefinitions(
      @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
      @NonNull IJsonGenerationState state);

  /**
   * Generate the schema type.
   *
   * @param properties
   *          the containing property context to add the property to
   * @param state
   *          the schema generation state used for context and writing
   * @param jsonKeyFlagName
   *          the name of the flag to use as the JSON key, or @{code null} if no
   *          flag is used as the JSON key
   * @param discriminator
   *          the name to use as the choice group discriminator, or @{code null}
   *          if no choice group discriminator is used
   * @throws SchemaGenerationException
   *           if an error occurred while writing the type
   */
  void generateProperty(
      @NonNull PropertyCollection properties,
      @NonNull IJsonGenerationState state);

  class PropertyCollection {
    private final Map<String, ObjectNode> properties;
    private final Set<String> required;

    public PropertyCollection() {
      this(new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    protected PropertyCollection(@NonNull Map<String, ObjectNode> properties, @NonNull Set<String> required) {
      this.properties = properties;
      this.required = required;
    }

    public Map<String, ObjectNode> getProperties() {
      return Collections.unmodifiableMap(properties);
    }

    public Set<String> getRequired() {
      return Collections.unmodifiableSet(required);
    }

    public void addProperty(@NonNull String name, @NonNull ObjectNode def) {
      properties.put(name, def);
    }

    public void addRequired(@NonNull String name) {
      required.add(name);
    }

    public PropertyCollection copy() {
      return new PropertyCollection(new LinkedHashMap<>(properties), new LinkedHashSet<>(required));
    }

    public void generate(@NonNull ObjectNode obj) {
      if (!properties.isEmpty()) {
        ObjectNode propertiesNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
        for (Map.Entry<String, ObjectNode> entry : properties.entrySet()) {
          propertiesNode.set(entry.getKey(), entry.getValue());
        }
        obj.set("properties", propertiesNode);

        if (!required.isEmpty()) {
          ArrayNode requiredNode = ObjectUtils.notNull(JsonNodeFactory.instance.arrayNode());
          for (String requiredProperty : required) {
            requiredNode.add(requiredProperty);
          }
          obj.set("required", requiredNode);
        }
      }
    }
  }
}
