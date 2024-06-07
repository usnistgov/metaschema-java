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

import gov.nist.secauto.metaschema.core.datatype.AbstractDataTypeAdapter;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DateWithTZAdapter
    extends AbstractDataTypeAdapter<ZonedDateTime, IDateItem> {
  @NonNull
  private static final List<QName> NAMES = ObjectUtils.notNull(
      List.of(
          new QName(MetapathConstants.NS_METAPATH.toASCIIString(), "date-with-timezone")));
  private static final Pattern DATE_TIMEZONE = Pattern.compile("^("
      + "^(?:(?:2000|2400|2800|(?:19|2[0-9](?:0[48]|[2468][048]|[13579][26])))-02-29)"
      + "|(?:(?:(?:19|2[0-9])[0-9]{2})-02-(?:0[1-9]|1[0-9]|2[0-8]))"
      + "|(?:(?:(?:19|2[0-9])[0-9]{2})-(?:0[13578]|10|12)-(?:0[1-9]|[12][0-9]|3[01]))"
      + "|(?:(?:(?:19|2[0-9])[0-9]{2})-(?:0[469]|11)-(?:0[1-9]|[12][0-9]|30))"
      + ")"
      + "(Z|[+-][0-9]{2}:[0-9]{2})$");

  DateWithTZAdapter() {
    super(ZonedDateTime.class);
  }

  @Override
  public List<QName> getNames() {
    return NAMES;
  }

  @Override
  public JsonFormatTypes getJsonRawType() {
    return JsonFormatTypes.STRING;
  }

  @SuppressWarnings("null")
  @Override
  public ZonedDateTime parse(String value) {
    Matcher matcher = DATE_TIMEZONE.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid date: " + value);
    }
    String parseValue = String.format("%sT00:00:00%s", matcher.group(1), matcher.group(2));
    try {
      return ZonedDateTime.from(DateFormats.DATE_TIME_WITH_TZ.parse(parseValue));
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException(ex.getLocalizedMessage(), ex);
    }
  }

  @SuppressWarnings("null")
  @Override
  public String asString(Object value) {
    return DateFormats.DATE_WITH_TZ.format((ZonedDateTime) value);
  }

  @SuppressWarnings("null")
  @Override
  public ZonedDateTime copy(Object obj) {
    return ZonedDateTime.from((ZonedDateTime) obj);
  }

  @Override
  public Class<IDateItem> getItemClass() {
    return IDateItem.class;
  }

  @Override
  public IDateItem newItem(Object value) {
    ZonedDateTime item = toValue(value);
    return IDateItem.valueOf(item);
  }
}
