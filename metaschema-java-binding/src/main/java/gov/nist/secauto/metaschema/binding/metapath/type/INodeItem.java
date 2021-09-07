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
import gov.nist.secauto.metaschema.binding.metapath.INodeContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.AssemblyDefinitionPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.IPathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.IPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.MetapathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.RootPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.type.IPathItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface INodeItem extends IPathItem, INodeContext {
  public static TerminalNodeItem newRootNodeItem(Object value, BindingContext context) {
    ClassBinding classBinding = context.getClassBinding(value.getClass());
    if (classBinding instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) classBinding;
      return new TerminalNodeItem(value, new RootPathSegment(new AssemblyDefinitionPathSegment(assembly)));
    } else {
      throw new UnsupportedOperationException("not an assembly");
    }
  }

  public static INodeItem newRelativeNodeItem(Object value, BindingContext context) {
    return newRelativeNodeItem(value, context, Collections.emptyList());
  }

  public static INodeItem newRelativeNodeItem(Object value, BindingContext context, List<IPathSegment> precedingPath) {
    ClassBinding classBinding = context.getClassBinding(value.getClass());
    if (!precedingPath.isEmpty()) {
      IPathSegment segment = precedingPath.get(precedingPath.size() - 1);
      if (segment.getDefinition() == classBinding) {
        precedingPath = precedingPath.subList(0, precedingPath.size() - 1);
      }
    }
    if (classBinding instanceof IAssemblyDefinition) {
      return new TerminalNodeItem(value, new AssemblyDefinitionPathSegment((IAssemblyDefinition) classBinding),
          precedingPath);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Retrieve the parent node item if it exists.
   * @return the parent node item, or {@code null} if this node item has no known parent
   */
  INodeItem getParent();
  
  /**
   * Determine if the node is a root node
   * @return {@code true} if the node is a root node, or {@code false} otherwise
   */
  boolean isRootNode();

  Object getValue();

  Stream<? extends INodeItem> getNodeItemStream();

  @Override
  IPathSegment getPathSegment();

  INodeItem newChildNodeItem(FlagProperty instance, Object item);

  Stream<INodeItem> newChildNodeItems(NamedModelProperty instance, Stream<?> items);

  default String getMetapath() {
    return toPath(MetapathFormatter.instance());
  }

  default String toPath(IPathFormatter formatter) {
    return formatter.format(getPath());
  }
}
