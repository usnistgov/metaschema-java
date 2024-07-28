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

package gov.nist.secauto.metaschema.core.model.xml.xmlbeans.handler;

import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;

public final class ConstraintLevelType {
  private ConstraintLevelType() {
    // disable construction
  }

  /**
   * Sets the value of obj onto the given simple value target.
   *
   * @param obj
   *          the boolean value to set
   * @param target
   *          the XML value to cast to a boolean
   */
  public static void encodeConstraintLevelType(IConstraint.Level obj, org.apache.xmlbeans.SimpleValue target) {
    if (obj != null) {
      switch (obj) {
      case CRITICAL:
        target.setStringValue("CRITICAL");
        break;
      case ERROR:
        target.setStringValue("ERROR");
        break;
      case WARNING:
        target.setStringValue("WARNING");
        break;
      case INFORMATIONAL:
        target.setStringValue("INFORMATIONAL");
        break;
      case DEBUG:
        target.setStringValue("DEBUG");
        break;
      default:
        throw new UnsupportedOperationException(obj.name());
      }
    }
  }

  /**
   * Returns an appropriate Java object from the given simple value.
   *
   * @param obj
   *          the XML value to cast to a boolean
   * @return the associated boolean value
   */
  public static IConstraint.Level decodeConstraintLevelType(org.apache.xmlbeans.SimpleValue obj) {
    String value = obj.getStringValue();
    IConstraint.Level retval;
    switch (value) {
    case "CRITICAL":
      retval = IConstraint.Level.CRITICAL;
      break;
    case "ERROR":
      retval = IConstraint.Level.ERROR;
      break;
    case "WARNING":
      retval = IConstraint.Level.WARNING;
      break;
    case "INFORMATIONAL":
      retval = IConstraint.Level.INFORMATIONAL;
      break;
    case "DEBUG":
      retval = IConstraint.Level.DEBUG;
      break;
    default:
      throw new UnsupportedOperationException(value);
    }
    return retval;
  }
}
