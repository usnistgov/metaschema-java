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
import java.util.List;
import java.util.Map;

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
  default INodeItem newNodeItem(@NonNull IDefinition definition, @NonNull Object value, @NonNull URI baseUri,
      boolean rootNode) {
    INodeItem retval;
    if (definition instanceof IAssemblyDefinition) {
      if (rootNode && definition instanceof IRootAssemblyDefinition) {
        retval = newDocumentNodeItem((IRootAssemblyDefinition) definition, value, baseUri);
      } else {
        retval = newAssemblyNodeItem((IAssemblyDefinition) definition, value, baseUri);
      }
    } else if (definition instanceof IFieldDefinition) {
      retval = newFieldNodeItem((IFieldDefinition) definition, value, baseUri);
    } else {
      throw new UnsupportedOperationException("must be a bound assembly or field");
    }
    return retval;
  }

  @NonNull
  IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagDefinition definition,
      @Nullable URI baseUri);

  @NonNull
  IFlagNodeItem newFlagNodeItem(
      @NonNull IFlagInstance instance,
      @NonNull IModelNodeItem parent,
      @Nullable Object value);

  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldDefinition definition,
      @Nullable Object value,
      @Nullable URI baseUri);

  @NonNull
  IFieldNodeItem newFieldNodeItem(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @NonNull Object value);

  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyDefinition definition,
      @Nullable Object value,
      @Nullable URI baseUri);

  @NonNull
  IAssemblyNodeItem newAssemblyNodeItem(
      @NonNull IAssemblyInstance instance,
      @NonNull IAssemblyNodeItem parent,
      int position,
      @Nullable Object value);

  @NonNull
  IDocumentNodeItem newDocumentNodeItem(
      @NonNull IRootAssemblyDefinition definition,
      @NonNull Object value,
      @NonNull URI documentUri);

  @NonNull
  IMetaschemaNodeItem newMetaschemaNodeItem(@NonNull IMetaschema metaschema);

  /**
   * Given the provided parent node item, generate a mapping of flag name to flag node item for each
   * flag on the parent assembly.
   *
   * @param parent
   *          the parent assembly containing flags
   * @return a mapping of flag name to flag item
   */
  @NonNull
  Map<String, IFlagNodeItem> generateFlags(@NonNull IModelNodeItem parent);

  /**
   * Given the provided parent node item, generate a mapping of model instance name to model node
   * item(s) for each model instance on the parent assembly.
   *
   * @param parent
   *          the parent assembly containing model instances
   * @return a mapping of model instance name to model node item(s)
   */
  @NonNull
  Map<String, List<IModelNodeItem>> generateModelItems(@NonNull IAssemblyNodeItem parent);
}
