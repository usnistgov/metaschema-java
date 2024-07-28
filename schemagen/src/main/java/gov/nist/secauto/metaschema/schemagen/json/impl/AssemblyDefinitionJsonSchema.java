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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.ModelType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;
import gov.nist.secauto.metaschema.schemagen.json.impl.IJsonProperty.PropertyCollection;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class AssemblyDefinitionJsonSchema
    extends AbstractModelDefinitionJsonSchema<IAssemblyDefinition> {

  private final Lazy<List<IGroupableModelInstanceJsonProperty<?>>> groupableModelInstances;

  private final Map<INamedModelInstanceAbsolute, IGroupableModelInstanceJsonProperty<?>> choiceInstances;

  public AssemblyDefinitionJsonSchema(
      @NonNull IAssemblyDefinition definition,
      @Nullable String jsonKeyFlagName,
      @Nullable String discriminatorProperty,
      @Nullable String discriminatorValue,
      @NonNull IJsonGenerationState state) {
    super(definition, jsonKeyFlagName, discriminatorProperty, discriminatorValue);
    this.groupableModelInstances = Lazy.lazy(() -> ObjectUtils.notNull(definition.getModelInstances().stream()
        .filter(instance -> !(instance instanceof IChoiceInstance))
        .map(instance -> {
          IGroupableModelInstanceJsonProperty<?> property;
          if (instance instanceof INamedModelInstanceAbsolute) {
            INamedModelInstanceAbsolute named = (INamedModelInstanceAbsolute) instance;
            property = new NamedModelInstanceJsonProperty(named, state);
          } else if (instance instanceof IChoiceGroupInstance) {
            IChoiceGroupInstance choice = (IChoiceGroupInstance) instance;
            property = new ChoiceGroupInstanceJsonProperty(choice, state);
          } else {
            throw new UnsupportedOperationException(
                "model instance class not supported: " + instance.getClass().getName());
          }
          return property;
        })
        .collect(Collectors.toUnmodifiableList())));

    this.choiceInstances = definition.getChoiceInstances().stream()
        .flatMap(choice -> explodeChoice(ObjectUtils.requireNonNull(choice)))
        .collect(Collectors.toUnmodifiableMap(
            Function.identity(),
            instance -> new NamedModelInstanceJsonProperty(ObjectUtils.requireNonNull(instance), state)));
  }

  private static Stream<? extends INamedModelInstanceAbsolute> explodeChoice(@NonNull IChoiceInstance choice) {
    return choice.getNamedModelInstances().stream();
  }

  @NonNull
  protected List<IGroupableModelInstanceJsonProperty<?>> getGroupableModelInstances() {
    return groupableModelInstances.get();
  }

  @Override
  public void gatherDefinitions(
      @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
      @NonNull IJsonGenerationState state) {
    // avoid recursion
    if (!gatheredDefinitions.containsKey(getKey())) {
      super.gatherDefinitions(gatheredDefinitions, state);

      // if (isInline(state)) {
      for (IGroupableModelInstanceJsonProperty<?> property : getGroupableModelInstances()) {
        property.gatherDefinitions(gatheredDefinitions, state);
      }

      // handle choices
      this.choiceInstances.values().forEach(property -> {
        property.gatherDefinitions(gatheredDefinitions, state);
      });
    }
  }

  @Override
  protected void generateBody(
      IJsonGenerationState state,
      ObjectNode obj) throws IOException {
    obj.put("type", "object");

    PropertyCollection properties = new PropertyCollection();

    // handle possible discriminator
    String discriminatorProperty = getDiscriminatorProperty();
    if (discriminatorProperty != null) {
      ObjectNode discriminatorObj = state.getJsonNodeFactory().objectNode();
      discriminatorObj.put("const", getDiscriminatorValue());
      properties.addProperty(discriminatorProperty, discriminatorObj);
    }

    // generate flag properties
    for (FlagInstanceJsonProperty flag : getFlagProperties()) {
      assert flag != null;
      flag.generateProperty(properties, state);
    }

    // generate model properties
    for (IGroupableModelInstanceJsonProperty<?> property : getGroupableModelInstances()) {
      assert property != null;
      property.generateProperty(properties, state);
    }

    Collection<? extends IChoiceInstance> choices = getDefinition().getChoiceInstances();
    if (choices.isEmpty()) {
      properties.generate(obj);
      obj.put("additionalProperties", false);
    } else {
      List<PropertyCollection> propertyChoices = CollectionUtil.singletonList(properties);
      propertyChoices = explodeChoices(choices, propertyChoices, state);

      if (propertyChoices.size() == 1) {
        propertyChoices.iterator().next().generate(obj);
      } else if (propertyChoices.size() > 1) {
        generateChoices(propertyChoices, obj, state);
      }
    }
  }

  protected void generateChoices(
      List<PropertyCollection> propertyChoices,
      @NonNull ObjectNode definitionNode,
      @NonNull IJsonGenerationState state) {
    ArrayNode anyOfdNode = ObjectUtils.notNull(JsonNodeFactory.instance.arrayNode());
    for (PropertyCollection propertyChoice : propertyChoices) {
      ObjectNode choiceDefinitionNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
      propertyChoice.generate(choiceDefinitionNode);
      choiceDefinitionNode.put("additionalProperties", false);
      anyOfdNode.add(choiceDefinitionNode);
    }
    definitionNode.set("anyOf", anyOfdNode);
  }

  protected List<PropertyCollection> explodeChoices(
      @NonNull Collection<? extends IChoiceInstance> choices,
      @NonNull List<PropertyCollection> propertyChoices,
      @NonNull IJsonGenerationState state) throws IOException {

    List<PropertyCollection> retval = propertyChoices;

    for (IChoiceInstance choice : choices) {
      List<PropertyCollection> newRetval = new LinkedList<>(); // NOPMD - intentional
      for (INamedModelInstanceAbsolute optionInstance : choice.getNamedModelInstances()) {
        if (ModelType.CHOICE.equals(optionInstance.getModelType())) {
          // recurse
          newRetval.addAll(explodeChoices(
              CollectionUtil.singleton((IChoiceInstance) optionInstance),
              retval,
              state));
        } else {
          // iterate over the old array of choices and append new choice
          for (PropertyCollection oldInstanceProperties : retval) {
            @SuppressWarnings("null")
            @NonNull PropertyCollection newInstanceProperties = oldInstanceProperties.copy();

            // add the choice
            choiceInstances.get(optionInstance)
                .generateProperty(newInstanceProperties, state);

            newRetval.add(newInstanceProperties);
          }
        }
      }
      retval = newRetval;
    }
    return retval;
  }
}
