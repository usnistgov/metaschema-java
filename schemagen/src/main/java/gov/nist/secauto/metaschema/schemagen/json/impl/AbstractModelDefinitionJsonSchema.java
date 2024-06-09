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

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.IGenerationState;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractModelDefinitionJsonSchema<D extends IModelDefinition>
    extends AbstractDefinitionJsonSchema<D> {
  @Nullable
  private final String jsonKeyFlagName;
  @Nullable
  private final String discriminatorProperty;
  @Nullable
  private final String discriminatorValue;
  @NonNull
  private final List<FlagInstanceJsonProperty> flagProperties;
  @NonNull
  private final IKey key;

  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  protected AbstractModelDefinitionJsonSchema(
      @NonNull D definition,
      @Nullable String jsonKeyFlagName,
      @Nullable String discriminatorProperty,
      @Nullable String discriminatorValue) {
    super(definition);
    this.jsonKeyFlagName = jsonKeyFlagName;
    this.discriminatorProperty = discriminatorProperty;
    this.discriminatorValue = discriminatorValue;
    this.key = IKey.of(definition, jsonKeyFlagName, discriminatorProperty, discriminatorValue);

    Stream<? extends IFlagInstance> flagStream = definition.getFlagInstances().stream();

    // determine the flag instances to generate
    if (jsonKeyFlagName != null) {
      IFlagInstance jsonKeyFlag = definition.getFlagInstanceByName(
          definition.getContainingModule().toFlagQName(jsonKeyFlagName));
      if (jsonKeyFlag == null) {
        throw new IllegalArgumentException(
            String.format("The referenced json-key flag-name '%s' does not exist on definition '%s'.",
                jsonKeyFlagName,
                definition.getName()));
      }
      flagStream = flagStream.filter(instance -> instance != jsonKeyFlag);
    }

    this.flagProperties = ObjectUtils.notNull(flagStream
        .map(instance -> new FlagInstanceJsonProperty(ObjectUtils.requireNonNull(instance)))
        .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public IKey getKey() {
    return key;
  }

  protected String getJsonKeyFlagName() {
    return jsonKeyFlagName;
  }

  protected String getDiscriminatorProperty() {
    return discriminatorProperty;
  }

  protected String getDiscriminatorValue() {
    return discriminatorValue;
  }

  @Override
  protected String generateDefinitionName(IJsonGenerationState state) {
    IModelDefinition definition = getDefinition();
    StringBuilder builder = new StringBuilder();
    if (jsonKeyFlagName != null) {
      builder
          .append(IGenerationState.toCamelCase(jsonKeyFlagName))
          .append("JsonKey");
    }

    if (discriminatorProperty != null || discriminatorValue != null) {
      builder
          .append(IGenerationState.toCamelCase(ObjectUtils.requireNonNull(discriminatorProperty)))
          .append(IGenerationState.toCamelCase(ObjectUtils.requireNonNull(discriminatorValue)))
          .append("Choice");
    }
    return state.getTypeNameForDefinition(
        definition,
        builder.toString());
  }

  protected List<FlagInstanceJsonProperty> getFlagProperties() {
    return flagProperties;
  }

  @Override
  public void gatherDefinitions(
      @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
      @NonNull IJsonGenerationState state) {
    super.gatherDefinitions(gatheredDefinitions, state);

    for (FlagInstanceJsonProperty property : getFlagProperties()) {
      property.gatherDefinitions(gatheredDefinitions, state);
    }
  }

  public static class ComplexKey implements IKey {
    @NonNull
    private final IDefinition definition;
    @Nullable
    private final String jsonKeyFlagName;
    @Nullable
    private final String discriminatorProperty;
    @Nullable
    private final String discriminatorValue;

    public ComplexKey(
        @NonNull IDefinition definition,
        @Nullable String jsonKeyFlagName,
        @Nullable String discriminatorProperty,
        @Nullable String discriminatorValue) {
      this.definition = definition;
      this.jsonKeyFlagName = jsonKeyFlagName;
      this.discriminatorProperty = discriminatorProperty;
      this.discriminatorValue = discriminatorValue;
    }

    @Override
    @NonNull
    public IDefinition getDefinition() {
      return definition;
    }

    @Override
    @Nullable
    public String getJsonKeyFlagName() {
      return jsonKeyFlagName;
    }

    @Override
    public String getDiscriminatorProperty() {
      return discriminatorProperty;
    }

    @Override
    public String getDiscriminatorValue() {
      return discriminatorValue;
    }

    @Override
    public int hashCode() {
      return Objects.hash(definition, jsonKeyFlagName, discriminatorProperty, discriminatorValue);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof IKey)) {
        return false;
      }
      IKey other = (IKey) obj;
      return Objects.equals(definition, other.getDefinition())
          && Objects.equals(jsonKeyFlagName, other.getJsonKeyFlagName())
          && Objects.equals(discriminatorProperty, other.getDiscriminatorProperty())
          && Objects.equals(discriminatorValue, other.getDiscriminatorValue());
    }
  }
}
