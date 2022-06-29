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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.AbstractNodeItemVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private final IConstraintValidationHandler handler;

  public DefaultConstraintValidator(
      @NotNull DynamicContext metapathContext,
      @NotNull IConstraintValidationHandler handler) {
    this.metapathContext = metapathContext;
    this.handler = handler;
  }

  @NotNull
  public IConstraintValidationHandler getConstraintValidationHandler() {
    return handler;
  }

  @NotNull
  protected DynamicContext getMetapathContext() {
    return metapathContext;
  }

  public void visit(@NotNull INodeItem item) {
    item.accept(new Visitor(), null);
  }

  @Override
  public void validate(@NotNull IFlagNodeItem item) {
    IFlagDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validate(@NotNull IFieldNodeItem item) {
    IFieldDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validate(@NotNull IAssemblyNodeItem item) {
    IAssemblyDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
    validateHasCardinality(definition.getHasCardinalityConstraints(), item);
    validateIndex(definition.getIndexConstraints(), item);
    validateUnique(definition.getUniqueConstraints(), item);
  }

  protected void validateHasCardinality(@NotNull List<@NotNull ? extends ICardinalityConstraint> constraints,
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) {

    items.stream().forEachOrdered(item -> {
      validateHasCardinality(constraints, item);
    });
  }

  protected void validateHasCardinality(@NotNull List<@NotNull ? extends ICardinalityConstraint> constraints,
      @NotNull IAssemblyNodeItem item) {
    for (ICardinalityConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateHasCardinality(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
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
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) {
    items.stream().forEachOrdered(item -> {
      validateIndex(constraints, item);
    });
  }

  protected void validateIndex(@NotNull List<@NotNull ? extends IIndexConstraint> constraints,
      @NotNull IAssemblyNodeItem item) {
    for (IIndexConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateIndex(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateIndex(@NotNull IIndexConstraint constraint, @NotNull IAssemblyNodeItem node,
      @NotNull ISequence<? extends INodeItem> targets) {
    String indexName = constraint.getName();
    if (indexToKeyToItemMap.containsKey(indexName)) {
      String msg = String.format("Duplicate index named '%s' found at path '%s'", indexName,
          node.getMetapath());
      LOGGER.atError().log(msg);
      throw new MetapathException(msg);
    }

    Map<@NotNull String, INodeItem> indexItems = new HashMap<>();
    targets.asStream()
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
      @NotNull List<@NotNull ? extends IAssemblyNodeItem> items) {

    items.stream()
        .forEachOrdered(item -> {
          validateUnique(constraints, item);
        });
  }

  protected void validateUnique(@NotNull List<@NotNull ? extends IUniqueConstraint> constraints,
      @NotNull IAssemblyNodeItem item) {
    for (IUniqueConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateUnique(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateUnique(@NotNull IUniqueConstraint constraint,
      @NotNull IAssemblyNodeItem node, @NotNull ISequence<? extends INodeItem> targets) {
    Map<@NotNull String, INodeItem> keyToItemMap = new HashMap<>();

    targets.asStream()
        .forEachOrdered(item -> {
          @NotNull
          String key;
          try {
            key = buildKey(constraint.getKeyFields(), item);
          } catch (MetapathException ex) {
            getConstraintValidationHandler().handleKeyMatchError(constraint, node, item, ex);
            throw ex;
          }

          if (keyToItemMap.containsKey(key)) {
            INodeItem oldItem = ObjectUtils.notNull(keyToItemMap.get(key));
            getConstraintValidationHandler().handleUniqueKeyViolation(constraint, node, oldItem, item);
          } else {
            keyToItemMap.put(key, item);
          }
        });
  }

  protected void validateMatches(@NotNull List<@NotNull ? extends IMatchesConstraint> constraints,
      @NotNull IDefinitionNodeItem item) {

    for (IMatchesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateMatches(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateMatches(@NotNull IMatchesConstraint constraint, @NotNull INodeItem node,
      ISequence<? extends INodeItem> targets) {
    targets.asStream()
        .forEachOrdered(item -> {
          String value = FnData.fnDataItem(item).asString();

          Pattern pattern = constraint.getPattern();
          if (pattern != null) {
            // validate pattern
            Predicate<String> predicate = pattern.asMatchPredicate();
            if (!predicate.test(value)) {
              getConstraintValidationHandler().handleMatchPatternViolation(constraint, node, item, value);
            }
          }

          IDataTypeAdapter<?> adapter = constraint.getDataType();
          if (adapter != null) {
            try {
              adapter.parse(value);
            } catch (IllegalArgumentException ex) {
              getConstraintValidationHandler().handleMatchDatatypeViolation(constraint, node, item, value, ex);
            }
          }
        });
  }

  protected void validateIndexHasKey(@NotNull List<@NotNull ? extends IIndexHasKeyConstraint> constraints,
      @NotNull IDefinitionNodeItem item) {

    for (IIndexHasKeyConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      validateIndexHasKey(constraint, targets);
    }
  }

  protected void validateIndexHasKey(@NotNull IIndexHasKeyConstraint constraint,
      @NotNull ISequence<? extends INodeItem> targets) {
    String indexName = constraint.getIndexName();

    Map<@NotNull String, List<@NotNull INodeItem>> keyRefItems = indexToKeyRefToItemMap.get(indexName);
    if (keyRefItems == null) {
      keyRefItems = new LinkedHashMap<>();
      indexToKeyRefToItemMap.put(indexName, keyRefItems);
    }

    for (IItem target : targets.asList()) {
      INodeItem item = (INodeItem) target;
      try {
        String key = buildKey(constraint.getKeyFields(), item);

        // LOGGER.info("key-ref: {} {}", key, item);
        //
        List<@NotNull INodeItem> items = keyRefItems.get(key);
        if (items == null) {
          items = new LinkedList<>();
          keyRefItems.put(key, items);
        }
        items.add(item);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateExpect(@NotNull List<@NotNull ? extends IExpectConstraint> constraints,
      @NotNull IDefinitionNodeItem item) {
    for (IExpectConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      validateExpect(constraint, item, targets);
    }
  }

  protected void validateExpect(@NotNull IExpectConstraint constraint, @NotNull INodeItem node,
      @NotNull ISequence<? extends INodeItem> targets) {
    targets.asStream().map(item -> (@NotNull INodeItem) item).forEachOrdered(item -> {
      MetapathExpression metapath = constraint.getTest();
      try {
        ISequence<?> result = metapath.evaluate(item, getMetapathContext());
        if (!FnBoolean.fnBoolean(result).toBoolean()) {
          getConstraintValidationHandler().handleExpectViolation(constraint, node, item, getMetapathContext());
        }
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    });
  }

  protected void validateAllowedValues(@NotNull List<@NotNull ? extends IAllowedValuesConstraint> constraints,
      @NotNull IDefinitionNodeItem item) {
    for (IAllowedValuesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      validateAllowedValues(constraint, targets);
    }
  }

  protected void validateAllowedValues(@NotNull IAllowedValuesConstraint constraint,
      ISequence<? extends IDefinitionNodeItem> targets) {
    targets.asStream().forEachOrdered(item -> {
      try {
        updateValueStatus(item, constraint);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    });
  }

  private void rethrowConstraintError(@NotNull IConstraint constraint, INodeItem item,
      MetapathException ex) {
    StringBuilder builder = new StringBuilder();
    builder.append("A ")
        .append(constraint.getClass().getName())
        .append(" constraint");

    String id = constraint.getId();
    if (id == null) {
      builder.append(" targeting the metapath '")
          .append(constraint.getTarget().getPath())
          .append("'");
    } else {
      builder.append(" with id '")
          .append(id)
          .append("'");
    }

    builder.append(" resulted in an unexpected error. The error was: ")
        .append(ex.getLocalizedMessage());

    throw new MetapathException(builder.toString(), ex);
  }

  protected void updateValueStatus(@NotNull INodeItem targetItem, @NotNull IAllowedValuesConstraint allowedValue) {
    // constraint.getAllowedValues().containsKey(value)

    @Nullable
    ValueStatus valueStatus = valueMap.get(targetItem);
    if (valueStatus == null) {
      valueStatus = new ValueStatus(targetItem);
      valueMap.put(targetItem, valueStatus);
    }

    valueStatus.registerAllowedValue(allowedValue);
  }

  protected void handleAllowedValues(@NotNull INodeItem targetItem) {
    ValueStatus valueStatus = valueMap.remove(targetItem);
    if (valueStatus != null) {
      valueStatus.validate();
    }
  }

  @NotNull
  protected String buildKey(@NotNull List<@NotNull ? extends IKeyField> keyFields, @NotNull INodeItem item) {
    StringBuilder key = new StringBuilder();
    for (IKeyField keyField : keyFields) {
      MetapathExpression keyPath = keyField.getTarget();

      INodeItem keyItem;
      try {
        keyItem = keyPath.evaluateAs(item, ResultType.NODE, getMetapathContext());
      } catch (InvalidTypeMetapathException ex) {
        throw new MetapathException("Key path did not result in a single node", ex);
      }

      String keyValue = null;
      if (keyItem != null) {
        keyValue = FnData.fnDataItem(keyItem).asString();
        keyValue = applyPattern(keyItem, keyField, keyValue);
      } // empty key

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

  /**
   * Apply the key value pattern, if configured, to generate the final key value.
   * 
   * @param keyItem
   *          the node item used to form the key field
   * @param keyField
   *          the key field configuration from the constraint
   * @param keyValue
   *          the current key value
   * @return the final key value
   */
  protected String applyPattern(@NotNull INodeItem keyItem, @NotNull IKeyField keyField, @NotNull String keyValue) {
    String retval = keyValue;
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
            "The first group was not a match for value '%s' of node '%s' for key field pattern '%s'",
            keyValue, keyItem.getMetapath(), pattern.pattern()));
      }
      retval = matcher.group(1);
    }
    return retval;
  }

  @Override
  public void finalizeValidation() {
    // key references
    for (Map.Entry<@NotNull String, Map<@NotNull String, List<@NotNull INodeItem>>> entry : indexToKeyRefToItemMap
        .entrySet()) {
      @SuppressWarnings("null")
      String indexName = entry.getKey();
      Map<@NotNull String, List<@NotNull INodeItem>> keyRefToItemMap = entry.getValue();

      Map<@NotNull String, INodeItem> indexItems = indexToKeyToItemMap.get(indexName);

      for (Map.Entry<@NotNull String, List<@NotNull INodeItem>> keyRefEntry : keyRefToItemMap
          .entrySet()) {
        @SuppressWarnings("null")
        String key = keyRefEntry.getKey();
        List<@NotNull INodeItem> items = keyRefEntry.getValue();

        if (!indexItems.containsKey(key)) {
          for (INodeItem item : items) {
            LOGGER.atError().log(String.format("Key reference not found in index '%s' for item at path '%s'", indexName,
                item.getMetapath()));
          }
        }
      }
    }
  }

  private class ValueStatus {
    private final List<@NotNull IAllowedValuesConstraint> constraints = new LinkedList<>();
    private final String value;
    private final INodeItem item;
    private boolean allowOthers = true;
    private IAllowedValuesConstraint.Extensible extensible = IAllowedValuesConstraint.Extensible.EXTERNAL;

    public ValueStatus(@NotNull INodeItem item) {
      this.item = item;
      this.value = FnData.fnDataItem(item).asString();
    }

    public void registerAllowedValue(@NotNull IAllowedValuesConstraint allowedValues) {
      this.constraints.add(allowedValues);
      if (!allowedValues.isAllowedOther()) {
        // record the most restrictive value
        allowOthers = false;
      }

      if (allowedValues.getExtensible().ordinal() > extensible.ordinal()) {
        // record the most restrictive value
        extensible = allowedValues.getExtensible();
      }
    }

    public void validate() {
      boolean pass = false;
      List<@NotNull IAllowedValuesConstraint> failedConstraints = new LinkedList<>();

      for (IAllowedValuesConstraint allowedValues : constraints) {
        if (allowedValues.getExtensible().ordinal() < extensible.ordinal()) {
          String msg = String.format(
              "An allowed values constraint with an extensibility scope '%s'"
                  + " exceeds the allowed scope '%s' at path '%s'",
              allowedValues.getExtensible().name(), extensible.name(), item.getMetapath());
          LOGGER.atError().log(msg);
          throw new MetapathException(msg);
        }

        IAllowedValue match = allowedValues.getAllowedValue(value);
        if (match == null) {
          if (IAllowedValuesConstraint.Extensible.NONE.equals(allowedValues.getExtensible())
              && !allowedValues.isAllowedOther()) {
            // failure
            failedConstraints = CollectionUtil.singletonList(allowedValues);
            break;
          }
          failedConstraints.add(allowedValues);
        } else {
          pass = true;
          break;
        }
      }

      if (!pass) {
        getConstraintValidationHandler().handleAllowedValuesViolation(failedConstraints, item);
      }
    }
  }

  private class Visitor
      extends AbstractNodeItemVisitor<Void, Void> {
    @Override
    public Void visitDocument(@NotNull IDocumentNodeItem item, Void context) {
      return super.visitDocument(item, context);
    }

    @Override
    public Void visitFlag(@NotNull IFlagNodeItem item, Void context) {
      validate(item);
      super.visitFlag(item, context);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitField(@NotNull IFieldNodeItem item, Void context) {
      validate(item);
      super.visitField(item, context);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitAssembly(@NotNull IAssemblyNodeItem item, Void context) {
      validate(item);
      super.visitAssembly(item, context);
      return null;
    }

    @Override
    public Void visitMetaschema(@NotNull IMetaschemaNodeItem item, Void context) {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    protected Void defaultResult() {
      // no result value
      return null;
    }
  }
}
