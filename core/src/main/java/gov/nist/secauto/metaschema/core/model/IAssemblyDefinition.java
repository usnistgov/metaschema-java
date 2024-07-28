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

import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.IFeatureModelConstrained;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface IAssemblyDefinition
    extends IModelDefinition, IContainerModelAssembly, IAssembly, IFeatureModelConstrained {
  QName MODEL_QNAME = new QName(IModule.XML_NAMESPACE, "model");

  /**
   * Check if the assembly is a top-level root assembly.
   *
   * @return {@code true} if the assembly is a top-level root, or {@code false}
   *         otherwise
   */
  default boolean isRoot() {
    // not a root by default
    return false;
  }

  /**
   * Get the root name if this assembly is a top-level root.
   *
   * @return the root name if this assembly is a top-level root, or {@code null}
   *         otherwise
   */
  @Nullable
  default String getRootName() {
    // not a root by default
    return null;
  }

  /**
   * Get the root index to use for binary data, if this assembly is a top-level
   * root.
   *
   * @return the root index if provided and this assembly is a top-level root, or
   *         {@code null} otherwise
   */
  @Nullable
  default Integer getRootIndex() {
    // not a root by default
    return null;
  }

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
      retval = getContainingModule().toModelQName(rootName);
    }
    return retval;
  }

  /**
   * Get the name used for the associated property in JSON/YAML.
   *
   * @return the root JSON property name if this assembly is a top-level root, or
   *         {@code null} otherwise
   */
  default String getRootJsonName() {
    return getRootName();
  }

  @Override
  default IAssemblyInstance getInlineInstance() {
    // not inline by default
    return null;
  }

  @Override
  default IAssemblyDefinition getOwningDefinition() {
    return this;
  }

  @Override
  default IAssemblyNodeItem getNodeItem() {
    return null;
  }

  @Override
  default boolean hasChildren() {
    return IModelDefinition.super.hasChildren() || IContainerModelAssembly.super.hasChildren();
  }
}
