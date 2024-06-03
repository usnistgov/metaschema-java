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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IModelElement;
import gov.nist.secauto.metaschema.core.model.INamedModelElement;
import gov.nist.secauto.metaschema.core.model.IValuedDefinition;
import gov.nist.secauto.metaschema.core.model.IValuedInstance;

import java.math.BigDecimal;
import java.math.BigInteger;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class MetadataUtils {
  private MetadataUtils() {
    // disable construction
  }

  public static void generateTitle(@NonNull INamedModelElement named, @NonNull ObjectNode obj) {
    String formalName = named.getEffectiveFormalName();
    if (formalName != null) {
      obj.put("title", formalName);
    }
  }

  public static <NAMED extends INamedModelElement & IModelElement> void generateDescription(@NonNull NAMED named,
      @NonNull ObjectNode obj) {
    MarkupLine description = named.getEffectiveDescription();

    StringBuilder retval = null;
    if (description != null) {
      retval = new StringBuilder().append(description.toMarkdown());
    }

    MarkupMultiline remarks = named.getRemarks();
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

  public static void generateDefault(IValuedInstance instance, ObjectNode obj) {
    Object defaultValue = instance.getEffectiveDefaultValue();
    if (defaultValue != null) {
      IValuedDefinition definition = instance.getDefinition();
      IDataTypeAdapter<?> adapter = definition.getJavaTypeAdapter();
      obj.set("default", toJsonValue(defaultValue, adapter));
    }
  }

  private static JsonNode toJsonValue(Object defaultValue, IDataTypeAdapter<?> adapter) {
    JsonNode retval = null;
    switch (adapter.getJsonRawType()) {
    case BOOLEAN:
      if (defaultValue instanceof Boolean) {
        retval = BooleanNode.valueOf((Boolean) defaultValue);
      } // else use default conversion
      break;
    case INTEGER:
      if (defaultValue instanceof BigInteger) {
        retval = BigIntegerNode.valueOf((BigInteger) defaultValue);
      } else if (defaultValue instanceof Integer) {
        retval = IntNode.valueOf((Integer) defaultValue);
      } else if (defaultValue instanceof Long) {
        retval = LongNode.valueOf((Long) defaultValue);
      } // else use default conversion
      break;
    case NUMBER:
      if (defaultValue instanceof BigDecimal) {
        retval = DecimalNode.valueOf((BigDecimal) defaultValue);
      } else if (defaultValue instanceof Double) {
        retval = DoubleNode.valueOf((Double) defaultValue);
      } // else use default conversion
      break;
    case ANY:
    case ARRAY:
    case OBJECT:
    case NULL:
      throw new UnsupportedOperationException("Invalid type: " + defaultValue.getClass());
    case STRING:
    default:
      // use default conversion
      break;
    }

    if (retval == null) {
      retval = TextNode.valueOf(adapter.asString(defaultValue));
    }
    return retval;
  }
}
