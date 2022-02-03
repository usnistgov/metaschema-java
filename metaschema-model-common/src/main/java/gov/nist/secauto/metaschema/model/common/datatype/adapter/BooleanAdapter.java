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

package gov.nist.secauto.metaschema.model.common.datatype.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.model.common.datatype.AbstractJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidValueForCastFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BooleanAdapter
    extends AbstractJavaTypeAdapter<Boolean, IBooleanItem> {

  @SuppressWarnings("null")
  public BooleanAdapter() {
    super(Boolean.class);
  }

  @Override
  public String getName() {
    return "boolean";
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
      generator.writeBoolean(((Boolean) value).booleanValue());
    } catch (ClassCastException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public Boolean copy(Object obj) {
    // the value is immutable
    return (Boolean) obj;
  }

  @SuppressWarnings("null")
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
  protected @NotNull IBooleanItem castInternal(@NotNull IAnyAtomicItem item) {
    IBooleanItem retval;
    if (item instanceof INumericItem) {
      retval = castToBoolean((INumericItem) item);
    } else if (item instanceof IStringItem) {
      retval = castToBoolean((IStringItem) item);
    } else {
      retval = castToBoolean((IStringItem) item.newStringItem());
    }
    return retval;
  }

  @NotNull
  protected IBooleanItem castToBoolean(@NotNull INumericItem item) {
    return IBooleanItem.valueOf(item.toEffectiveBoolean());
  }

  @NotNull
  protected IBooleanItem castToBoolean(@NotNull IStringItem item) throws InvalidValueForCastFunctionMetapathException {
    IBooleanItem retval;
    try {
      INumericItem numeric = INumericItem.cast(item);
      retval = castToBoolean(numeric);
    } catch (InvalidValueForCastFunctionMetapathException ex) {
      retval = super.castInternal(item);
    }
    return retval;
  }

}
