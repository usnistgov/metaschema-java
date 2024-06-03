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

package gov.nist.secauto.metaschema.core.metapath.item.node;

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAtomicValuedItem;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFlagNodeItem
    extends IDefinitionNodeItem<IFlagDefinition, IFlagInstance>, IAtomicValuedItem {
  @Override
  default NodeItemType getNodeItemType() {
    return NodeItemType.FLAG;
  }

  @Override
  default IFlagNodeItem getNodeItem() {
    return this;
  }

  @Override
  IFlagDefinition getDefinition();

  @Override
  IFlagInstance getInstance();

  @Override
  @Nullable
  default URI getBaseUri() {
    INodeItem parent = getParentNodeItem();
    return parent == null ? null : parent.getBaseUri();
  }

  /**
   * FlagContainer do not have flag items. This call should return an empty
   * collection.
   */
  @SuppressWarnings("null")
  @Override
  default Collection<? extends IFlagNodeItem> getFlags() {
    // a flag does not have flags
    return Collections.emptyList();
  }

  /**
   * FlagContainer do not have flag items. This call should return {@code null}.
   */
  @Override
  default IFlagNodeItem getFlagByName(@NonNull QName name) {
    // a flag does not have flags
    return null;
  }

  /**
   * FlagContainer do not have flag items. This call should return an empty
   * stream.
   */
  @SuppressWarnings("null")
  @Override
  default @NonNull Stream<? extends IFlagNodeItem> flags() {
    // a flag does not have flags
    return Stream.empty();
  }

  /**
   * FlagContainer do not have model items. This call should return an empty
   * collection.
   */
  @SuppressWarnings("null")
  @Override
  default @NonNull Collection<? extends List<? extends IModelNodeItem<?, ?>>> getModelItems() {
    // a flag does not have model items
    return Collections.emptyList();
  }

  /**
   * FlagContainer do not have model items. This call should return an empty list.
   */
  @SuppressWarnings("null")
  @Override
  default List<? extends IModelNodeItem<?, ?>> getModelItemsByName(QName name) {
    // a flag does not have model items
    return Collections.emptyList();
  }

  /**
   * FlagContainer do not have model items. This call should return an empty
   * stream.
   */
  @SuppressWarnings("null")
  @NonNull
  @Override
  default Stream<? extends IModelNodeItem<?, ?>> modelItems() {
    // a flag does not have model items
    return Stream.empty();
  }

  @Override
  default @NonNull String format(@NonNull IPathFormatter formatter) {
    return formatter.formatFlag(this);
  }

  @Override
  default <CONTEXT, RESULT> RESULT accept(@NonNull INodeItemVisitor<CONTEXT, RESULT> visitor, CONTEXT context) {
    return visitor.visitFlag(this, context);
  }
}
