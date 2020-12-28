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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.Property;
import gov.nist.secauto.metaschema.binding.model.property.info.JsonBindingSupplier;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultJsonProblemHandler implements JsonProblemHandler {
  private static final String JSON_SCHEMA_ROOT_FIELD_NAME = "$schema";
  private static final Set<String> ignoredRootFieldNames;

  static {
    ignoredRootFieldNames = new HashSet<>();
    ignoredRootFieldNames.add(JSON_SCHEMA_ROOT_FIELD_NAME);
  }

  // TODO: implement this
  @Override
  public boolean handleUnknownRootProperty(Object instance, AssemblyClassBinding classBinding, String fieldName,
      JsonParsingContext parsingContext) throws BindingException, IOException {
    if (ignoredRootFieldNames.contains(fieldName)) {
      JsonParser parser = parsingContext.getReader();
      try {
        JsonUtil.skipNextValue(parser);
      } catch (IOException ex) {
        throw new BindingException(ex);
      }
      return true;
    }
    return false;
  }

  // TODO: implement this
  @Override
  public boolean canHandleUnknownProperty(ClassBinding classBinding, String propertyName,
      JsonParsingContext parsingContext) throws IOException {
    return false;
  }

  // TODO: implement this
  @Override
  public Map<Property, JsonBindingSupplier> handleUnknownProperty(ClassBinding classBinding, String propertyName,
      JsonParsingContext parsingContext) throws BindingException, IOException {
    return Collections.emptyMap();
  }

  // TODO: implement this
  @Override
  public Map<Property, JsonBindingSupplier> handleMissingFields(ClassBinding classBinding,
      Map<String, Property> missingPropertyBindings, JsonParsingContext context) throws BindingException {
    return Collections.emptyMap();
  }

}
