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

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathSegment;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.net.URI;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface INodeItem extends IItem, INodeContext, IPathSegment, INodeItemVisitable {

  /**
   * Gets the value of the provided node item.
   * <p>
   * If the provided node item is a document, this method get the first child node item's value, since
   * a document doesn't have a value.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param item
   *          the node item to get the value of
   * @return a bound object
   * @throws NullPointerException
   *           if the node item has no associated value
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <CLASS> CLASS toValue(@NonNull INodeItem item) {
    INodeItem valuedItem;
    if (item instanceof IDocumentNodeItem) {
      // get first child item, since the document has no value
      valuedItem = item.modelItems().findFirst().get();
    } else {
      valuedItem = item;
    }
    return ObjectUtils.requireNonNull((CLASS) valuedItem.getValue());
  }

  /**
   * Retrieve the parent node item if it exists.
   *
   * @return the parent node item, or {@code null} if this node item has no known parent
   */
  INodeItem getParentNodeItem();

  /**
   * Retrieve the parent content node item if it exists. A content node is a non-document node.
   *
   * @return the parent content node item, or {@code null} if this node item has no known parent
   *         content node item
   */
  IModelNodeItem<?, ?> getParentContentNodeItem();

  /**
   * Get the type of node item this is.
   *
   * @return the node item's type
   */
  @NonNull
  NodeItemType getNodeItemType();

  /**
   * Retrieve the base URI of this node.
   * <p>
   * The base URI of a node will be in order of preference:
   * <ol>
   * <li>the base URI defined on the node</li>
   * <li>the base URI defined on the nearest ancestor node</li>
   * <li>the base URI defined on the document node</li>
   * <li>{@code null} if the document node is unknown</li>
   * </ol>
   *
   * @return the base URI or {@code null} if it is unknown
   */
  URI getBaseUri();

  /**
   * Get the path for this node item as a Metapath.
   *
   * @return the Metapath
   */
  @NonNull
  default String getMetapath() {
    return toPath(IPathFormatter.METAPATH_PATH_FORMATER);
  }

  @Override
  default Stream<? extends INodeItem> getPathStream() {
    INodeItem parent = getParentNodeItem();
    return ObjectUtils.notNull(
        parent == null ? Stream.of(this) : Stream.concat(getParentNodeItem().getPathStream(), Stream.of(this)));
  }

  @NonNull
  default Stream<? extends INodeItem> ancestor() {
    return ancestorsOf(this);
  }

  @NonNull
  default Stream<? extends INodeItem> ancestorOrSelf() {
    return ObjectUtils.notNull(Stream.concat(Stream.of(this), ancestor()));
  }

  @NonNull
  static Stream<? extends INodeItem> ancestorsOf(@NonNull INodeItem nodeItem) {
    INodeItem parent = nodeItem.getParentNodeItem();
    return ObjectUtils.notNull(parent == null ? Stream.empty() : Stream.concat(Stream.of(parent), ancestorsOf(parent)));
  }

  @NonNull
  default Stream<? extends INodeItem> descendant() {
    return decendantsOf(this);
  }

  @NonNull
  static Stream<? extends INodeItem> decendantsOf(@NonNull INodeItem nodeItem) {
    Stream<? extends INodeItem> children = nodeItem.modelItems();

    return ObjectUtils.notNull(children.flatMap(child -> {
      assert child != null;
      return Stream.concat(Stream.of(child), decendantsOf(child));
    }));
  }

  @NonNull
  default Stream<? extends INodeItem> descendantOrSelf() {
    return ObjectUtils.notNull(Stream.concat(Stream.of(this), descendant()));
  }

}
