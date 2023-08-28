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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.json.impl.JsonGenerationState;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractDefinitionJsonSchema<D extends IDefinition>
    extends AbstractDefineableJsonSchema {
  @NonNull
  private final D definition;

  protected AbstractDefinitionJsonSchema(@NonNull D definition) {
    this.definition = definition;
  }

  @NonNull
  protected D getDefinition() {
    return definition;
  }

  @Override
  public boolean isInline(JsonGenerationState state) {
    return state.isInline(getDefinition());
  }

  @Override
  protected String generateDefinitionName(JsonGenerationState state) {
    return state.getTypeNameForDefinition(definition, null);
  }

  protected abstract void generateBody(@NonNull JsonGenerationState state, @NonNull ObjectNode obj) throws IOException;

  @Override
  public void generateSchema(JsonGenerationState state, ObjectNode obj) {
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
    String formalName = definition.getEffectiveFormalName();
    if (formalName != null) {
      obj.put("title", formalName);
    }
  }

  public static void generateDescription(@NonNull IDefinition definition, @NonNull ObjectNode obj) {
    MarkupLine description = definition.getDescription();

    StringBuilder retval = null;
    if (description != null) {
      retval = new StringBuilder().append(description.toMarkdown());
    }

    MarkupMultiline remarks = definition.getRemarks();
    if (remarks != null) {
      if (retval == null) {
        retval = new StringBuilder();
      } else {
        retval.append("\n\n");
      }
      retval.append(remarks.toMarkdown());
    }
    if (retval != null) {
      obj.put("description", retval.toString());
    }
  }
}
