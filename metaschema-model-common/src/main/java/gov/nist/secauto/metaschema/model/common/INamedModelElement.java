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

package gov.nist.secauto.metaschema.model.common;

import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IInstanceSet;

import javax.xml.namespace.QName;

public interface INamedModelElement extends IModelElement {
  /**
   * Get the XML qualified name to use in XML.
   * 
   * @return the XML qualified name
   */
  default QName getXmlQName() {
    QName retval = null;
    String namespace = getXmlNamespace();
    if (namespace != null) {
      retval = new QName(namespace, getEffectiveName());
    } else {
      retval = new QName(getEffectiveName());
    }
    return retval;
  }

  /**
   * Get the name used for the associated property in JSON/YAML.
   * 
   * @return the JSON property name
   */
  default String getJsonName() {
    return getEffectiveName();
  }

  /**
   * Get the name to use based on the provided names. This method will return the use name provided by
   * {@link #getUseName()} if the call is not {@code null}, and fall back to the name provided by
   * {@link #getName()} otherwise. This is the model name to use for the
   * for an instance where the instance is referenced.
   * 
   * @return the use name if available, or the name if not
   * 
   * @see #getUseName()
   * @see #getName()
   */
  default String getEffectiveName() {
    String retval = getUseName();
    if (retval == null) {
      retval = getName();
    }
    return retval;
  }

  /**
   * Retrieve the name of the model element.
   * 
   * @return the name
   */
  String getName();

  /**
   * Retrieve the name to use for the model element, instead of the name.
   * 
   * @return the use name or {@code null} if no use name is defined
   */
  String getUseName();

  /**
   * Retrieve the XML namespace for this instance.
   * 
   * @return the XML namespace or {@code null} if no namespace is defined
   */
  String getXmlNamespace();

  IInstanceSet evaluateMetapathInstances(MetapathExpression target);
}
