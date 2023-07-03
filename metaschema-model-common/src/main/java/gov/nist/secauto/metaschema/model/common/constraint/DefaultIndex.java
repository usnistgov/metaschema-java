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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class DefaultIndex implements IIndex {
  @NonNull
  private final List<IKeyField> keyFields;
  @NonNull
  private final Map<List<String>, INodeItem> keyToItemMap = new ConcurrentHashMap<>();

  /**
   * Construct a new index.
   *
   * @param keyFields
   *          the key field components to use to generate keys by default
   */
  protected DefaultIndex(@NonNull List<? extends IKeyField> keyFields) {
    this.keyFields = CollectionUtil.unmodifiableList(new ArrayList<>(keyFields));
  }

  @Override
  public List<IKeyField> getKeyFields() {
    return keyFields;
  }

  @Override
  public INodeItem put(@NonNull INodeItem item, @NonNull List<String> key) {
    INodeItem oldItem = null;
    if (!IIndex.isAllNulls(key)) {
      // only add keys with some information (values)
      oldItem = keyToItemMap.put(key, item);
    }
    return oldItem;
  }

  @Override
  public INodeItem get(List<String> key) {
    return keyToItemMap.get(key);
  }

  @Override
  public List<String> toKey(@NonNull INodeItem item, @NonNull List<? extends IKeyField> keyFields,
      @NonNull DynamicContext dynamicContext) {
    if (getKeyFields().size() != keyFields.size()) {
      throw new IllegalArgumentException("Provided key fields are not the same size as the index requires.");
    }
    return CollectionUtil.unmodifiableList(
        ObjectUtils.notNull(keyFields.stream()
            .map(keyField -> {
              assert keyField != null;
              return buildKeyItem(item, keyField, dynamicContext);
            })
            .collect(Collectors.toCollection(ArrayList::new))));
  }

  /**
   * Evaluates the provided key field component against the item to generate a key value.
   *
   * @param item
   *          the item to generate the key value from
   * @param keyField
   *          the key field component used to generate the key value
   * @param dynamicContext
   *          the Metapath evaluation context
   * @return the key value or {@code null} if the evaluation resulted in no value
   */
  @Nullable
  protected static String buildKeyItem(
      @NonNull INodeItem item,
      @NonNull IKeyField keyField,
      @NonNull DynamicContext dynamicContext) {
    MetapathExpression keyPath = keyField.getTarget();

    INodeItem keyItem;
    try {
      keyItem = keyPath.evaluateAs(item, ResultType.NODE, dynamicContext);
    } catch (InvalidTypeMetapathException ex) {
      throw new MetapathException("Key path did not result in a single node", ex);
    }

    String keyValue = null;
    if (keyItem != null) {
      keyValue = FnData.fnDataItem(keyItem).asString();
      Pattern pattern = keyField.getPattern();
      if (pattern != null) {
        keyValue = applyPattern(keyItem, keyValue, pattern);
      }
    } // empty key
    return keyValue;
  }

  /**
   * Apply the key value pattern, if configured, to generate the final key value.
   *
   * @param keyItem
   *          the node item used to form the key field
   * @param pattern
   *          the key field pattern configuration from the constraint
   * @param keyValue
   *          the current key value
   * @return the final key value
   */
  protected static String applyPattern(@NonNull INodeItem keyItem, @NonNull String keyValue,
      @NonNull Pattern pattern) {
    Matcher matcher = pattern.matcher(keyValue);
    if (!matcher.matches()) {
      throw new MetapathException(
          String.format("Key field declares the pattern '%s' which does not match the value '%s' of node '%s'",
              pattern.pattern(), keyValue, keyItem.getMetapath()));
    }

    if (matcher.groupCount() != 1) {
      throw new MetapathException(String.format(
          "The first group was not a match for value '%s' of node '%s' for key field pattern '%s'",
          keyValue, keyItem.getMetapath(), pattern.pattern()));
    }
    return matcher.group(1);
  }

}
