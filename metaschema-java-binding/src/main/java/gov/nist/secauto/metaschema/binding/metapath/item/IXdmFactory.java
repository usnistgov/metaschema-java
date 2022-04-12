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

package gov.nist.secauto.metaschema.binding.metapath.item;

import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.binding.model.IBoundFieldDefinition;
import gov.nist.secauto.metaschema.binding.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.binding.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * This factory interface provides methods to create new {@link INodeItem} instances based on
 * Metaschema information. These node items represent a node graph of parsed data that can be
 * queried using a Metapath expression.
 */
public interface IXdmFactory {
  IXdmFactory INSTANCE = new DefaultXdmFactory();

  /**
   * Generate a new document node item.
   * 
   * @param definition
   *          the Metaschema assembly definition that describes the model at the root of the document
   * @param value
   *          the bound object representing the data at the root of the document
   * @param documentUri
   *          the URI that was the source of the document, which is used for resolving relative URIs
   *          within the document
   * @return a new document node item representing the rooted assembly value
   */
  @NotNull
  IDocumentNodeItem newDocumentNodeItem(@NotNull RootAssemblyDefinition definition, @NotNull Object value,
      @NotNull URI documentUri);

  @NotNull
  IAssemblyNodeItem newAssemblyNodeItem(@NotNull IBoundAssemblyInstance instance, @NotNull Object value,
      int position, @NotNull IAssemblyNodeItem parentNodeItem);

  @NotNull
  IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyClassBinding definition, @NotNull Object value,
      URI baseUri);

  @NotNull
  IFieldNodeItem newFieldNodeItem(@NotNull IBoundFieldInstance instance, @NotNull Object value, int position,
      @NotNull IAssemblyNodeItem parentNodeItem);

  @NotNull
  IFieldNodeItem newFieldNodeItem(@NotNull IBoundFieldDefinition definition, @NotNull Object value,
      URI baseUri);

  @NotNull
  IFlagNodeItem newFlagNodeItem(@NotNull IBoundFlagInstance instance, @NotNull Object value,
      @NotNull IModelNodeItem parentNodeItem);

  @NotNull
  INodeItem newNodeItem(@NotNull IClassBinding definition, @NotNull Object boundObject, @NotNull URI baseUri,
      boolean rootNode);
}
