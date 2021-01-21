/**
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

package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.ProblemHandler;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyAccessor;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public interface JsonProblemHandler extends ProblemHandler {
  <CLASS> boolean handleUnknownRootProperty(CLASS instance, AssemblyClassBinding<CLASS> classBinding, String fieldName,
      JsonParsingContext parsingContext) throws BindingException, IOException;

  boolean canHandleUnknownProperty(ClassBinding<?> classBinding, String propertyName, JsonParsingContext parsingContext)
      throws BindingException, IOException;

  Map<PropertyAccessor, Supplier<?>> handleUnknownProperty(ClassBinding<?> classBinding, String propertyName,
      JsonParsingContext parsingContext) throws BindingException, IOException;

  /**
   * A callback used to handle bound properties for which no data was found when the content was
   * parsed.
   * <p>
   * This can be used to supply default or prescribed values based on application logic.
   * 
   * @param classBinding
   *          the bound class on which the missing properties are found
   * @param missingPropertyBindings
   *          a map of field names to property bindings for missing fields
   * @param parsingContext
   *          the parser context used for deserialziation
   * @return a mapping of property to suppliers for any properties handled by this method
   * @throws BindingException
   *           if an unhandled binding error has occurred for any reason
   */
  Map<PropertyBinding, Supplier<?>> handleMissingFields(ClassBinding<?> classBinding,
      Map<String, PropertyBinding> missingPropertyBindings, JsonParsingContext parsingContext) throws BindingException;
}
