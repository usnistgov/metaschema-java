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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

class DocumentNodeItemImpl
    implements IDocumentNodeItem, IFeatureModelContainerItem {
  @NonNull
  private final IRootAssemblyNodeItem root;
  @NonNull
  private final URI documentUri;

  @NonNull
  private final Lazy<ModelContainer> model;

  public DocumentNodeItemImpl(
      @NonNull IAssemblyDefinition root,
      @NonNull Object rootValue,
      @NonNull URI documentUri,
      @NonNull INodeItemGenerator generator) {
    this.root = new RootAssemblyValuedNodeItemImpl(root, this, rootValue, generator);
    this.documentUri = documentUri;
    this.model = ObjectUtils.notNull(Lazy.lazy(generator.newDataModelSupplier(this.root)));
  }

  @NonNull
  public IRootAssemblyNodeItem getRootAssemblyNodeItem() {
    return root;
  }

  @Override
  public URI getNamespace() {
    return ObjectUtils.notNull(URI.create(getRootAssemblyNodeItem().getName().getNamespaceURI()));
  }

  @Override
  @NonNull
  public URI getDocumentUri() {
    return documentUri;
  }

  @SuppressWarnings("null")
  @Override
  public ModelContainer getModel() {
    return model.get();
  }

  @Override
  public Object getValue() {
    return getRootAssemblyNodeItem().getValue();
  }

  @Override
  public IResourceLocation getLocation() {
    return getRootAssemblyNodeItem().getLocation();
  }
}
