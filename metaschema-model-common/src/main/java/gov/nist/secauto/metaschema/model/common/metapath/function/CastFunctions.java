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

package gov.nist.secauto.metaschema.model.common.metapath.function;

import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INcNameItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INonNegativeIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IPositiveIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;

public class CastFunctions {
  private CastFunctions() {
    // disable
  }

  public static IStringItem castToString(IAnyAtomicItem item) {
    IStringItem retval;
    if (item instanceof IStringItem) {
      retval = (IStringItem) item;
    } else {
      retval = item.newStringItem();
    }
    return retval;
  }

  public static IBooleanItem castToBoolean(INumericItem item) {
    return IBooleanItem.valueOf(item.toEffectiveBoolean());
  }

  public static IBooleanItem castToBoolean(IStringItem item) throws InvalidValueForCastException {
    IBooleanItem retval;
    try {
      INumericItem numeric = castToNumeric(item);
      retval = castToBoolean(numeric);
    } catch (InvalidValueForCastException ex) {
      retval = IBooleanItem.valueOf(item.asString());
    }
    return retval;
  }

  public static IBooleanItem castToBoolean(IAnyAtomicItem item) {
    IBooleanItem retval;
    if (item instanceof IBooleanItem) {
      retval = (IBooleanItem) item;
    } else if (item instanceof INumericItem) {
      retval = castToBoolean((INumericItem) item);
    } else if (item instanceof IStringItem) {
      retval = castToBoolean((IStringItem) item);
    } else {
      retval = castToBoolean((IStringItem) item.newStringItem());
    }
    return retval;
  }

  public static IDecimalItem castToDecimal(IAnyAtomicItem item) throws InvalidValueForCastException {
    IDecimalItem retval;
    if (item instanceof INumericItem) {
      if (item instanceof IDecimalItem) {
        retval = (IDecimalItem) item;
      } else {
        // must be an integer type
        retval = IDecimalItem.valueOf(((IIntegerItem) item).asDecimal());
      }
    } else if (item instanceof IBooleanItem) {
      boolean value = ((IBooleanItem) item).toBoolean();
      retval = IDecimalItem.valueOf(value ? 1.0 : 0.0);
    } else {
      try {
        retval = IDecimalItem.valueOf(item.asString());
      } catch (NumberFormatException ex) {
        throw new InvalidValueForCastException(ex);
      }
    }
    return retval;
  }

  public static IDurationItem castToDuration(IAnyAtomicItem item) throws InvalidValueForCastException {
    IDurationItem retval;
    if (item instanceof IDurationItem) {
      retval = (IDurationItem)item;
    } else {
      try {
        retval = IDayTimeDurationItem.valueOf(item.asString());
      } catch (IllegalArgumentException ex) {
        try {
          retval = IYearMonthDurationItem.valueOf(item.asString());
        } catch(IllegalArgumentException ex2) {
          throw new InvalidValueForCastException(ex2);
        }
      }
    }
    return retval;
  }

  public static IDateTimeItem castToDateTime(IAnyAtomicItem item) {
    // TODO: bring up to spec
    IDateTimeItem retval;
    if (item instanceof IDateTimeItem) {
      retval = (IDateTimeItem) item;
    } else if (item instanceof IDateItem) {
      retval = IDateTimeItem.valueOf(((IDateItem) item).asZonedDateTime());
    } else if (item instanceof IStringItem || item instanceof IUntypedAtomicItem) {
      retval = IDateTimeItem.valueOf(item.asString());
    } else {
      throw new UnsupportedOperationException();
    }
    return retval;
  }

  // TODO: time?

  public static IDateItem castToDate(IAnyAtomicItem item) {
    // TODO: bring up to spec
    IDateItem retval;
    if (item instanceof IDateItem) {
      retval = (IDateItem) item;
    } else if (item instanceof IDateTimeItem) {
      retval = IDateItem.valueOf(((IDateTimeItem) item).asZonedDateTime());
    } else if (item instanceof IStringItem || item instanceof IUntypedAtomicItem) {
      retval = IDateItem.valueOf(item.asString());
    } else {
      throw new UnsupportedOperationException();
    }
    return retval;
  }

  // public static IBase64BinaryItem base64Binary(IAnyAtomicItem item) {
  //
  // }
  //
  // public static IAnyUriItem anyUri(IAnyAtomicItem item) {
  //
  // }
  //
  // public static ITokenItem token(IAnyAtomicItem item) {
  //
  // }

  public static INcNameItem castToNcName(IAnyAtomicItem item) {
    return INcNameItem.valueOf(item.asString());
  }

  public static IIntegerItem castToInteger(IAnyAtomicItem item) throws InvalidValueForCastException {
    IIntegerItem retval;
    if (item instanceof INumericItem) {
      if (item instanceof IIntegerItem) {
        retval = (IIntegerItem) item;
      } else {
        // must be a decimal type
        retval = IIntegerItem.valueOf(((IDecimalItem) item).asInteger());
      }
    } else if (item instanceof IBooleanItem) {
      boolean value = ((IBooleanItem) item).toBoolean();
      retval = value ? IIntegerItem.ONE : IIntegerItem.ZERO;
    } else {
      try {
        retval = IIntegerItem.valueOf(item.asString());
      } catch (NumberFormatException ex) {
        throw new InvalidValueForCastException(ex);
      }
    }
    return retval;

  }

  public static INumericItem castToNumeric(IAnyAtomicItem item) throws InvalidValueForCastException {
    INumericItem retval;
    if (item instanceof INumericItem) {
      retval = (INumericItem) item;
    } else {
      try {
        retval = IDecimalItem.valueOf(item.asString());
      } catch (NumberFormatException ex) {
        throw new InvalidValueForCastException(ex);
      }
    }
    return retval;
  }

  public static INonNegativeIntegerItem castToNonNegativeInteger(IAnyAtomicItem item)
      throws InvalidValueForCastException {
    IIntegerItem integerItem = castToInteger(item);
    return INonNegativeIntegerItem.valueOf(integerItem);
  }

  public static IPositiveIntegerItem castToPositiveInteger(IAnyAtomicItem item) throws InvalidValueForCastException {
    IIntegerItem integerItem = castToInteger(item);
    return IPositiveIntegerItem.valueOf(integerItem);
  }
}
