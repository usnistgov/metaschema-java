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

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;

import gov.nist.secauto.metaschema.core.datatype.AbstractCustomJavaDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.object.DateTime;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DateTimeAdapter
    extends AbstractCustomJavaDataTypeAdapter<DateTime, IDateTimeItem> {
  @NonNull
  private static final List<String> NAMES = ObjectUtils.notNull(
      List.of(
          "date-time",
          // for backwards compatibility with original type name
          "dateTime"));

  DateTimeAdapter() {
    super(DateTime.class);
  }

  @Override
  public List<String> getNames() {
    return NAMES;
  }

  @Override
  public JsonFormatTypes getJsonRawType() {
    return JsonFormatTypes.STRING;
  }

  @SuppressWarnings("null")
  @Override
  public DateTime parse(String value) {
    try {
      return new DateTime(ZonedDateTime.from(DateFormats.DATE_TIME_WITH_TZ.parse(value)), true); // NOPMD - readability
    } catch (DateTimeParseException ex) {
      try {
        LocalDateTime dateTime = LocalDateTime.from(DateFormats.DATE_TIME_WITHOUT_TZ.parse(value));
        return new DateTime(ZonedDateTime.of(dateTime, ZoneOffset.UTC), false);
      } catch (DateTimeParseException ex2) {
        IllegalArgumentException newEx = new IllegalArgumentException(ex2.getLocalizedMessage(), ex2);
        newEx.addSuppressed(ex);
        throw newEx; // NOPMD - it's ok
      }
    }
  }

  @Override
  public String asString(Object obj) {
    DateTime value = (DateTime) obj;
    String retval;
    if (value.hasTimeZone()) {
      @SuppressWarnings("null")
      @NonNull String formatted = DateFormats.DATE_TIME_WITH_TZ.format(value.getValue());
      retval = formatted;
    } else {
      @SuppressWarnings("null")
      @NonNull String formatted = DateFormats.DATE_TIME_WITHOUT_TZ.format(value.getValue());
      retval = formatted;
    }
    return retval;
  }

  @Override
  public Class<IDateTimeItem> getItemClass() {
    return IDateTimeItem.class;
  }

  @Override
  public IDateTimeItem newItem(Object value) {
    DateTime item = toValue(value);
    return IDateTimeItem.valueOf(item);
  }

  @Override
  protected IDateTimeItem castInternal(@NonNull IAnyAtomicItem item) {
    // TODO: bring up to spec
    IDateTimeItem retval;
    if (item instanceof IDateItem) {
      retval = IDateTimeItem.valueOf(((IDateItem) item).asZonedDateTime());
    } else if (item instanceof IStringItem || item instanceof IUntypedAtomicItem) {
      retval = super.castInternal(item);
    } else {
      throw new InvalidValueForCastFunctionException(
          String.format("unsupported item type '%s'", item.getClass().getName()));
    }
    return retval;
  }

}
