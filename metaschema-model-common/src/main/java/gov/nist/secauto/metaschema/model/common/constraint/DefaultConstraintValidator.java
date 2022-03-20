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

import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.XPathFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAtomicValuedNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: change the name of this class
public class DefaultConstraintValidator implements IConstraintValidator {
  private static final Logger LOGGER = LogManager.getLogger(DefaultConstraintValidator.class);

  @NotNull
  private final Map<@NotNull INodeItem, ValueStatus> valueMap = new LinkedHashMap<>();
  @NotNull
  private final Map<@NotNull String, Map<@NotNull String, INodeItem>> indexToKeyToItemMap = new LinkedHashMap<>();
  @NotNull
  private final Map<@NotNull String, Map<@NotNull String, List<@NotNull INodeItem>>> indexToKeyRefToItemMap
      = new LinkedHashMap<>();
  @NotNull
  private final DynamicContext metapathContext;
  @NotNull
  private IConstraintValidationHandler handler = new LoggingConstraintValidationHandler();

  public DefaultConstraintValidator(@NotNull DynamicContext metapathContext) {
    Objects.requireNonNull(metapathContext, "metapathContext");
    this.metapathContext = metapathContext;
  }

  @NotNull
  public IConstraintValidationHandler getConstraintValidationHandler() {
    return handler;
  }

  @SuppressWarnings("null")
  public void setConstraintValidationHandler(@NotNull IConstraintValidationHandler handler) {
    this.handler = Objects.requireNonNull(handler, "handler");
  }

  @NotNull
  protected DynamicContext getMetapathContext() {
    return metapathContext;
  }

  @Override
  public void validate(@NotNull IFlagNodeItem item) throws MetapathException {
    IFlagDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validate(@NotNull IFieldNodeItem item) throws MetapathException {
    IFieldDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validate(@NotNull IAssemblyNodeItem item) throws MetapathException {
    IAssemblyDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
    validateHasCardinality(definition.getHasCardinalityConstraints(), item);
    validateIndex(definition.getIndexConstraints(), item);
    validateUnique(definition.getUniqueConstraints(), item);
  }

  protected void validateHasCardinality(@NotNull List<@NotNull ? extends ICardinalityConstraint> constraints,
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) throws MetapathException {

    items.stream().forEachOrdered(item -> {
      validateHasCardinality(constraints, item);
    });
  }

  protected void validateHasCardinality(@NotNull List<@NotNull ? extends ICardinalityConstraint> constraints,
      @NotNull IAssemblyNodeItem item) throws MetapathException {
    for (ICardinalityConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item.evaluateMetapath(metapath, getMetapathContext());
      validateHasCardinality(constraint, item, targets);
    }
  }

  protected void validateHasCardinality(@NotNull ICardinalityConstraint constraint, @NotNull IAssemblyNodeItem node,
      ISequence<? extends INodeItem> targets) {
    int itemCount = targets.size();

    Integer minOccurs = constraint.getMinOccurs();
    if (minOccurs != null && itemCount < minOccurs) {
      getConstraintValidationHandler().handleCardinalityMinimumViolation(constraint, node, targets);
    }

    Integer maxOccurs = constraint.getMaxOccurs();
    if (maxOccurs != null && itemCount > maxOccurs) {
      getConstraintValidationHandler().handleCardinalityMaximumViolation(constraint, node, targets);
    }
  }

  protected void validateIndex(@NotNull List<@NotNull ? extends IIndexConstraint> constraints,
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) throws MetapathException {
    items.stream().forEachOrdered(item -> {
      validateIndex(constraints, item);
    });
  }

  protected void validateIndex(@NotNull List<@NotNull ? extends IIndexConstraint> constraints,
      @NotNull IAssemblyNodeItem item) throws MetapathException {
    for (@NotNull
    IIndexConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      @NotNull
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item
              .evaluateMetapath(metapath, getMetapathContext());
      validateIndex(constraint, item, targets);
    }
  }

  protected void validateIndex(@NotNull IIndexConstraint constraint, @NotNull IAssemblyNodeItem node,
      @NotNull ISequence<? extends INodeItem> targets) throws MetapathException {
    String indexName = constraint.getName();
    if (indexToKeyToItemMap.containsKey(indexName)) {
      String msg = String.format("Duplicate index named '%s' found at path '%s'", indexName,
          node.getMetapath());
      LOGGER.atError().log(msg);
      throw new MetapathException(msg);
    }

    Map<@NotNull String, INodeItem> indexItems = new HashMap<>();
    targets.asStream().map(item -> (INodeItem) item)
        .forEachOrdered(item -> {
          @NotNull
          String key;
          try {
            key = buildKey(constraint.getKeyFields(), item);
          } catch (MetapathException ex) {
            getConstraintValidationHandler().handleKeyMatchError(constraint, node, item, ex);
            throw ex;
          }

          // LOGGER.info("key: {} {}", key, item);
          INodeItem oldItem = indexItems.put(key, item);
          if (oldItem != null) {
            getConstraintValidationHandler().handleIndexDuplicateKeyViolation(constraint, node, oldItem, item);
          }
        });
    indexToKeyToItemMap.put(indexName, indexItems);
  }

  protected void validateUnique(@NotNull List<@NotNull ? extends IUniqueConstraint> constraints,
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) throws MetapathException {

    items.stream().forEachOrdered(item -> {
      validateUnique(constraints, item);
    });
  }

  protected void validateUnique(@NotNull List<@NotNull ? extends IUniqueConstraint> constraints,
      @NotNull IAssemblyNodeItem item) throws MetapathException {
    for (IUniqueConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item
              .evaluateMetapath(metapath, getMetapathContext());
      validateUnique(constraint, item, targets);
    }
  }

  protected void validateUnique(@NotNull IUniqueConstraint constraint,
      @NotNull IAssemblyNodeItem node, @NotNull ISequence<?> targets) throws MetapathException {
    Map<String, INodeItem> keyToItemMap = new HashMap<>();

    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      @NotNull
      String key;
      try {
        key = buildKey(constraint.getKeyFields(), item);
      } catch (MetapathException ex) {
        getConstraintValidationHandler().handleKeyMatchError(constraint, node, item, ex);
        throw ex;
      }

      if (keyToItemMap.containsKey(key)) {
        INodeItem oldItem = keyToItemMap.get(key);
        getConstraintValidationHandler().handleUniqueKeyViolation(constraint, node, oldItem, item);
      } else {
        keyToItemMap.put(key, item);
      }
    });
  }

