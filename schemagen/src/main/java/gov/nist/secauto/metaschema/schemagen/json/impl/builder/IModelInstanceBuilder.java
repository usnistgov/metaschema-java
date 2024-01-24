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

import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IGroupable;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.schemagen.json.IDataTypeJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IDefineableJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IDefineableJsonSchema.IKey;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IModelInstanceBuilder<T extends IModelInstanceBuilder<T>> extends IBuilder<T> {
  @NonNull
  T addItemType(@NonNull INamedModelInstanceGrouped itemType);

  @NonNull
  T addItemType(@NonNull INamedModelInstanceAbsolute itemType);

  @NonNull
  T minItems(int min);

  @NonNull
  T maxItems(int max);

  @NonNull
  List<IType> getTypes();

  int getMinOccurrence();

  int getMaxOccurrence();

  interface IType {
    @Nullable
    IDefineableJsonSchema getJsonKeyFlagSchema(@NonNull IJsonGenerationState state);

    @Nullable
    IDataTypeJsonSchema getJsonKeyDataTypeSchema(@NonNull IJsonGenerationState state);

    @NonNull
    IDefinitionJsonSchema<IModelDefinition> getJsonSchema(@NonNull IJsonGenerationState state);

    void build(
        @NonNull ArrayNode anyOf,
        @NonNull IJsonGenerationState state);

    void build(
        @NonNull ObjectNode object,
        @NonNull IJsonGenerationState state);

    default void gatherDefinitions(
        @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
        @NonNull IJsonGenerationState state) {
      IDefinitionJsonSchema<IModelDefinition> schema = getJsonSchema(state);
      schema.gatherDefinitions(gatheredDefinitions, state);
    }
  }

  @NonNull
  static <I extends IModelInstance & IGroupable> IModelInstanceBuilder<?> builder(@NonNull I instance) {
    IModelInstanceBuilder<?> builder;

    if (instance instanceof INamedModelInstanceAbsolute) {
      INamedModelInstanceAbsolute named = (INamedModelInstanceAbsolute) instance;
      builder = newCollectionBuilder(named);
      builder.addItemType(named);
    } else if (instance instanceof IChoiceGroupInstance) {
      IChoiceGroupInstance choice = (IChoiceGroupInstance) instance;
      builder = newCollectionBuilder(choice);
      for (INamedModelInstanceGrouped groupedInstance : choice.getNamedModelInstances()) {
        assert groupedInstance != null;
        builder.addItemType(groupedInstance);
      }
    } else {
      throw new UnsupportedOperationException(
          "Unsupported named model instance type: " + instance.getClass().getName());
    }
    return builder;
  }

  @NonNull
  static IModelInstanceBuilder<?> newCollectionBuilder(@NonNull IGroupable groupable) {
    JsonGroupAsBehavior behavior = groupable.getJsonGroupAsBehavior();
    IModelInstanceBuilder<?> retval;
    switch (behavior) {
    case LIST:
      retval = new ArrayBuilder();
      break;
    case SINGLETON_OR_LIST:
      retval = new SingletonOrListBuilder();
      break;
    case KEYED:
      retval = new KeyedObjectBuilder();
      break;
    case NONE:
      retval = new SingletonBuilder();
      break;
    default:
      throw new UnsupportedOperationException(
          String.format("Unsupported group-as in-json binding '%s'.", behavior));
    }

    retval.minItems(groupable.getMinOccurs());
    retval.maxItems(groupable.getMaxOccurs());
    return retval;
  }
}
