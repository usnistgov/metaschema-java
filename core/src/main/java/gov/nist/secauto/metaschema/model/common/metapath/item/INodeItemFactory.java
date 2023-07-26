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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface INodeItemFactory {

  /**
   * Get the singleton instance of the default node factory.
   *
   * @return the node factory instance
   */
  @NonNull
  static INodeItemFactory instance() {
    return DefaultNodeItemFactory.instance();
  }

  @NonNull
  IDocumentNodeItem newDocumentNodeItem(
      @NonNull IRootAssemblyDefinition definition,
      @NonNull URI documentUri,
      @NonNull Object value);

  @NonNull
  IMetaschemaNodeItem newMetaschemaNodeItem(@NonNull IMetaschema metaschema);

  // @NonNull
  // default INodeItem newNodeItem(
  // @NonNull IDefinition definition,
  // @NonNull Object value,
  // @NonNull URI baseUri) {
  // INodeItem retval;
  // if (definition instanceof IAssemblyDefinition) {
  // if (definition instanceof IRootAssemblyDefinition) {
  // retval = newDocumentNodeItem((IRootAssemblyDefinition) definition, baseUri, value);
  // } else {
  // retval = newAssemblyNodeItem((IAssemblyDefinition) definition, baseUri, value);
  // }
  // } else if (definition instanceof IFieldDefinition) {
  // retval = newFieldNodeItem((IFieldDefinition) definition, baseUri, value);
  // } else {
  // throw new UnsupportedOperationException("must be a bound assembly or field");
  // }
  // return retval;
  // }

  /**
   * Create a new {@link IFlagNodeItem}, with no associated value, based on the provided flag
   * definition.
   *
   * @param definition
   *          the flag definition
   * @param parent
   *          the item for the Metaschema containing the definition
   * @return the new flag node item
   */
  @NonNull
  default IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagDefinition definition,
      @NonNull IMetaschemaNodeItem parent) {
    return new FlagDefinitionNodeItemImpl(definition, parent);
  }

  /**
   * Create a new {@link IFlagNodeItem} based on the provided flag instance.
   *
   * @param instance
   *          the flag instance
   * @param parent
   *          the node item containing the flag
   * @return the new flag node item
   */
  @NonNull
  default IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagInstance instance,
      @NonNull IModelNodeItem<?, ?> parent) {
    return new FlagInstanceNoValueNodeItemImpl(instance, parent);
  }

  /**
   * Create a new {@link IFlagNodeItem} based on the provided flag instance.
   *
   * @param instance
   *          the flag instance
   * @param parent
   *          the node item containing the flag
   * @param value
   *          the value, which may be {@code null}
   * @return the new flag node item
   */
  @NonNull
  default IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagInstance instance,
      @NonNull IModelNodeItem<?, ?> parent,
      @NonNull Object value) {
    return new FlagInstanceNodeItemImpl(instance, parent, value);
  }

  /**
   * Create a new {@link IFieldNodeItem} based on the provided definition, which is expected to be a
   * global definition within the provided Metaschema.
   *
   * @param definition
   *          the global definition
   * @param metaschema
   *          the Metaschema containing the definition
   * @return the new field node item
   */
  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldDefinition definition,
      @NonNull IMetaschemaNodeItem metaschema);

  /**
   * Create a new {@link IFieldNodeItem} that is detached from a Metaschema.
   *
   * @param definition
   *          the global definition
   * @param baseUri
   *          the base URI to use for this node item when evaluating a {@link MetapathExpression}
   * @return the new field node item
   */
  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldDefinition definition,
      @Nullable URI baseUri);

  /**
   * Create a new {@link IFieldNodeItem} that is based on a Metaschema instance.
   * <p>
   * A single instance of this item is expected to represent the possibility in a metaschema of a
   * series of instance values.
   *
   * @param instance
   *          the Metaschema field instance
   * @param parent
   *          the parent node item
   * @return the new field node item
   */
  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyNodeItem parent);

  /**
   * Create a new {@link IFieldNodeItem} that is based on a Metaschema instance with associated data.
   *
   * @param instance
   *          the Metaschema field instance
   * @param parent
   *          the parent node item
   * @param position
   *          the data item's position in the sequence of data items for the instance
   * @param value
   *          the data item's value
   * @return the new field node item
   */
  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @NonNull Object value);

  /**
   * Create a new {@link IAssemblyNodeItem} that is detached from a Metaschema.
   *
   * @param definition
   *          the global definition
   * @return the new assembly node item
   */
  @NonNull
  default IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition) {
    return newAssemblyNodeItem(definition, (URI) null);
  }

  /**
   * Create a new {@link IAssemblyNodeItem} based on the provided definition, which is expected to be
   * a global definition within the provided Metaschema.
   *
   * @param definition
   *          the global definition
   * @param metaschema
   *          the Metaschema containing the definition
   * @return the new assembly node item
   */
  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition,
      @NonNull IMetaschemaNodeItem metaschema);

  /**
   * Create a new {@link IAssemblyNodeItem} that is detached from a Metaschema.
   *
   * @param definition
   *          the global definition
   * @param baseUri
   *          the base URI to use for this node item when evaluating a {@link MetapathExpression}
   * @return the new assembly node item
   */
  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition,
      @Nullable URI baseUri);

  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition classBinding,
      @Nullable URI baseUri,
      @NonNull Object value);

  /**
   * Create a new {@link IAssemblyNodeItem} that is based on a Metaschema instance.
   * <p>
   * A single instance of this item is expected to represent the possibility in a metaschema of a
   * series of instance values.
   *
   * @param instance
   *          the Metaschema assembly instance
   * @param parent
   *          the parent node item
   * @return the new assembly node item
   */
  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyInstance instance,
      @NonNull IAssemblyNodeItem parent);

  /**
   * Create a new {@link IAssemblyNodeItem} that is based on a Metaschema instance with associated
   * data.
   *
   * @param instance
   *          the Metaschema assembly instance
   * @param parent
   *          the parent node item
   * @param position
   *          the data item's position in the sequence of data items for the instance
   * @param value
   *          the data item's value
   * @return the new assembly node item
   */
  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @NonNull Object value);
}
