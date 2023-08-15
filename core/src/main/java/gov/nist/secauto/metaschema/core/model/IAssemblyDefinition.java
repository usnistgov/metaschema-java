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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.model.impl.IFeatureModelConstrained;

import javax.xml.namespace.QName;

public interface IAssemblyDefinition
    extends IFlagContainer, IModelContainer, IAssembly, IFeatureModelConstrained {

  @Override
  default IAssemblyDefinition getContainingDefinition() {
    return this;
  }

  /**
   * Check if the assembly is a top-level root assembly.
   *
   * @return {@code true} if the assembly is a top-level root, or {@code false}
   *         otherwise
   */
  boolean isRoot();

  /**
   * Get the root name if this assembly is a top-level root.
   *
   * @return the root name if this assembly is a top-level root, or {@code null}
   *         otherwise
   */
  String getRootName();

  /**
   * Get the XML qualified name to use in XML as the root element.
   *
   * @return the root XML qualified name if this assembly is a top-level root, or
   *         {@code null} otherwise
   */
  default QName getRootXmlQName() {
    QName retval = null;
    String rootName = getRootName();
    if (rootName != null) {
      retval = new QName(getContainingMetaschema().getXmlNamespace().toASCIIString(), rootName);
    }
    return retval;
  }

  @Override
  IAssemblyInstance getInlineInstance();

  /**
   * Get the name used for the associated property in JSON/YAML.
   *
   * @return the root JSON property name if this assembly is a top-level root, or
   *         {@code null} otherwise
   */
  default String getRootJsonName() {
    return getRootName();
  }
}
