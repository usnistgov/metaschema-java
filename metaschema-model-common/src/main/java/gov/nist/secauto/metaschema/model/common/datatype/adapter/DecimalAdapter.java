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

import gov.nist.secauto.metaschema.model.common.datatype.AbstractJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidValueForCastFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.IDecimalType;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;

public class DecimalAdapter extends AbstractJavaTypeAdapter<BigDecimal, IDecimalItem> implements IDecimalType {
  @NotNull
  private static final BigDecimal DECIMAL_BOOLEAN_TRUE = new BigDecimal("1.0");
  @NotNull
  private static final BigDecimal DECIMAL_BOOLEAN_FALSE = new BigDecimal("0.0");

  @SuppressWarnings("null")
  public DecimalAdapter() {
    super(BigDecimal.class);
  }

  @Override
  public String getName() {
    return "decimal";
  }

  @Override
  public BigDecimal parse(String value) throws IllegalArgumentException {
    return new BigDecimal(value);
  }

  @Override
  public void writeJsonValue(Object value, JsonGenerator generator) throws IOException {
    try {
      generator.writeNumber((BigDecimal) value);
    } catch (ClassCastException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public BigDecimal copy(Object obj) {
    // a BigDecimal is immutable
    return (BigDecimal)obj;
  }

  @SuppressWarnings("null")
  @Override
  public @NotNull Class<IDecimalItem> getItemClass() {
    return IDecimalItem.class;
  }

  @Override
  public IDecimalItem newItem(Object value) {
    BigDecimal item = toValue(value);
    return new DecimalItemImpl(item);
  }

  @Override
  protected @NotNull IDecimalItem castInternal(@NotNull IAnyAtomicItem item)
      throws InvalidValueForCastFunctionMetapathException {
    IDecimalItem retval;
    if (item instanceof INumericItem) {
      if (item instanceof IDecimalItem) {
        retval = (IDecimalItem) item;
      } else {
        // must be an integer type
        retval = newItem(((IIntegerItem) item).asDecimal());
      }
    } else if (item instanceof IBooleanItem) {
      boolean value = ((IBooleanItem) item).toBoolean();
      retval = newItem(value ? DECIMAL_BOOLEAN_TRUE : DECIMAL_BOOLEAN_FALSE);
    } else {
      retval = super.castInternal(item);
    }
    return retval;
  }
}
