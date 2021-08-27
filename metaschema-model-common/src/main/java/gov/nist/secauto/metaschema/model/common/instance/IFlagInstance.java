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

package gov.nist.secauto.metaschema.model.common.instance;

import gov.nist.secauto.metaschema.model.common.Flag;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.DefaultMetaschemaContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IInstanceSet;

import java.util.Collections;

import javax.xml.namespace.QName;

public interface IFlagInstance extends INamedInstance, Flag {
  @Override
  default QName getXmlQName() {
    return QName.valueOf(getEffectiveName());
  }

  @Override
  IFlagDefinition getDefinition();

  /**
   * Determines if a flag value is required to be provided.
   * 
   * @return {@code true} if a value is required, or {@code false} otherwise
   */
  boolean isRequired();

  // /**
  // * Determines if this flag's value is used as the property name for the JSON object that holds
  // * the remaining data based on this flag's containing definition.
  // *
  // * @return {@code true} if this flag is used as a JSON key, or {@code false} otherwise
  // */
  // boolean isJsonKey();

  // /**
  // * Determines if this flag is used as a JSON "value key". A "value key" is a flag who's value is
  // * used as the property name for the containing objects value.
  // *
  // * @return {@code true} if the flag is used as a JSON "value key", or {@code false} otherwise
  // */
  // boolean isJsonValueKey();
  
  @Override
  default IInstanceSet evaluateMetapathInstances(MetapathExpression metapath) {
    return metapath.evaluateMetaschemaInstance(new DefaultMetaschemaContext(IInstanceSet.newInstanceSet(Collections.singleton(this))));
  }
}
