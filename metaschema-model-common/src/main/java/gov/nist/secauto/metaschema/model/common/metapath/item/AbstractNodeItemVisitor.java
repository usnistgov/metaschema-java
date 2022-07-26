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

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractNodeItemVisitor<RESULT, CONTEXT> implements INodeItemVisitor<RESULT, CONTEXT> {
  public final RESULT visit(@NonNull INodeItemVisitable item, CONTEXT context) {
    return item.accept(this, context);
  }

  protected abstract RESULT defaultResult();

  protected RESULT visitFlags(@NonNull INodeItem item, CONTEXT context) {
    RESULT result = defaultResult();
    for (IFlagNodeItem flag : item.getFlags()) {
      assert flag != null;
      if (!shouldVisitNextChild(flag, result, context)) {
        break;
      }

      RESULT childResult = flag.accept(this, context);
      result = aggregateResult(result, childResult, context);
    }
    return result;
  }

  protected RESULT visitModelChildren(@NonNull INodeItem item, CONTEXT context) {
    RESULT result = defaultResult();

    for (List<? extends IModelNodeItem> childItems : item.getModelItems()) {
      for (IModelNodeItem childItem : childItems) {
        assert childItem != null;
        if (!shouldVisitNextChild(childItem, result, context)) {
          break;
        }

        RESULT childResult = childItem.accept(this, context);
        result = aggregateResult(result, childResult, context);
      }
    }
    return result;
  }

  @SuppressWarnings("unused")
  protected boolean shouldVisitNextChild(@NonNull INodeItem item, RESULT result, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return true;
  }

  @SuppressWarnings("unused")
  protected boolean shouldVisitNextChild(@NonNull IRequiredValueModelNodeItem item, RESULT result, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return true;
  }

  @SuppressWarnings("unused")
  protected RESULT aggregateResult(RESULT result, RESULT childResult, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return childResult;
  }

  @Override
  public RESULT visitDocument(IDocumentNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return visitAssembly(item.getRootAssemblyNodeItem(), context);
  }

  @Override
  public RESULT visitFlag(IFlagNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return defaultResult();
  }

  @Override
  public RESULT visitField(IFieldNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return visitFlags(item, context);
  }

  @Override
  public RESULT visitAssembly(IAssemblyNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context), context);
  }

  @Override
  public RESULT visitMetaschema(IMetaschemaNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context), context);
  }
}
