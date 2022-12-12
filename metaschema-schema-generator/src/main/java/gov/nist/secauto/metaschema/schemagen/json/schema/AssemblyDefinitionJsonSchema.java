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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.ModelType;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.FlagInstanceFilter;
import gov.nist.secauto.metaschema.schemagen.json.JsonGenerationState;
import gov.nist.secauto.metaschema.schemagen.json.property.FlagInstanceJsonProperty;
import gov.nist.secauto.metaschema.schemagen.json.property.IJsonProperty.PropertyCollection;
import gov.nist.secauto.metaschema.schemagen.json.property.INamedModelInstanceJsonProperty;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class AssemblyDefinitionJsonSchema
    extends AbstractDefinitionJsonSchema<IAssemblyDefinition> {

  public AssemblyDefinitionJsonSchema(
      @NonNull IAssemblyDefinition definition) {
    super(definition);
  }

  @Override
  public void resolveSubSchemas(JsonGenerationState state) {
    for (IFlagInstance instance : getDefinition().getFlagInstances()) {
      state.getSchema(instance.getDefinition());
    }

    for (INamedModelInstance instance : getDefinition().getNamedModelInstances()) {
      state.getSchema(instance.getDefinition());
    }
  }

  @Override
  protected void generateBody(JsonGenerationState state, ObjectNode obj) throws IOException {
    IAssemblyDefinition definition = getDefinition();

    obj.put("type", "object");

    // determine the flag instances to generate
    IFlagInstance jsonKeyFlag = definition.getJsonKeyFlagInstance();
    Collection<? extends IFlagInstance> flags
        = FlagInstanceFilter.filterFlags(definition.getFlagInstances(), jsonKeyFlag);

    PropertyCollection properties = new PropertyCollection();

    // generate flag properties
    for (IFlagInstance flag : flags) {
      assert flag != null;
      new FlagInstanceJsonProperty(flag).generateProperty(properties, state); // NOPMD instantiation needed
    }
    // generate model properties
    Collection<? extends INamedModelInstance> instances = definition.getNamedModelInstances();
    for (INamedModelInstance instance : instances) {
      assert instance != null;
      INamedModelInstanceJsonProperty.newProperty(instance)
          .generateProperty(properties, state);
    }

    Collection<? extends IChoiceInstance> choices = definition.getChoiceInstances();
    if (choices.isEmpty()) {
      properties.generate(obj);

      obj.put("additionalProperties", false);
    } else {
      generateChoices(choices, properties, obj, state);
    }
  }

  protected void generateChoices(
      @NonNull Collection<? extends IChoiceInstance> choices,
      @NonNull PropertyCollection properties,
      @NonNull ObjectNode definitionNode,
      @NonNull JsonGenerationState state) throws IOException {
    List<PropertyCollection> propertyChoices = CollectionUtil.singletonList(properties);
    propertyChoices = explodeChoices(choices, propertyChoices, state);

    if (propertyChoices.size() == 1) {
      propertyChoices.iterator().next().generate(definitionNode);
    } else if (propertyChoices.size() > 1) {
      ArrayNode anyOfdNode = ObjectUtils.notNull(JsonNodeFactory.instance.arrayNode());
      for (PropertyCollection propertyChoice : propertyChoices) {
        ObjectNode choiceDefinitionNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
        propertyChoice.generate(choiceDefinitionNode);
        choiceDefinitionNode.put("additionalProperties", false);
        anyOfdNode.add(choiceDefinitionNode);
      }
      definitionNode.set("anyOf", anyOfdNode);
    }
  }

  protected List<PropertyCollection> explodeChoices(
      @NonNull Collection<? extends IChoiceInstance> choices,
      @NonNull List<PropertyCollection> propertyChoices,
      @NonNull JsonGenerationState state) throws IOException {

    List<PropertyCollection> retval = propertyChoices;

    for (IChoiceInstance choice : choices) {
      List<PropertyCollection> newRetval = new LinkedList<>(); // NOPMD - intentional
      for (IModelInstance optionInstance : choice.getModelInstances()) {
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
            INamedModelInstanceJsonProperty.newProperty((INamedModelInstance) optionInstance)
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
