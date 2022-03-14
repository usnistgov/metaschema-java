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

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.IBoundAssemblyInstance;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFieldInstance;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.property.RelativeAssemblyDefinitionAssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.RelativeFieldDefinitionFieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.RootDefinitionAssemblyProperty;

import java.net.URI;

public class DefaultXdmFactory implements IXdmFactory {

  @Override
  public IBoundXdmAssemblyNodeItem newAssemblyNodeItem(IBoundAssemblyInstance instance, Object value, int position,
      IBoundXdmAssemblyNodeItem parentNodeItem) {
    return new IntermediateXdmAssemblyNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IBoundXdmAssemblyNodeItem newAssemblyNodeItem(IBoundAssemblyInstance instance, Object value, int position,
      URI baseUri) {
    return new OrphanedXdmAssemblyNodeItemImpl(instance, value, position, baseUri);
  }

  @Override
  public IBoundXdmFieldNodeItem newFieldNodeItem(IBoundFieldInstance instance, Object value, int position,
      IBoundXdmAssemblyNodeItem parentNodeItem) {
    return new IntermediateXdmFieldNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IBoundXdmFieldNodeItem newFieldNodeItem(IBoundFieldInstance instance, Object value, int position,
      URI baseUri) {
    return new OrphanedXdmFieldNodeItemImpl(instance, value, position, baseUri);
  }

  @Override
  public IBoundXdmFlagNodeItem newFlagNodeItem(IBoundFlagInstance instance, Object value,
      IBoundXdmModelNodeItem parentNodeItem) {
    return new XdmFlagNodeItemImpl(instance, value, parentNodeItem);
  }

  @Override
  public IBoundXdmDocumentNodeItem newDocumentNodeItem(Object value, IBindingContext bindingContext, URI documentUri) {
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) bindingContext.getClassBinding(value.getClass());
    if (classBinding == null) {
      throw new IllegalStateException("Could not find class binding for class: " + value.getClass());
    }
    return newDocumentNodeItem(classBinding, value, documentUri);
  }

  @Override
  public IBoundXdmDocumentNodeItem newDocumentNodeItem(IAssemblyClassBinding definition, Object value,
      URI documentUri) {
    return new XdmDocumentNodeItemImpl(new RootDefinitionAssemblyProperty(definition), value, documentUri);
  }

  @Override
  public IBoundXdmAssemblyNodeItem newRelativeAssemblyNodeItem(IAssemblyClassBinding definition, Object value,
      URI baseUri) {
    return newAssemblyNodeItem(new RelativeAssemblyDefinitionAssemblyProperty(definition), value, 1, baseUri);
  }

  @Override
  public IBoundXdmFieldNodeItem newRelativeFieldNodeItem(IFieldClassBinding definition, Object value, URI baseUri) {
    return newFieldNodeItem(new RelativeFieldDefinitionFieldProperty(definition), value, 1, baseUri);
  }

  @Override
  public IBoundXdmNodeItem newNodeItem(IClassBinding definition, Object value, URI baseUri, boolean rootNode) {
    IBoundXdmNodeItem retval;
    if (definition instanceof IAssemblyClassBinding) {
      if (rootNode) {
        retval = newDocumentNodeItem((IAssemblyClassBinding) definition, value, baseUri);
      } else {
        retval = newRelativeAssemblyNodeItem((IAssemblyClassBinding) definition, value, baseUri);
      }
    } else if (definition instanceof IFieldClassBinding) {
      retval = newRelativeFieldNodeItem((IFieldClassBinding) definition, value, baseUri);
    } else {
      throw new UnsupportedOperationException("must be a bound assembly or field");
    }
    return retval;
  }
}
