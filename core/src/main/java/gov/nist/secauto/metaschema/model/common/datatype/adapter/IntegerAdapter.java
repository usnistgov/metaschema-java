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

import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.math.BigInteger;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class IntegerAdapter
    extends AbstractIntegerAdapter<IIntegerItem> {
  @NonNull
  private static final List<String> NAMES = ObjectUtils.notNull(
      List.of("integer"));

  IntegerAdapter() {
    // avoid general construction
  }

  @Override
  public List<String> getNames() {
    return NAMES;
  }

  @Override
  public @NonNull Class<IIntegerItem> getItemClass() {
    return IIntegerItem.class;
  }

  @Override
  public IIntegerItem newItem(Object value) {
    BigInteger item = toValue(value);
    return IIntegerItem.valueOf(item);
  }

  @Override
  protected IIntegerItem castInternal(@NonNull IAnyAtomicItem item) {
    IIntegerItem retval;
    if (item instanceof INumericItem) {
      if (item instanceof IIntegerItem) {
        retval = (IIntegerItem) item;
      } else {
        // must be a decimal type
        retval = newItem(((IDecimalItem) item).asInteger());
      }
    } else if (item instanceof IBooleanItem) {
      boolean value = ((IBooleanItem) item).toBoolean();
      retval = value ? IIntegerItem.ONE : IIntegerItem.ZERO;
    } else {
      retval = super.castInternal(item);
    }
    return retval;
  }
}