  protected void validateMatches(List<@NotNull ? extends IMatchesConstraint> constraints, INodeItem item)
      throws MetapathException {

    for (IMatchesConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item.evaluateMetapath(metapath, getMetapathContext());
      validateMatches(constraint, item, targets);
    }
  }

  protected void validateMatches(IMatchesConstraint constraint, INodeItem node,
      ISequence<?> targets) throws MetapathException {
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      @SuppressWarnings("null")
      String value = XPathFunctions.fnDataItem(item).asString();

      Pattern pattern = constraint.getPattern();
      if (pattern != null) {
        // validate pattern
        Predicate<String> predicate = pattern.asMatchPredicate();
        if (!predicate.test(value)) {
          getConstraintValidationHandler().handleMatchPatternViolation(constraint, node, item, value);
        }
      }

      IJavaTypeAdapter<?> adapter = constraint.getDataType();
      try {
        adapter.parse(value);
      } catch (IllegalArgumentException ex) {
        getConstraintValidationHandler().handleMatchDatatypeViolation(constraint, node, item, value, ex);
      }
    });
  }

  protected void validateIndexHasKey(List<@NotNull ? extends IIndexHasKeyConstraint> constraints, INodeItem item)
      throws MetapathException {

    for (IIndexHasKeyConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item.evaluateMetapath(metapath, getMetapathContext());
      validateIndexHasKey(constraint, item, targets);
    }
  }

  protected void validateIndexHasKey(IIndexHasKeyConstraint constraint, INodeItem node,
      ISequence<?> targets) throws MetapathException {
    String indexName = constraint.getIndexName();

    Map<@NotNull String, List<@NotNull INodeItem>> keyRefItems = indexToKeyRefToItemMap.get(indexName);
    if (keyRefItems == null) {
      keyRefItems = new LinkedHashMap<>();
      indexToKeyRefToItemMap.put(indexName, keyRefItems);
    }

    for (IItem target : targets.asList()) {
      INodeItem item = (INodeItem) target;
      String key = buildKey(constraint.getKeyFields(), item);

      // LOGGER.info("key-ref: {} {}", key, item);
      //
      List<@NotNull INodeItem> items = keyRefItems.get(key);
      if (items == null) {
        items = new LinkedList<>();
        keyRefItems.put(key, items);
      }

      items.add(item);
    }
  }

  protected void validateExpect(List<@NotNull ? extends IExpectConstraint> constraints, INodeItem item)
      throws MetapathException {
    for (IExpectConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends INodeItem>) item.evaluateMetapath(metapath, getMetapathContext());
      validateExpect(constraint, item, targets);
    }
  }

  protected void validateExpect(IExpectConstraint constraint, INodeItem node, ISequence<?> targets)
      throws MetapathException {
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      MetapathExpression metapath = constraint.getTest();
      try {
        ISequence<?> result = item.evaluateMetapath(metapath, getMetapathContext());
        if (!XPathFunctions.fnBoolean(result).toBoolean()) {
          getConstraintValidationHandler().handleExpectViolation(constraint, node, item, getMetapathContext());
        }
      } catch (Exception ex) {
        String msg = String.format("Unable to evaluate expect constraint '%s' at path '%s'", metapath.getPath(),
            item.getMetapath());
        LOGGER.atError().withThrowable(ex).log(msg);
        throw new MetapathException(msg, ex);
      }
    });
  }

  protected void validateAllowedValues(List<@NotNull ? extends IAllowedValuesConstraint> constraints, INodeItem item)
      throws MetapathException {
    for (IAllowedValuesConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> targets
          = (ISequence<? extends IAtomicValuedNodeItem>) item.evaluateMetapath(metapath, getMetapathContext());
      validateAllowedValues(constraint, item, targets);
    }
  }

  protected void validateAllowedValues(IAllowedValuesConstraint constraint, INodeItem node,
      ISequence<? extends INodeItem> targets) throws MetapathException {
    targets.asStream().forEachOrdered(item -> {
      String value = XPathFunctions.fnDataItem(item).asString();

      updateValueStatus((IAtomicValuedNodeItem) item, constraint.getAllowedValues().containsKey(value));
    });
  }

  protected void updateValueStatus(@NotNull IAtomicValuedNodeItem item, boolean newStatus) {
    @Nullable
    ValueStatus valueStatus = valueMap.get(item);

    if (valueStatus == null) {
      valueStatus = new ValueStatus(item, newStatus);
      valueMap.put(item, valueStatus);
    } else {
      if (!valueStatus.isValid() && newStatus) {
        valueStatus.updateStatus(newStatus);
      }
    }
  }

  @NotNull
  protected String buildKey(@NotNull List<@NotNull ? extends IKeyField> keyFields, @NotNull INodeItem item)
      throws MetapathException {
    StringBuilder key = new StringBuilder();
    for (IKeyField keyField : keyFields) {
      MetapathExpression keyPath = keyField.getTarget();

      @SuppressWarnings("unchecked")
      List<@NotNull ? extends INodeItem> result
          = (List<@NotNull ? extends INodeItem>) item.evaluateMetapath(keyPath, getMetapathContext()).asList();
      if (result.size() > 1) {
        throw new MetapathException("Key resulted in multiple nodes: " + result);
      }

      String keyValue;
      if (result.size() == 1) {
        @SuppressWarnings("null")
        INodeItem keyItem = result.iterator().next();

        keyValue = XPathFunctions.fnDataItem(keyItem).asString();
        Pattern pattern = keyField.getPattern();
        if (pattern != null) {
          Matcher matcher = pattern.matcher(keyValue);
          if (!matcher.matches()) {
            throw new MetapathException(
                String.format("Key field declares the pattern '%s' which does not match the value '%s' of node '%s'",
                    pattern.pattern(), keyValue, keyItem.getMetapath()));
          }

          if (matcher.groupCount() != 1) {
            throw new MetapathException(String.format(
                "Key field declares a pattern '%s' for which the first group was not a match for value '%s' of node '%s'",
                pattern.pattern(), keyValue, keyItem.getMetapath()));
          }
          keyValue = matcher.group(1);
        }
      } else {
        // empty key
        keyValue = null;
      }

      if (keyValue != null) {
        key.append(keyValue);
      }
      key.append("|||");
    }
    @SuppressWarnings("null")
    @NotNull
    String retval = key.toString();
    return retval;
  }

  @Override
  public void finalizeValidation() throws MetapathException {
    for (Map.Entry<@NotNull INodeItem, ValueStatus> entry : valueMap.entrySet()) {
      ValueStatus status = entry.getValue();
      if (status != null && !status.isValid()) {
        LOGGER.atWarn().log(String.format("Value '%s' did not match one of the required allowed values at path '%s'",
            status.getValue(), status.getItem().getMetapath()));
      }
    }

    for (@NotNull
    Map.Entry<
        @NotNull String,
        @NotNull Map<@NotNull String, List<@NotNull INodeItem>>> entry : indexToKeyRefToItemMap.entrySet()) {
      @SuppressWarnings("null")
      String indexName = entry.getKey();
      @SuppressWarnings("null")
      Map<@NotNull String, List<@NotNull INodeItem>> keyRefToItemMap = entry.getValue();

      Map<@NotNull String, INodeItem> indexItems = indexToKeyToItemMap.get(indexName);

      for (Map.Entry<@NotNull String, List<@NotNull INodeItem>> keyRefEntry : keyRefToItemMap
          .entrySet()) {
        @SuppressWarnings("null")
        String key = keyRefEntry.getKey();
        @SuppressWarnings("null")
        List<INodeItem> items = keyRefEntry.getValue();

        if (!indexItems.containsKey(key)) {
          for (INodeItem item : items) {
            LOGGER.atError().log(String.format("Key reference not found in index '%s' for item at path '%s'", indexName,
                item.getMetapath()));
          }
        }
      }
    }
  }

  private static class ValueStatus {
    private final IAtomicValuedNodeItem item;
    private boolean valid;

    public ValueStatus(IAtomicValuedNodeItem item, boolean initialStatus) {
      this.item = item;
      this.valid = initialStatus;
    }

    public boolean isValid() {
      return valid;
    }

    public Object getValue() {
      return item.getValue();
    }

    public INodeItem getItem() {
      return item;
    }

    private void updateStatus(boolean newStatus) {
      if (!valid && newStatus) {
        valid = newStatus;
      }
    }

  }
}
