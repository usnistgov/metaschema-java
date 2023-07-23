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
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface INodeItemFactory {

  @NonNull
  static IDocumentNodeItem newDocumentNodeItem(
      @NonNull IRootAssemblyDefinition definition,
      @NonNull URI documentUri,
      @NonNull Object value) {
    return new DocumentNodeItemImpl(definition, value, documentUri, DataNodeItemFactory.instance());
  }

  @NonNull
  static IMetaschemaNodeItem newMetaschemaNodeItem(@NonNull IMetaschema metaschema) {
    return new MetaschemaNodeItemImpl(metaschema, MetaschemaNodeItemFactory.instance());
  }

  @NonNull
  default INodeItem newNodeItem(
      @NonNull IDefinition definition,
      @NonNull Object value,
      @NonNull URI baseUri) {
    INodeItem retval;
    if (definition instanceof IAssemblyDefinition) {
      if (definition instanceof IRootAssemblyDefinition) {
        retval = newDocumentNodeItem((IRootAssemblyDefinition) definition, baseUri, value);
      } else {
        retval = newAssemblyNodeItem((IAssemblyDefinition) definition, baseUri, value);
      }
    } else if (definition instanceof IFieldDefinition) {
      retval = newFieldNodeItem((IFieldDefinition) definition, baseUri, value);
    } else {
      throw new UnsupportedOperationException("must be a bound assembly or field");
    }
    return retval;
  }

  /**
   * Create a new {@link IFlagNodeItem}, with no associated value, based on the provided flag
   * definition.
   *
   * @param definition
   *          the flag definition
   * @param baseUri
   *          the base URI of the definition
   * @return the new flag node item
   */
  @NonNull
  IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagDefinition definition,
      @Nullable URI baseUri);

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
  IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagInstance instance,
      @NonNull IModelNodeItem parent,
      @Nullable Object value);

  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldDefinition definition,
      @Nullable URI baseUri,
      @Nullable Object value);

  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @NonNull Object value);

  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition,
      @Nullable URI baseUri,
      @Nullable Object value);

  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @Nullable Object value);
}
