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

package gov.nist.secauto.metaschema.binding.metapath.type;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.metapath.TerminalNodeContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.format.FormatterFactory;
import gov.nist.secauto.metaschema.model.common.metapath.format.IAssemblyPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.format.IFieldPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.format.IFlagPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;

public class NodeItemFactory {
  
  public static IAssemblyNodeItem newNodeItem(IAssemblyPathSegment segment, Object value, IAssemblyNodeItem parent) {
    return new AssemblyNodeItemImpl(value, segment, parent);
  }

  public static IFieldNodeItem newNodeItem(IFieldPathSegment segment, Object value, IAssemblyNodeItem parent) {
    return new FieldNodeItemImpl(value, segment, parent);
  }

  public static IFlagNodeItem newNodeItem(IFlagPathSegment segment, Object value, IModelNodeItem parent) {
    return new FlagNodeItemImpl(value, segment, parent);
  }
  // public static TerminalNodeItem newRootNodeItem(Object value, BindingContext context) {
  // ClassBinding classBinding = context.getClassBinding(value.getClass());
  // if (classBinding instanceof AssemblyClassBinding) {
  // AssemblyClassBinding assembly = (AssemblyClassBinding) classBinding;
  // FormatterFactory formatterFactory = FormatterFactory.instance();
  // return new TerminalNodeItem(value, formatterFactory.newAssemblyPathSegment(assembly));
  // } else {
  // throw new UnsupportedOperationException("not an assembly");
  // }
  // }

  public static INodeContext newTerminalNodeContext(Object value, BindingContext bindingContext) {
    ClassBinding classBinding = bindingContext.getClassBinding(value.getClass());
    if (classBinding instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) classBinding;
      IAssemblyPathSegment segment = FormatterFactory.instance().newRootAssemblyPathSegment(assembly);
      IAssemblyNodeItem nodeItem = NodeItemFactory.newNodeItem(segment, value, null);

      return new TerminalNodeContext(nodeItem);
    } else {
      throw new UnsupportedOperationException("not an assembly");
    }
  }

  // public static INodeItem newRelativeNodeItem(Object value, BindingContext context) {
  // return newRelativeNodeItem(value, context, Collections.emptyList());
  // }
  //
  // public static INodeItem newRelativeNodeItem(Object value, BindingContext context,
  // List<IPathSegment> precedingPath) {
  // ClassBinding classBinding = context.getClassBinding(value.getClass());
  // if (!precedingPath.isEmpty()) {
  // IPathSegment segment = precedingPath.get(precedingPath.size() - 1);
  // if (segment.getDefinition() == classBinding) {
  // precedingPath = precedingPath.subList(0, precedingPath.size() - 1);
  // }
  // }
  // if (classBinding instanceof IAssemblyDefinition) {
  // return new TerminalNodeItem(value,
  // FormatterFactory.instance().newRootAssemblyPathSegment((IAssemblyDefinition) classBinding),
  // precedingPath);
  // } else {
  // throw new UnsupportedOperationException();
  // }
  // }

}
