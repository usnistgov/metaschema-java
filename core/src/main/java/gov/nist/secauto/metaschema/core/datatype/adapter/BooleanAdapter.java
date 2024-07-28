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

package gov.nist.secauto.metaschema.core.datatype.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;

import gov.nist.secauto.metaschema.core.datatype.AbstractDataTypeAdapter;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BooleanAdapter
    extends AbstractDataTypeAdapter<Boolean, IBooleanItem> {
  @NonNull
  private static final List<QName> NAMES = ObjectUtils.notNull(
      List.of(new QName(MetapathConstants.NS_METAPATH.toASCIIString(), "boolean")));

  BooleanAdapter() {
    super(Boolean.class);
  }

  @Override
  public List<QName> getNames() {
    return NAMES;
  }

  @Override
  public JsonFormatTypes getJsonRawType() {
    return JsonFormatTypes.BOOLEAN;
  }

  @SuppressWarnings("null")
  @Override
  public Boolean parse(String value) {
    return Boolean.valueOf(value);
  }

  @Override
  public Boolean parse(JsonParser parser) throws IOException {
    Boolean value = parser.getBooleanValue();
    // skip over value
    parser.nextToken();
    return value;
  }

  @Override
  public void writeJsonValue(Object value, JsonGenerator generator)
      throws IOException {
    try {
      generator.writeBoolean((Boolean) value);
    } catch (ClassCastException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public Boolean copy(Object obj) {
    // the value is immutable
    return (Boolean) obj;
  }

  @Override
  public Class<IBooleanItem> getItemClass() {
    return IBooleanItem.class;
  }

  @Override
  public IBooleanItem newItem(Object value) {
    Boolean item = toValue(value);
    return IBooleanItem.valueOf(item);
  }

  @Override
  protected IBooleanItem castInternal(@NonNull IAnyAtomicItem item) {
    IBooleanItem retval;
    if (item instanceof INumericItem) {
      retval = castToBoolean((INumericItem) item);
    } else if (item instanceof IStringItem) {
      retval = castToBoolean((IStringItem) item);
    } else {
      retval = castToBoolean(item.asStringItem());
    }
    return retval;
  }

  /**
   * Cast the provided numeric value to a boolean. Any non-zero value will be
   * {@code true}, or {@code false} otherwise.
   *
   * @param item
   *          the item to cast
   * @return {@code true} if the item value is non-zero, or {@code false}
   *         otherwise
   */
  @NonNull
  protected IBooleanItem castToBoolean(@NonNull INumericItem item) {
    return IBooleanItem.valueOf(item.toEffectiveBoolean());
  }

  /**
   * If the string is a numeric value, treat it as so. Otherwise parse the value
   * as a boolean string.
   *
   * @param item
   *          the item to cast
   * @return the effective boolean value of the string
   * @throws InvalidValueForCastFunctionException
   *           if the provided item cannot be cast to a boolean value by any means
   */
  @NonNull
  protected IBooleanItem castToBoolean(@NonNull IStringItem item) {
    IBooleanItem retval;
    try {
      INumericItem numeric = INumericItem.cast(item);
      retval = castToBoolean(numeric);
    } catch (InvalidValueForCastFunctionException ex) {
      retval = super.castInternal(item);
    }
    return retval;
  }

}
