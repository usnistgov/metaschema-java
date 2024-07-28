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

package gov.nist.secauto.metaschema.core.metapath.cst;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ICollectionValue;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MapConstructor implements IExpression {
  @NonNull
  private final List<MapConstructor.Entry> entries;

  public MapConstructor(@NonNull List<MapConstructor.Entry> entries) {
    this.entries = entries;
  }

  @Override
  public List<MapConstructor.Entry> getChildren() {
    return entries;
  }

  @Override
  public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    return IMapItem.ofCollection(
        ObjectUtils.notNull(getChildren().stream()
            .map(item -> {
              IAnyAtomicItem key
                  = FnData.fnData(item.getKeyExpression().accept(dynamicContext, focus)).getFirstItem(true);
              ICollectionValue value = item.getValueExpression().accept(dynamicContext, focus).toCollectionValue();

              return IMapItem.entry(key, value);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
        .asSequence();
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitMapConstructor(this, context);
  }

  public static class Entry implements IExpression {
    @NonNull
    private final IExpression keyExpression;
    @NonNull
    private final IExpression valueExpression;

    public Entry(@NonNull IExpression keyExpression, @NonNull IExpression valueExpression) {
      this.keyExpression = keyExpression;
      this.valueExpression = valueExpression;
    }

    @NonNull
    public IExpression getKeyExpression() {
      return keyExpression;
    }

    @NonNull
    public IExpression getValueExpression() {
      return valueExpression;
    }

    @SuppressWarnings("null")
    @Override
    public List<? extends IExpression> getChildren() {
      return List.of(keyExpression, valueExpression);
    }

    @Override
    public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
      throw new UnsupportedOperationException("handled by the map constructor");
    }

    @Override
    public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
      return visitor.visitMapConstructorEntry(this, context);
    }

  }
}
