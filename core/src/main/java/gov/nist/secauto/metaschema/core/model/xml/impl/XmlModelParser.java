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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import gov.nist.secauto.metaschema.core.model.IGroupable;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupAsType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigInteger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

//@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class XmlModelParser {
  private XmlModelParser() {
    // disable construction
  }

  /**
   * Get the group-as/@in-json value based on the XMLBeans representation.
   *
   * @param groupAs
   *          the XMLBeans value
   * @return the in-json value
   */
  @NonNull
  public static JsonGroupAsBehavior getJsonGroupAsBehavior(@Nullable GroupAsType groupAs) {
    JsonGroupAsBehavior retval = IGroupable.DEFAULT_JSON_GROUP_AS_BEHAVIOR;
    if (groupAs != null && groupAs.isSetInJson()) {
      retval = ObjectUtils.notNull(groupAs.getInJson());
    }
    return retval;
  }

  /**
   * Get the group-as/@in-xml value based on the XMLBeans representation.
   *
   * @param groupAs
   *          the XMLBeans value
   * @return the in-xml value
   */
  @NonNull
  public static XmlGroupAsBehavior getXmlGroupAsBehavior(@Nullable GroupAsType groupAs) {
    XmlGroupAsBehavior retval = IGroupable.DEFAULT_XML_GROUP_AS_BEHAVIOR;
    if (groupAs != null && groupAs.isSetInXml()) {
      retval = ObjectUtils.notNull(groupAs.getInXml());
    }
    return retval;
  }

  /**
   * Convert the XMLBeans max occurrence to an integer value.
   *
   * @param value
   *          the XMLBeans value
   * @return the integer value
   */
  public static int getMinOccurs(@Nullable BigInteger value) {
    int retval = IGroupable.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (value != null) {
      retval = value.intValueExact();
    }
    return retval;
  }

  /**
   * Convert the XMLBeans max occurrence to an integer value.
   * <p>
   * If the source value is "unbounded", the the value {@code -1} is used.
   *
   * @param value
   *          the XMLBeans value
   * @return the integer value
   */
  public static int getMaxOccurs(@Nullable Object value) {
    int retval = IGroupable.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (value != null) {
      if (value instanceof String) {
        // must be "unbounded"
        retval = -1;
      } else if (value instanceof BigInteger) {
        retval = ((BigInteger) value).intValueExact();
      } else {
        throw new IllegalStateException("Invalid type: " + value.getClass().getName());
      }
    }
    return retval;
  }
}
