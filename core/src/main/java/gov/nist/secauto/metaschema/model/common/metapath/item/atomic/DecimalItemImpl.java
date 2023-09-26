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

package gov.nist.secauto.metaschema.model.common.metapath.item.atomic;

import gov.nist.secauto.metaschema.model.common.datatype.adapter.DecimalAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import edu.umd.cs.findbugs.annotations.NonNull;

class DecimalItemImpl
    extends AbstractNumericItem<BigDecimal>
    implements IDecimalItem {
  public DecimalItemImpl(@NonNull BigDecimal value) {
    super(value);
  }

  @Override
  public DecimalAdapter getJavaTypeAdapter() {
    return MetaschemaDataTypeProvider.DECIMAL;
  }

  @SuppressWarnings("null")
  @Override
  public String asString() {
    BigDecimal decimal = getValue();
    // if the fractional part is empty, render as an integer
    return decimal.scale() <= 0 ? decimal.toBigIntegerExact().toString() : decimal.toPlainString();
  }

  @Override
  public boolean toEffectiveBoolean() {
    return !BigDecimal.ZERO.equals(getValue());
  }

  @Override
  public BigDecimal asDecimal() {
    return getValue();
  }

  @SuppressWarnings("null")
  @Override
  public BigInteger asInteger() {
    return getValue().toBigInteger();
  }

  @SuppressWarnings("null")
  @Override
  public INumericItem abs() {
    return new DecimalItemImpl(getValue().abs());
  }

  @SuppressWarnings("null")
  @Override
  public IIntegerItem ceiling() {
    return IIntegerItem.valueOf(getValue().setScale(0, RoundingMode.CEILING).toBigIntegerExact());
  }

  @SuppressWarnings("null")
  @Override
  public IIntegerItem floor() {
    return IIntegerItem.valueOf(getValue().setScale(0, RoundingMode.FLOOR).toBigIntegerExact());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getValue().hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true; // NOPMD readability
    } else if (obj == null) {
      return false; // NOPMD readability
    } else if (getClass() != obj.getClass()) {
      return false; // NOPMD readability
    }
    DecimalItemImpl other = (DecimalItemImpl) obj;
    return getValue().equals(other.getValue());
  }

}
