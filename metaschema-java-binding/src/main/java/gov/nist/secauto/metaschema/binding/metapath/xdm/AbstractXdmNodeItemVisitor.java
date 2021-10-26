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

import gov.nist.secauto.metaschema.model.common.metapath.xdm.IXdmFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.xdm.IXdmModelNodeItem;

import java.util.List;

public abstract class AbstractXdmNodeItemVisitor<RESULT, CONTEXT> implements IBoundXdmNodeItemVisitor<RESULT, CONTEXT> {
  protected abstract RESULT defaultResult();

  protected RESULT visitFlags(IBoundXdmModelNodeItem item, CONTEXT context) {
    RESULT result = defaultResult();
    for (IBoundXdmFlagNodeItem flag : item.getFlags().values()) {
      if (!shouldVisitNextChild(flag, result, context)) {
        break;
      }

      RESULT childResult = flag.accept(this, context);
      result = aggregateResult(result, childResult);
    }
    return result;
  }
  protected RESULT visitModelChildren(IBoundXdmAssemblyNodeItem item, CONTEXT context) {
    RESULT result = defaultResult();
    
    for (List<? extends IBoundXdmModelNodeItem> childItems : item.getModelItems().values()) {
      for (IBoundXdmModelNodeItem childItem : childItems) {
        if (!shouldVisitNextChild(childItem, result, context)) {
          break;
        }
  
        RESULT childResult = childItem.accept(this, context);
        result = aggregateResult(result, childResult);
      }
    }
    return result;
  }

  protected boolean shouldVisitNextChild(
      @SuppressWarnings("unused") IXdmFlagNodeItem item,
      @SuppressWarnings("unused") RESULT result, @SuppressWarnings("unused") CONTEXT context) {
    return true;
  }

  protected boolean shouldVisitNextChild(
      @SuppressWarnings("unused") IXdmModelNodeItem item,
      @SuppressWarnings("unused") RESULT result, @SuppressWarnings("unused") CONTEXT context) {
    return true;
  }

  private RESULT aggregateResult(@SuppressWarnings("unused") RESULT result, RESULT childResult) {
    return childResult;
  }

  public RESULT visit(IBoundXdmNodeItem item, CONTEXT context) {
    return item.accept(this, context);
  }

  public RESULT visitDocument(IBoundXdmDocumentNodeItem item, CONTEXT context) {
    return visitAssembly(item.getRootAssemblyNodeItem(), context);
  }

  @Override
  public RESULT visitFlag(IBoundXdmFlagNodeItem item, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitField(IBoundXdmFieldNodeItem item, CONTEXT context) {
    return visitFlags(item, context);
  }

  @Override
  public RESULT visitAssembly(IBoundXdmAssemblyNodeItem item, CONTEXT context) {
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context));
  }
}
