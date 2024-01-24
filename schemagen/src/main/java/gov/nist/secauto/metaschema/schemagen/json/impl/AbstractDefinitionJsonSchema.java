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

import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractDefinitionJsonSchema<D extends IDefinition>
    extends AbstractDefineableJsonSchema
    implements IDefinitionJsonSchema<D> {
  @NonNull
  private final D definition;

  @Override
  public D getDefinition() {
    return definition;
  }

  protected AbstractDefinitionJsonSchema(
      @NonNull D definition) {
    this.definition = definition;
  }

  @Override
  public boolean isInline(IJsonGenerationState state) {
    return state.isInline(getDefinition());
  }

  protected abstract void generateBody(
      @NonNull IJsonGenerationState state,
      @NonNull ObjectNode obj) throws IOException;

  @Override
  public void generateInlineSchema(ObjectNode obj, IJsonGenerationState state) {
    D definition = getDefinition();

    try {
      generateTitle(definition, obj);
      generateDescription(definition, obj);

      generateBody(state, obj);
    } catch (IOException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  public static void generateTitle(@NonNull IDefinition definition, @NonNull ObjectNode obj) {
    MetadataUtils.generateTitle(definition, obj);
  }

  public static void generateDescription(@NonNull IDefinition definition, @NonNull ObjectNode obj) {
    MetadataUtils.generateDescription(definition, obj);
  }

  @Override
  public void gatherDefinitions(
      @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
      @NonNull IJsonGenerationState state) {
    gatheredDefinitions.put(getKey(), this);
  }

  public static class SimpleKey implements IKey {
    @NonNull
    private final IDefinition definition;

    public SimpleKey(@NonNull IDefinition definition) {
      this.definition = definition;
    }

    @Override
    public IDefinition getDefinition() {
      return definition;
    }

    @Override
    public String getJsonKeyFlagName() {
      return null;
    }

    @Override
    public String getDiscriminatorProperty() {
      return null;
    }

    @Override
    public String getDiscriminatorValue() {
      return null;
    }

    @Override
    public int hashCode() {
      return Objects.hash(definition, null, null, null);
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
          && Objects.equals(null, other.getJsonKeyFlagName())
          && Objects.equals(null, other.getDiscriminatorProperty())
          && Objects.equals(null, other.getDiscriminatorValue());
    }
  }

}
