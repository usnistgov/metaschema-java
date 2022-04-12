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
import gov.nist.secauto.metaschema.binding.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import java.net.URI;

/**
 * A concrete implementation of the {@link IXdmFactory}.
 */
public class DefaultXdmFactory implements IXdmFactory {

  @Override
  public IDocumentNodeItem newDocumentNodeItem(RootAssemblyDefinition definition, Object value,
      URI documentUri) {
    return new XdmDocumentNodeItemImpl(definition, value, documentUri);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(IBoundAssemblyInstance instance, Object value, int position,
      IAssemblyNodeItem parentNodeItem) {
    return new IntermediateXdmAssemblyNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IAssemblyNodeItem newAssemblyNodeItem(IAssemblyClassBinding definition, Object value,
      URI baseUri) {
    return new OrphanedXdmAssemblyNodeItemImpl(definition, value, 1, baseUri);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(IBoundFieldInstance instance, Object value, int position,
      IAssemblyNodeItem parentNodeItem) {
    return new IntermediateXdmFieldNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IFieldNodeItem newFieldNodeItem(IBoundFieldDefinition definition, Object value,
      URI baseUri) {
    return new OrphanedXdmFieldNodeItemImpl(definition, value, 1, baseUri);
  }

  @Override
  public IFlagNodeItem newFlagNodeItem(IBoundFlagInstance instance, Object value,
      IModelNodeItem parentNodeItem) {
    return new XdmFlagNodeItemImpl(instance, value, parentNodeItem);
  }

  @Override
  public INodeItem newNodeItem(IClassBinding definition, Object value, URI baseUri, boolean rootNode) {
    INodeItem retval;
    if (definition instanceof IAssemblyClassBinding) {
      if (rootNode) {
        retval = newDocumentNodeItem(new RootAssemblyDefinition((IAssemblyClassBinding) definition), value, baseUri);
      } else {
        retval = newAssemblyNodeItem((IAssemblyClassBinding) definition, value, baseUri);
      }
    } else if (definition instanceof IFieldClassBinding) {
      retval = newFieldNodeItem((IFieldClassBinding) definition, value, baseUri);
    } else {
      throw new UnsupportedOperationException("must be a bound assembly or field");
    }
    return retval;
  }
}
