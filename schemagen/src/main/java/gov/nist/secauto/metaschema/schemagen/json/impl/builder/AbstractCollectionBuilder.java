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

import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IGroupable;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.schemagen.json.IDataTypeJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IDefineableJsonSchema.IKey;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractCollectionBuilder<T extends AbstractCollectionBuilder<T>>
    extends AbstractBuilder<T>
    implements IModelInstanceBuilder<T> {
  private int minOccurrence = IGroupable.DEFAULT_GROUP_AS_MIN_OCCURS;
  private int maxOccurrence = IGroupable.DEFAULT_GROUP_AS_MAX_OCCURS;

  @NonNull
  private final List<IModelInstanceBuilder.IType> types = new LinkedList<>();

  @Override
  public T addItemType(INamedModelInstanceAbsolute itemType) {
    types.add(new AbsoluteType(itemType));
    return thisBuilder();
  }

  @Override
  public T addItemType(INamedModelInstanceGrouped itemType) {
    types.add(new GroupedType(itemType));
    return thisBuilder();
  }

  @Override
  public List<IType> getTypes() {
    return CollectionUtil.unmodifiableList(types);
  }

  @Override
  public T minItems(int min) {
    if (min < 0) {
      throw new IllegalArgumentException(
          String.format("The minimum value '%d' cannot be negative.", min));
    }
    minOccurrence = min;
    return thisBuilder();
  }

  @Override
  public T maxItems(int max) {
    if (max < -1 || max == 0) {
      throw new IllegalArgumentException(
          String.format("The maximum value '%d' must be -1 or a positive value.", max));
    }
    maxOccurrence = max;
    return thisBuilder();
  }

  @Override
  public int getMinOccurrence() {
    return minOccurrence;
  }

  @Override
  public int getMaxOccurrence() {
    return maxOccurrence;
  }

  /**
   * Generates the type reference(s).
   *
   * @param object
   *          the parent object node to add properties to
   * @param state
   *          the generation state
   */
  protected void buildInternal(
      @NonNull ObjectNode object,
      @NonNull IJsonGenerationState state) {
    if (types.size() == 1) {
      // build the item type reference
      types.iterator().next().build(object, state);
    } else if (types.size() > 1) {
      // build an anyOf of the item type references
      ArrayNode anyOf = object.putArray("anyOf");
      for (IType type : types) {
        type.build(anyOf, state);
      }
    }
  }

  private abstract static class Type<T extends INamedModelInstance> implements IModelInstanceBuilder.IType {
    @NonNull
    private final T namedModelInstance;
    @Nullable
    private final IFlagInstance jsonKeyFlag;
    @Nullable
    private final String discriminatorProperty;
    @Nullable
    private final String discriminatorValue;
    @NonNull
    private final IKey key;

    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
    protected Type(@NonNull T instance) {
      this.namedModelInstance = instance;

      this.jsonKeyFlag = instance.getEffectiveJsonKey();

      if (instance instanceof INamedModelInstanceGrouped) {
        INamedModelInstanceGrouped grouped = (INamedModelInstanceGrouped) instance;
        this.discriminatorProperty = grouped.getParentContainer().getJsonDiscriminatorProperty();
        this.discriminatorValue = grouped.getEffectiveDisciminatorValue();
      } else {
        this.discriminatorProperty = null;
        this.discriminatorValue = null;
      }
      this.key
          = IKey.of(
              instance.getDefinition(),
              jsonKeyFlag == null ? null : jsonKeyFlag.getName(),
              this.discriminatorProperty,
              this.discriminatorValue);
    }

    @NonNull
    protected T getNamedModelInstance() {
      return namedModelInstance;
    }

    @Nullable
    protected IFlagInstance getJsonKeyFlag() {
      return jsonKeyFlag;
    }

    @Nullable
    protected String getJsonKeyFlagName() {
      return jsonKeyFlag == null ? null : jsonKeyFlag.getEffectiveName();
    }

    @Nullable
    protected String getDiscriminatorProperty() {
      return discriminatorProperty;
    }

    @Nullable
    protected String getDiscriminatorValue() {
      return discriminatorValue;
    }

    @Override
    public IDefinitionJsonSchema<IFlagDefinition> getJsonKeyFlagSchema(@NonNull IJsonGenerationState state) {
      IFlagInstance jsonKey = getJsonKeyFlag();
      return jsonKey == null ? null : state.getSchema(IKey.of(jsonKey.getDefinition()));
    }

    @Override
    public IDataTypeJsonSchema getJsonKeyDataTypeSchema(IJsonGenerationState state) {
      IFlagInstance jsonKey = getJsonKeyFlag();
      return jsonKey == null ? null : state.getDataTypeSchemaForDefinition(jsonKey.getDefinition());
    }

    @Override
    public IDefinitionJsonSchema<IModelDefinition> getJsonSchema(IJsonGenerationState state) {
      return state.getSchema(key);
    }

    @Override
    public void build(
        @NonNull ArrayNode anyOf,
        @NonNull IJsonGenerationState state) {
      build(anyOf.addObject(), state);
    }

    @Override
    public void build(
        @NonNull ObjectNode object,
        @NonNull IJsonGenerationState state) {
      IDefinitionJsonSchema<IModelDefinition> schema = getJsonSchema(state);
      schema.generateSchemaOrRef(object, state);
    }
  }

  private static final class AbsoluteType
      extends Type<INamedModelInstanceAbsolute> {

    private AbsoluteType(@NonNull INamedModelInstanceAbsolute namedModelInstance) {
      super(namedModelInstance);
    }
  }

  private static final class GroupedType
      extends Type<INamedModelInstanceGrouped> {

    private GroupedType(@NonNull INamedModelInstanceGrouped namedModelInstance) {
      super(namedModelInstance);
    }
  }
}
