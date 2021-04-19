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

package gov.nist.secauto.metaschema.model.instances;

import gov.nist.itl.metaschema.model.m4.xml.XmlGroupBehavior;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum XmlGroupAsBehavior {
  /**
   * In XML, child element instances will be wrapped by a grouping element.
   */
  GROUPED(XmlGroupBehavior.GROUPED),
  /**
   * In XML, child element instances will exist in an unwrapped form.
   */
  UNGROUPED(XmlGroupBehavior.UNGROUPED);

  private static final Map<XmlGroupBehavior.Enum, XmlGroupAsBehavior> modelToEnumMap;

  static {
    Map<XmlGroupBehavior.Enum, XmlGroupAsBehavior> map = new HashMap<>();
    for (XmlGroupAsBehavior e : values()) {
      map.put(e.getModelValue(), e);
    }
    modelToEnumMap = Collections.unmodifiableMap(map);
  }

  /**
   * Lookup the XML group-as behavior based on the provided XML schema bound enum value.
   * 
   * @param value
   *          the XML schema bound enum value to lookup
   * @return the XML group-as behavior
   */
  public static XmlGroupAsBehavior lookup(XmlGroupBehavior.Enum value) {
    return modelToEnumMap.get(value);
  }

  private final XmlGroupBehavior.Enum modelValue;

  XmlGroupAsBehavior(XmlGroupBehavior.Enum modelValue) {
    this.modelValue = modelValue;
  }

  /**
   * Get the associated bound JSON group-as enum value from the Metaschema XML schema.
   * 
   * @return the bound XML enum
   */
  protected XmlGroupBehavior.Enum getModelValue() {
    return modelValue;
  }
}
