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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.FieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.RelativeDefinitionAssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.RootDefinitionAssemblyProperty;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class DefaultXdmFactory implements IXdmFactory {

  @Override
  public IBoundXdmAssemblyNodeItem newAssemblyNodeItem(AssemblyProperty instance, Object value, int position,
      IBoundXdmAssemblyNodeItem parentNodeItem) {
    return new XdmAssemblyNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IBoundXdmFieldNodeItem newFieldNodeItem(@NotNull FieldProperty instance, Object value, int position,
      IBoundXdmAssemblyNodeItem parentNodeItem) {
    return new XdmFieldNodeItemImpl(instance, value, position, parentNodeItem);
  }

  @Override
  public IBoundXdmFlagNodeItem newFlagNodeItem(FlagProperty instance, Object value,
      IBoundXdmModelNodeItem parentNodeItem) {
    return new XdmFlagNodeItemImpl(instance, value, parentNodeItem);
  }

  @Override
  public IBoundXdmRootAssemblyNodeItem newRootAssemblyNodeItem(RootDefinitionAssemblyProperty instance, Object value,
      URI documentUri) {
    return new XdmRootAssemblyNodeItemImpl(instance, value, documentUri);
  }

  @Override
  public IBoundXdmRootAssemblyNodeItem newRootAssemblyNodeItem(AssemblyClassBinding definition, Object value,
      URI documentUri) {
    return newRootAssemblyNodeItem(new RootDefinitionAssemblyProperty(definition), value, documentUri);
  }

  @Override
  public IBoundXdmRootAssemblyNodeItem newRootAssemblyNodeItem(Object value, BindingContext bindingContext,
      URI documentUri) {
    return newRootAssemblyNodeItem((AssemblyClassBinding) bindingContext.getClassBinding(value.getClass()), value,
        documentUri);
  }

  @Override
  public IBoundXdmAssemblyNodeItem newRelativeAssemblyNodeItem(AssemblyClassBinding definition, Object value,
      URI documentUri) {
    return newAssemblyNodeItem(new RelativeDefinitionAssemblyProperty(definition), value, 1, null);
  }
}
