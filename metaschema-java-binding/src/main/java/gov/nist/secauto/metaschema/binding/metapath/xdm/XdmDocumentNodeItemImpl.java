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

package gov.nist.secauto.metaschema.binding.metapath.xdm;

import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.RootDefinitionAssemblyProperty;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.item.NodeItemType;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class XdmDocumentNodeItemImpl implements IBoundXdmDocumentNodeItem {
  @NotNull
  private final IBoundXdmRootAssemblyNodeItem rootNodeItem;
  @NotNull
  private final URI documentUri;

  public XdmDocumentNodeItemImpl(
      @NotNull RootDefinitionAssemblyProperty instance,
      @NotNull Object rootValue,
      @NotNull URI documentUri) {
    this.rootNodeItem = new XdmRootAssemblyNodeItemImpl(instance, rootValue, this);
    this.documentUri = documentUri;
  }

  @Override
  public IBoundXdmDocumentNodeItem getContextNodeItem() {
    return this;
  }

  @Override
  public URI getDocumentUri() {
    return documentUri;
  }

  @Override
  public IAssemblyClassBinding getClassBinding() {
    return getRootAssemblyNodeItem().getClassBinding();
  }

  @Override
  @NotNull
  public IBoundXdmRootAssemblyNodeItem getRootAssemblyNodeItem() {
    return rootNodeItem;
  }

  @Override
  @NotNull
  public IBoundXdmRootAssemblyNodeItem getRootAssemblyPathSegment() {
    return rootNodeItem;
  }

  @Override
  public Map<@NotNull String, ? extends IBoundXdmFlagNodeItem> getFlags() {
    return Collections.emptyMap();
  }

  @Override
  public Map<@NotNull String, ? extends List<@NotNull ? extends IBoundXdmModelNodeItem>> getModelItems() {
    IBoundXdmAssemblyNodeItem nodeItem = getRootAssemblyNodeItem();
    return Collections.singletonMap(nodeItem.getInstance().getEffectiveName(), Collections.singletonList(nodeItem));
  }

}
