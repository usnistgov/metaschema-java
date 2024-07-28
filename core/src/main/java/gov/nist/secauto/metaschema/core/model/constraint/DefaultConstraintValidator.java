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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.node.AbstractNodeItemVisitor;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModuleNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Used to perform constraint validation over one or more node items.
 * <p>
 * This class is not thread safe.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class DefaultConstraintValidator
    implements IConstraintValidator, IMutableConfiguration<ValidationFeature<?>> { // NOPMD - intentional
  private static final Logger LOGGER = LogManager.getLogger(DefaultConstraintValidator.class);

  @NonNull
  private final Map<INodeItem, ValueStatus> valueMap = new LinkedHashMap<>(); // NOPMD - intentional
  @NonNull
  private final Map<String, IIndex> indexNameToIndexMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<String, List<KeyRef>> indexNameToKeyRefMap = new ConcurrentHashMap<>();
  @NonNull
  private final IConstraintValidationHandler handler;
  @NonNull
  private final IMutableConfiguration<ValidationFeature<?>> configuration;

  /**
   * Construct a new constraint validator instance.
   *
   * @param handler
   *          the validation handler to use for handling constraint violations
   */
  public DefaultConstraintValidator(
      @NonNull IConstraintValidationHandler handler) {
    this.handler = handler;
    this.configuration = new DefaultConfiguration<>();
  }

  /**
   * Get the current configuration of the serializer/deserializer.
   *
   * @return the configuration
   */
  @NonNull
  protected IMutableConfiguration<ValidationFeature<?>> getConfiguration() {
    return configuration;
  }

  @Override
  public DefaultConstraintValidator enableFeature(ValidationFeature<?> feature) {
    return set(feature, true);
  }

  @Override
  public DefaultConstraintValidator disableFeature(ValidationFeature<?> feature) {
    return set(feature, false);
  }

  @Override
  public DefaultConstraintValidator applyConfiguration(
      @NonNull IConfiguration<ValidationFeature<?>> other) {
    getConfiguration().applyConfiguration(other);
    return this;
  }

  @Override
  public DefaultConstraintValidator set(ValidationFeature<?> feature, Object value) {
    getConfiguration().set(feature, value);
    return this;
  }

  @Override
  public boolean isFeatureEnabled(ValidationFeature<?> feature) {
    return getConfiguration().isFeatureEnabled(feature);
  }

  @Override
  public Map<ValidationFeature<?>, Object> getFeatureValues() {
    return getConfiguration().getFeatureValues();
  }

  /**
   * Get the validation handler to use for handling constraint violations.
   *
   * @return the handler
   */
  @NonNull
  protected IConstraintValidationHandler getConstraintValidationHandler() {
    return handler;
  }

  @Override
  public void validate(
      @NonNull INodeItem item,
      @NonNull DynamicContext dynamicContext) {
    item.accept(new Visitor(), dynamicContext);
  }

  /**
   * Validate the provided flag item against any associated constraints.
   *
   * @param item
   *          the flag item to validate
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a
   *           constraint
   */
  protected void validateFlag(
      @NonNull IFlagNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    IFlagDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item, dynamicContext);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item, dynamicContext);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item, dynamicContext);
    validateMatches(definition.getMatchesConstraints(), item, dynamicContext);
  }

  /**
   * Validate the provided field item against any associated constraints.
   *
   * @param item
   *          the field item to validate
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a
   *           constraint
   */
  protected void validateField(
      @NonNull IFieldNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    IFieldDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item, dynamicContext);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item, dynamicContext);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item, dynamicContext);
    validateMatches(definition.getMatchesConstraints(), item, dynamicContext);
  }

  /**
   * Validate the provided assembly item against any associated constraints.
   *
   * @param item
   *          the assembly item to validate
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a
   *           constraint
   */
  protected void validateAssembly(
      @NonNull IAssemblyNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    IAssemblyDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item, dynamicContext);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item, dynamicContext);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item, dynamicContext);
    validateMatches(definition.getMatchesConstraints(), item, dynamicContext);
    validateHasCardinality(definition.getHasCardinalityConstraints(), item, dynamicContext);
    validateIndex(definition.getIndexConstraints(), item, dynamicContext);
    validateUnique(definition.getUniqueConstraints(), item, dynamicContext);
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateHasCardinality( // NOPMD false positive
      @NonNull List<? extends ICardinalityConstraint> constraints,
      @NonNull IAssemblyNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    for (ICardinalityConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      try {
        validateHasCardinality(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   */
  private void validateHasCardinality(
      @NonNull ICardinalityConstraint constraint,
      @NonNull IAssemblyNodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    int itemCount = targets.size();

    IConstraintValidationHandler handler = getConstraintValidationHandler();

    boolean violation = false;
    Integer minOccurs = constraint.getMinOccurs();
    if (minOccurs != null && itemCount < minOccurs) {
      handler.handleCardinalityMinimumViolation(constraint, node, targets);
      violation = true;
    }

    Integer maxOccurs = constraint.getMaxOccurs();
    if (maxOccurs != null && itemCount > maxOccurs) {
      handler.handleCardinalityMaximumViolation(constraint, node, targets);
      violation = true;
    }

    if (!violation) {
      handlePass(constraint, node, node);
    }
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateIndex(
      @NonNull List<? extends IIndexConstraint> constraints,
      @NonNull IAssemblyNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    for (IIndexConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      try {
        validateIndex(constraint, item, targets, dynamicContext);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateIndex(
      @NonNull IIndexConstraint constraint,
      @NonNull IAssemblyNodeItem node,
      @NonNull ISequence<? extends INodeItem> targets,
      @NonNull DynamicContext dynamicContext) {
    String indexName = constraint.getName();

    IConstraintValidationHandler handler = getConstraintValidationHandler();
    if (indexNameToIndexMap.containsKey(indexName)) {
      handler.handleIndexDuplicateViolation(constraint, node);
    } else {
      IIndex index = IIndex.newInstance(constraint.getKeyFields());
      targets.stream()
          .forEachOrdered(item -> {
            assert item != null;
            if (item.hasValue()) {
              try {
                INodeItem oldItem = index.put(item, dynamicContext);
                if (oldItem == null) {
                  handlePass(constraint, node, item);
                } else {
                  handler.handleIndexDuplicateKeyViolation(constraint, node, oldItem, item);
                }
              } catch (MetapathException ex) {
                handler.handleKeyMatchError(constraint, node, item, ex);
              }
            }
          });
      indexNameToIndexMap.put(indexName, index);
    }
  }

  private void handlePass(
      @NonNull IConstraint constraint,
      @NonNull INodeItem node,
      @NonNull INodeItem item) {
    if (isFeatureEnabled(ValidationFeature.VALIDATE_GENERATE_PASS_FINDINGS)) {
      getConstraintValidationHandler().handlePass(constraint, node, item);
    }
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateUnique(
      @NonNull List<? extends IUniqueConstraint> constraints,
      @NonNull IAssemblyNodeItem item,
      @NonNull DynamicContext dynamicContext) {
    for (IUniqueConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      try {
        validateUnique(constraint, item, targets, dynamicContext);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateUnique(
      @NonNull IUniqueConstraint constraint,
      @NonNull IAssemblyNodeItem node,
      @NonNull ISequence<? extends INodeItem> targets,
      @NonNull DynamicContext dynamicContext) {

    IConstraintValidationHandler handler = getConstraintValidationHandler();
    IIndex index = IIndex.newInstance(constraint.getKeyFields());
    targets.stream()
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            try {
              INodeItem oldItem = index.put(item, dynamicContext);
              if (oldItem == null) {
                handlePass(constraint, node, item);
              } else {
                handler.handleUniqueKeyViolation(constraint, node, oldItem, item);
              }
            } catch (MetapathException ex) {
              handler.handleKeyMatchError(constraint, node, item, ex);
              throw ex;
            }
          }
        });
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateMatches( // NOPMD false positive
      @NonNull List<? extends IMatchesConstraint> constraints,
      @NonNull IDefinitionNodeItem<?, ?> item,
      @NonNull DynamicContext dynamicContext) {

    for (IMatchesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      try {
        validateMatches(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   */
  private void validateMatches(
      @NonNull IMatchesConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    IConstraintValidationHandler handler = getConstraintValidationHandler();
    targets.stream()
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            String value = FnData.fnDataItem(item).asString();

            boolean violation = false;
            Pattern pattern = constraint.getPattern();
            if (pattern != null && !pattern.asMatchPredicate().test(value)) {
              // failed pattern match
              handler.handleMatchPatternViolation(constraint, node, item, value);
              violation = true;
            }

            IDataTypeAdapter<?> adapter = constraint.getDataType();
            if (adapter != null) {
              try {
                adapter.parse(value);
              } catch (IllegalArgumentException ex) {
                handler.handleMatchDatatypeViolation(constraint, node, item, value, ex);
                violation = true;
              }
            }

            if (!violation) {
              handlePass(constraint, node, item);
            }
          }
        });
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateIndexHasKey( // NOPMD false positive
      @NonNull List<? extends IIndexHasKeyConstraint> constraints,
      @NonNull IDefinitionNodeItem<?, ?> item,
      @NonNull DynamicContext dynamicContext) {

    for (IIndexHasKeyConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      validateIndexHasKey(constraint, item, targets);
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   */
  private void validateIndexHasKey(
      @NonNull IIndexHasKeyConstraint constraint,
      @NonNull IDefinitionNodeItem<?, ?> node,
      @NonNull ISequence<? extends INodeItem> targets) {
    String indexName = constraint.getIndexName();

    List<KeyRef> keyRefItems = indexNameToKeyRefMap.get(indexName);
    if (keyRefItems == null) {
      keyRefItems = new LinkedList<>();
      indexNameToKeyRefMap.put(indexName, keyRefItems);
    }

    KeyRef keyRef = new KeyRef(constraint, node, new ArrayList<>(targets.getValue()));
    keyRefItems.add(keyRef);
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateExpect(
      @NonNull List<? extends IExpectConstraint> constraints,
      @NonNull IDefinitionNodeItem<?, ?> item,
      @NonNull DynamicContext dynamicContext) {
    for (IExpectConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      validateExpect(constraint, item, targets, dynamicContext);
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateExpect(
      @NonNull IExpectConstraint constraint,
      @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets,
      @NonNull DynamicContext dynamicContext) {
    MetapathExpression metapath = MetapathExpression.compile(
        constraint.getTest(),
        dynamicContext.getStaticContext());

    IConstraintValidationHandler handler = getConstraintValidationHandler();
    targets.stream()
        .map(item -> (INodeItem) item)
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            try {
              ISequence<?> result = metapath.evaluate(item, dynamicContext);
              if (!FnBoolean.fnBoolean(result).toBoolean()) {
                handler.handleExpectViolation(constraint, node, item, dynamicContext);
              } else {
                handlePass(constraint, node, item);
              }
            } catch (MetapathException ex) {
              rethrowConstraintError(constraint, item, ex);
            }
          }
        });
  }

  /**
   * Evaluates the provided collection of {@code constraints} in the context of
   * the {@code item}.
   *
   * @param constraints
   *          the constraints to execute
   * @param item
   *          the focus of Metapath evaluation
   * @param dynamicContext
   *          the Metapath dynamic execution context to use for Metapath
   *          evaluation
   */
  private void validateAllowedValues(
      @NonNull List<? extends IAllowedValuesConstraint> constraints,
      @NonNull IDefinitionNodeItem<?, ?> item,
      @NonNull DynamicContext dynamicContext) {
    for (IAllowedValuesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem<?, ?>> targets = constraint.matchTargets(item, dynamicContext);
      validateAllowedValues(constraint, item, targets);
    }
  }

  /**
   * Evaluates the provided {@code constraint} against each of the
   * {@code targets}.
   *
   * @param constraint
   *          the constraint to execute
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   * @param targets
   *          the focus of Metapath evaluation for evaluating any constraint
   *          Metapath clauses
   */
  private void validateAllowedValues(
      @NonNull IAllowedValuesConstraint constraint,
      @NonNull IDefinitionNodeItem<?, ?> node,
      @NonNull ISequence<? extends IDefinitionNodeItem<?, ?>> targets) {
    targets.stream().forEachOrdered(item -> {
      assert item != null;
      if (item.hasValue()) {
        try {
          updateValueStatus(item, constraint, node);
        } catch (MetapathException ex) {
          rethrowConstraintError(constraint, item, ex);
        }
      }
    });
  }

  private static void rethrowConstraintError(
      @NonNull IConstraint constraint,
      @NonNull INodeItem item,
      @NonNull MetapathException ex) {
    StringBuilder builder = new StringBuilder(128);
    builder.append("A ")
        .append(constraint.getClass().getName())
        .append(" constraint");

    String id = constraint.getId();
    if (id == null) {
      builder.append(" targeting the metapath '")
          .append(constraint.getTarget())
          .append('\'');
    } else {
      builder.append(" with id '")
          .append(id)
          .append('\'');
    }

    builder.append(", matching the item at path '")
        .append(item.getMetapath())
        .append("', resulted in an unexpected error. The error was: ")
        .append(ex.getLocalizedMessage());

    throw new MetapathException(builder.toString(), ex);
  }

  /**
   * Add a new allowed value to the value status tracker.
   *
   * @param targetItem
   *          the item whose value is targeted by the constraint
   * @param allowedValues
   *          the allowed values constraint
   * @param node
   *          the original focus of Metapath evaluation for identifying the
   *          targets
   */
  protected void updateValueStatus(
      @NonNull INodeItem targetItem,
      @NonNull IAllowedValuesConstraint allowedValues,
      @NonNull IDefinitionNodeItem<?, ?> node) {
    // constraint.getAllowedValues().containsKey(value)

    @Nullable ValueStatus valueStatus = valueMap.get(targetItem);
    if (valueStatus == null) {
      valueStatus = new ValueStatus(targetItem);
      valueMap.put(targetItem, valueStatus);
    }

    valueStatus.registerAllowedValue(allowedValues, node);
  }

  /**
   * Evaluate the value associated with the {@code targetItem} and update the
   * status tracker.
   *
   * @param targetItem
   *          the item whose value will be validated
   */
  protected void handleAllowedValues(@NonNull INodeItem targetItem) {
    ValueStatus valueStatus = valueMap.remove(targetItem);
    if (valueStatus != null) {
      valueStatus.validate();
    }
  }

  @Override
  public void finalizeValidation(DynamicContext dynamicContext) {
    // key references
    for (Map.Entry<String, List<KeyRef>> entry : indexNameToKeyRefMap.entrySet()) {
      String indexName = entry.getKey();
      IIndex index = indexNameToIndexMap.get(indexName);

      List<KeyRef> keyRefs = entry.getValue();

      IConstraintValidationHandler handler = getConstraintValidationHandler();
      for (KeyRef keyRef : keyRefs) {
        IIndexHasKeyConstraint constraint = keyRef.getConstraint();

        INodeItem node = keyRef.getNode();
        List<INodeItem> targets = keyRef.getTargets();
        for (INodeItem item : targets) {
          assert item != null;

          try {
            List<String> key = IIndex.toKey(item, constraint.getKeyFields(), dynamicContext);

            if (index == null) {
              handler.handleGenericValidationViolation(constraint, node, item,
                  String.format("Key reference to undefined index with name '%s'", indexName));
            } else {
              INodeItem referencedItem = index.get(key);

              if (referencedItem == null) {
                handler.handleIndexMiss(constraint, node, item, key);
              } else {
                handlePass(constraint, node, item);
              }
            }
          } catch (MetapathException ex) {
            handler.handleKeyMatchError(constraint, node, item, ex);
          }
        }
      }
    }
  }

  private class ValueStatus {
    @NonNull
    private final List<Pair<IAllowedValuesConstraint, IDefinitionNodeItem<?, ?>>> constraints = new LinkedList<>();
    @NonNull
    private final String value;
    @NonNull
    private final INodeItem item;
    private boolean allowOthers = true;
    @NonNull
    private IAllowedValuesConstraint.Extensible extensible = IAllowedValuesConstraint.Extensible.EXTERNAL;

    public ValueStatus(@NonNull INodeItem item) {
      this.item = item;
      this.value = FnData.fnDataItem(item).asString();
    }

    public void registerAllowedValue(
        @NonNull IAllowedValuesConstraint allowedValues,
        @NonNull IDefinitionNodeItem<?, ?> node) {
      this.constraints.add(Pair.of(allowedValues, node));
      if (!allowedValues.isAllowedOther()) {
        // record the most restrictive value
        allowOthers = false;
      }

      IAllowedValuesConstraint.Extensible newExtensible = allowedValues.getExtensible();
      if (newExtensible.ordinal() > extensible.ordinal()) {
        // record the most restrictive value
        extensible = allowedValues.getExtensible();
      } else if (IAllowedValuesConstraint.Extensible.NONE.equals(newExtensible)
          && IAllowedValuesConstraint.Extensible.NONE.equals(extensible)) {
        // this is an error, where there are two none constraints that conflict
        throw new MetapathException(
            String.format("Multiple constraints have extensibility scope=none at path '%s'", item.getMetapath()));
      } else if (allowedValues.getExtensible().ordinal() < extensible.ordinal()) {
        String msg = String.format(
            "An allowed values constraint with an extensibility scope '%s'"
                + " exceeds the allowed scope '%s' at path '%s'",
            allowedValues.getExtensible().name(), extensible.name(), item.getMetapath());
        LOGGER.atError().log(msg);
        throw new MetapathException(msg);
      }
    }

    public void validate() {
      if (!constraints.isEmpty()) {
        boolean match = false;
        List<IAllowedValuesConstraint> failedConstraints = new LinkedList<>();
        IConstraintValidationHandler handler = getConstraintValidationHandler();
        for (Pair<IAllowedValuesConstraint, IDefinitionNodeItem<?, ?>> pair : constraints) {
          IAllowedValuesConstraint allowedValues = pair.getLeft();
          IDefinitionNodeItem<?, ?> node = pair.getRight();
          IAllowedValue matchingValue = allowedValues.getAllowedValue(value);
          if (matchingValue != null) {
            match = true;
            handlePass(allowedValues, node, item);
          } else if (IAllowedValuesConstraint.Extensible.NONE.equals(allowedValues.getExtensible())) {
            // hard failure, since no other values can satisfy this constraint
            failedConstraints = CollectionUtil.singletonList(allowedValues);
            match = false;
            break;
          } else {
            failedConstraints.add(allowedValues);
          } // this constraint passes, but we need to make sure other constraints do as well
        }

        // it's not a failure if allow others is true
        if (!match && !allowOthers) {
          handler.handleAllowedValuesViolation(failedConstraints, item);
        }
      }
    }
  }

  class Visitor
      extends AbstractNodeItemVisitor<DynamicContext, Void> {

    @NonNull
    private DynamicContext handleLetStatements(
        @NonNull INodeItem focus,
        @NonNull Map<QName, ILet> letExpressions,
        @NonNull DynamicContext dynamicContext) {

      DynamicContext retval;
      Collection<ILet> lets = letExpressions.values();
      if (lets.isEmpty()) {
        retval = dynamicContext;
      } else {
        final DynamicContext subContext = dynamicContext.subContext();

        for (ILet let : lets) {
          QName name = let.getName();
          ISequence<?> result = let.getValueExpression().evaluate(focus, subContext);

          // ensure the sequence is list backed
          result.getValue();

          subContext.bindVariableValue(name, result);
        }
        retval = subContext;
      }
      return retval;
    }

    @Override
    public Void visitFlag(@NonNull IFlagNodeItem item, DynamicContext context) {
      assert context != null;

      IFlagDefinition definition = item.getDefinition();
      DynamicContext effectiveContext = handleLetStatements(item, definition.getLetExpressions(), context);

      validateFlag(item, effectiveContext);
      super.visitFlag(item, effectiveContext);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitField(@NonNull IFieldNodeItem item, DynamicContext context) {
      assert context != null;

      IFieldDefinition definition = item.getDefinition();
      DynamicContext effectiveContext = handleLetStatements(item, definition.getLetExpressions(), context);

      validateField(item, effectiveContext);
      super.visitField(item, effectiveContext);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitAssembly(@NonNull IAssemblyNodeItem item, DynamicContext context) {
      assert context != null;

      IAssemblyDefinition definition = item.getDefinition();
      DynamicContext effectiveContext = handleLetStatements(item, definition.getLetExpressions(), context);

      validateAssembly(item, effectiveContext);
      super.visitAssembly(item, effectiveContext);
      return null;
    }

    @Override
    public Void visitMetaschema(@NonNull IModuleNodeItem item, DynamicContext context) {
      throw new UnsupportedOperationException("not needed");
    }

    @Override
    protected Void defaultResult() {
      // no result value
      return null;
    }
  }

  private static class KeyRef {
    @NonNull
    private final IIndexHasKeyConstraint constraint;
    @NonNull
    private final INodeItem node;
    @NonNull
    private final List<INodeItem> targets;

    public KeyRef(
        @NonNull IIndexHasKeyConstraint constraint,
        @NonNull INodeItem node,
        @NonNull List<INodeItem> targets) {
      this.node = node;
      this.constraint = constraint;
      this.targets = targets;
    }

    @NonNull
    public IIndexHasKeyConstraint getConstraint() {
      return constraint;
    }

    @NonNull
    protected INodeItem getNode() {
      return node;
    }

    @NonNull
    public List<INodeItem> getTargets() {
      return targets;
    }
  }
}
