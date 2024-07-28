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

public abstract class AbstractRecursionPreventingNodeItemVisitor<CONTEXT, RESULT>
    extends AbstractNodeItemVisitor<CONTEXT, RESULT> {

  @Override
  public RESULT visitAssembly(IAssemblyNodeItem item, CONTEXT context) {
    // only walk new records to avoid looping
    // check if this item's definition is the same as an ancestor's
    return isDecendant(item, item.getDefinition())
        ? defaultResult()
        : super.visitAssembly(item, context);
  }

  /**
   * Determines if the provided node is a descendant of the assembly definition.
   *
   * @param node
   *          the node item to test
   * @param assemblyDefinition
   *          the assembly definition to determine as an ancestor of the node
   * @return {@code true} if the assembly definition is the node's ancestor, or
   *         {@code false} otherwise
   */
  protected boolean isDecendant(IAssemblyNodeItem node, IAssemblyDefinition assemblyDefinition) {
    return node.ancestor()
        .map(ancestor -> {
          boolean retval = false;
          if (ancestor instanceof IAssemblyNodeItem) {
            IAssemblyDefinition ancestorDef = ((IAssemblyNodeItem) ancestor).getDefinition();
            retval = ancestorDef.equals(assemblyDefinition);
          }
          return retval;
        }).anyMatch(value -> value);
  }
}
