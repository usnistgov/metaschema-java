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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.property.PropertyItemHandler;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.ListPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.MapPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

import java.io.IOException;
import java.util.function.Supplier;

public interface PropertyValueHandler {

  public static <CLASS, CLASS_BINDING extends ClassBinding<CLASS>, PROPERTY_BINDING extends PropertyBinding>
      PropertyValueHandler newPropertyValueHandler(CLASS_BINDING classBinding, PROPERTY_BINDING propertyBinding,
          JsonParsingContext parsingContext) throws BindingException {
    PropertyValueHandler retval;

    PropertyItemHandler propertyItemHandler
        = PropertyItemHandler.newPropertyItemHandler(propertyBinding, parsingContext.getBindingContext());

    PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();
    if (propertyInfo instanceof CollectionPropertyInfo) {
      CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo) propertyInfo;
      if (propertyInfo instanceof ListPropertyInfo) {
        retval = new ListPropertyValueHandler(classBinding, propertyItemHandler,
            JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(collectionPropertyInfo.getJsonGroupAsBehavior()));
      } else if (propertyInfo instanceof MapPropertyInfo) {
        retval = new MapPropertyValueHandler(classBinding, propertyItemHandler, parsingContext.getBindingContext());
      } else {
        throw new BindingException(
            String.format("Unsupported collection type '%s'.", collectionPropertyInfo.getClass().getSimpleName()));
      }
    } else {
      retval = new SingletonPropertyValueHandler(classBinding, propertyItemHandler);
    }
    return retval;
  }

  /**
   * Parses the next value.
   * <p>
   * When called, the current token is expected to be at the value which may be a
   * {@link JsonToken#START_ARRAY} for an array of values, a {@link JsonToken#START_OBJECT} for a
   * single object, or one of the {@link JsonToken} value types.
   * <p>
   * After this call completes the parser's current token is expected either at the start of the next
   * value or at the end of the value sequence.
   * <p>
   * If at the start of the next value, the current token will be a {@link JsonToken#START_OBJECT} for
   * the next object in an array, or one of the {@link JsonToken} value types.
   * <p>
   * Once the end of the value sequence is reached and all values have been parsed, the current token
   * is expected to be at the {@link JsonToken#END_ARRAY} if the inside an array, or otherwise the
   * {@link JsonToken#END_OBJECT} for this object.
   * <p>
   * 
   * @param parsingContext
   *          the collection of objects used for parsing
   * @return {@code true} if there are additional fields to parse, meaning that the end of the value
   *         sequence has not been seen, or {@code false} otherwise.
   * @throws BindingException
   *           if a binding error has occurred
   * @throws IOException
   *           if an input error has occurred while parsing
   */
  boolean parseNextFieldValue(Object parent, JsonParsingContext parsingContext) throws BindingException, IOException;

  Supplier<? extends Object> getObjectSupplier();
}
