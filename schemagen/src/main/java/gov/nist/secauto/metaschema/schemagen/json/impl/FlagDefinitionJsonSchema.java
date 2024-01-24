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

import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.schemagen.json.IDataTypeJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FlagDefinitionJsonSchema
    extends AbstractDefinitionJsonSchema<IFlagDefinition> {
  @NonNull
  private final IKey key;

  public FlagDefinitionJsonSchema(@NonNull IFlagDefinition definition, @NonNull IJsonGenerationState state) {
    super(definition);
    this.key = IKey.of(definition);
    state.getDataTypeSchemaForDefinition(definition);
  }

  @Override
  protected String generateDefinitionName(IJsonGenerationState state) {
    return state.getTypeNameForDefinition(getDefinition(), null);
  }

  @Override
  protected void generateBody(
      IJsonGenerationState state,
      ObjectNode obj) {
    IFlagDefinition definition = getDefinition();
    IDataTypeJsonSchema schema = state.getDataTypeSchemaForDefinition(definition);
    schema.generateSchemaOrRef(obj, state);
  }

  @Override
  public void gatherDefinitions(
      @NonNull Map<IKey, IDefinitionJsonSchema<?>> gatheredDefinitions,
      @NonNull IJsonGenerationState state) {
    super.gatherDefinitions(gatheredDefinitions, state);

    IDataTypeJsonSchema schema = state.getDataTypeSchemaForDefinition(getDefinition());
    if (schema instanceof IDefinitionJsonSchema) {
      ((IDefinitionJsonSchema<?>) schema).gatherDefinitions(gatheredDefinitions, state);
    }
  }

  @Override
  public IKey getKey() {
    return key;
  }
}
