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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.format.IContentPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.format.IFormatterFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IContentNodeItem extends INodeItem, IValueItem {
  /**
   * Retrieve the parent node item if it exists.
   * 
   * @return the parent node item, or {@code null} if this node item has no known parent
   */
  @Nullable
  INodeItem getParentNodeItem();

  /**
   * Determine if the node is a root node.
   * 
   * @return {@code true} if the node is a root node, or {@code false} otherwise
   */
  default boolean isRootNode() {
    return false;
  }

  /**
   * Get the Metaschema definition associated with this node.
   * 
   * @return the definition
   */
  @NotNull
  INamedDefinition getDefinition();

  /**
   * Retrieve the value associated with the item.
   * 
   * @return the value
   */
  @Override
  Object getValue();

  // TODO: rename to asAtomicItem
  @Override
  IAnyAtomicItem toAtomicItem();

  /**
   * Get the path for this node item as a Metapath.
   * 
   * @return the Metapath
   */
  @NotNull
  default String getMetapath() {
    return toPath(IFormatterFactory.METAPATH_FORMATTER);
  }

  @Override
  IContentPathSegment getPathSegment();

}
