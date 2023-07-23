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
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.AbstractNodeItemVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Used to perform constraint validation over one or more node items.
 * <p>
 * This class is not thread safe.
 */
public class DefaultConstraintValidator implements IConstraintValidator { // NOPMD - intentional
  private static final Logger LOGGER = LogManager.getLogger(DefaultConstraintValidator.class);

  @NonNull
  private final Map<INodeItem, ValueStatus> valueMap = new LinkedHashMap<>(); // NOPMD - intentional
  @NonNull
  private final Map<String, IIndex> indexNameToIndexMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<String, List<KeyRef>> indexNameToKeyRefMap = new ConcurrentHashMap<>();
  @NonNull
  private final DynamicContext metapathContext;
  @NonNull
  private final IConstraintValidationHandler handler;

  public DefaultConstraintValidator(
      @NonNull DynamicContext metapathContext,
      @NonNull IConstraintValidationHandler handler) {
    this.metapathContext = metapathContext;
    this.handler = handler;
  }

  @NonNull
  public IConstraintValidationHandler getConstraintValidationHandler() {
    return handler;
  }

  @NonNull
  protected DynamicContext getMetapathContext() {
    return metapathContext;
  }

  @Override
  public void validate(@NonNull INodeItem item) {
    item.accept(new Visitor(), null);
  }

  /**
   * Validate the provided flag item against any associated constraints.
   *
   * @param item
   *          the flag item to validate
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a constraint
   */
  protected void validateFlag(@NonNull IFlagNodeItem item) {
    IFlagDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  /**
   * Validate the provided field item against any associated constraints.
   *
   * @param item
   *          the field item to validate
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a constraint
   */
  protected void validateField(@NonNull IFieldNodeItem item) {
    IFieldDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  /**
   * Validate the provided assembly item against any associated constraints.
   *
   * @param item
   *          the assembly item to validate
   * @throws MetapathException
   *           if an error occurred while evaluating a Metapath used in a constraint
   */
  protected void validateAssembly(@NonNull IAssemblyNodeItem item) {
    IAssemblyDefinition definition = item.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesConstraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
    validateHasCardinality(definition.getHasCardinalityConstraints(), item);
    validateIndex(definition.getIndexConstraints(), item);
    validateUnique(definition.getUniqueConstraints(), item);
  }

  protected void validateHasCardinality(@NonNull List<? extends ICardinalityConstraint> constraints,
      @NonNull List<? extends IAssemblyNodeItem> items) {

    items.stream().forEachOrdered(item -> {
      assert item != null;
      validateHasCardinality(constraints, item);
    });
  }

  protected void validateHasCardinality(@NonNull List<? extends ICardinalityConstraint> constraints,
      @NonNull IAssemblyNodeItem item) {
    for (ICardinalityConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateHasCardinality(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateHasCardinality(@NonNull ICardinalityConstraint constraint, @NonNull IAssemblyNodeItem node,
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

  protected void validateIndex(@NonNull List<? extends IIndexConstraint> constraints,
      @NonNull List<? extends IAssemblyNodeItem> items) {
    items.stream().forEachOrdered(item -> {
      assert item != null;
      validateIndex(constraints, item);
    });
  }

  protected void validateIndex(@NonNull List<? extends IIndexConstraint> constraints,
      @NonNull IAssemblyNodeItem item) {
    for (IIndexConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateIndex(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateIndex(@NonNull IIndexConstraint constraint, @NonNull IAssemblyNodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    String indexName = constraint.getName();
    if (indexNameToIndexMap.containsKey(indexName)) {
      getConstraintValidationHandler().handleIndexDuplicateViolation(constraint, node);
      return; // NOPMD - readability
    }

    IIndex index = IIndex.newInstance(constraint.getKeyFields());
    targets.asStream()
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            try {
              INodeItem oldItem = index.put(item, metapathContext);
              if (oldItem != null) {
                getConstraintValidationHandler().handleIndexDuplicateKeyViolation(constraint, node, oldItem, item);
              }
            } catch (MetapathException ex) {
              getConstraintValidationHandler().handleKeyMatchError(constraint, node, item, ex);
            }
          }
        });
    indexNameToIndexMap.put(indexName, index);
  }

  protected void validateUnique(@NonNull List<? extends IUniqueConstraint> constraints,
      @NonNull List<? extends IAssemblyNodeItem> items) {

    items.stream().forEachOrdered(item -> {
      assert item != null;
      validateUnique(constraints, item);
    });
  }

  protected void validateUnique(@NonNull List<? extends IUniqueConstraint> constraints,
      @NonNull IAssemblyNodeItem item) {
    for (IUniqueConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateUnique(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateUnique(@NonNull IUniqueConstraint constraint,
      @NonNull IAssemblyNodeItem node, @NonNull ISequence<? extends INodeItem> targets) {
    IIndex index = IIndex.newInstance(constraint.getKeyFields());
    targets.asStream()
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            try {
              INodeItem oldItem = index.put(item, metapathContext);
              if (oldItem != null) {
                getConstraintValidationHandler().handleUniqueKeyViolation(constraint, node, oldItem, item);
              }
            } catch (MetapathException ex) {
              getConstraintValidationHandler().handleKeyMatchError(constraint, node, item, ex);
              throw ex;
            }
          }
        });
  }

  protected void validateMatches(@NonNull List<? extends IMatchesConstraint> constraints,
      @NonNull IDefinitionNodeItem item) {

    for (IMatchesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      try {
        validateMatches(constraint, item, targets);
      } catch (MetapathException ex) {
        rethrowConstraintError(constraint, item, ex);
      }
    }
  }

  protected void validateMatches(@NonNull IMatchesConstraint constraint, @NonNull INodeItem node,
      ISequence<? extends INodeItem> targets) {
    targets.asStream()
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
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
          }
        });
  }

  protected void validateIndexHasKey(
      @NonNull List<? extends IIndexHasKeyConstraint> constraints,
      @NonNull IDefinitionNodeItem node) {

    for (IIndexHasKeyConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(node, getMetapathContext());
      validateIndexHasKey(constraint, node, targets);
    }
  }

  protected void validateIndexHasKey(
      @NonNull IIndexHasKeyConstraint constraint,
      @NonNull IDefinitionNodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    String indexName = constraint.getIndexName();

    List<KeyRef> keyRefItems = indexNameToKeyRefMap.get(indexName);
    if (keyRefItems == null) {
      keyRefItems = new LinkedList<>();
      indexNameToKeyRefMap.put(indexName, keyRefItems);
    }

    KeyRef keyRef = new KeyRef(constraint, node, new ArrayList<>(targets.asList()));
    keyRefItems.add(keyRef);
  }

  protected void validateExpect(@NonNull List<? extends IExpectConstraint> constraints,
      @NonNull IDefinitionNodeItem item) {
    for (IExpectConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      validateExpect(constraint, item, targets);
    }
  }

  protected void validateExpect(@NonNull IExpectConstraint constraint, @NonNull INodeItem node,
      @NonNull ISequence<? extends INodeItem> targets) {
    targets.asStream()
        .map(item -> (INodeItem) item)
        .forEachOrdered(item -> {
          assert item != null;
          if (item.hasValue()) {
            MetapathExpression metapath = constraint.getTest();
            try {
              ISequence<?> result = metapath.evaluate(item, getMetapathContext());
              if (!FnBoolean.fnBoolean(result).toBoolean()) {
                getConstraintValidationHandler().handleExpectViolation(constraint, node, item, getMetapathContext());
              }
            } catch (MetapathException ex) {
              rethrowConstraintError(constraint, item, ex);
            }
          }
        });
  }

  protected void validateAllowedValues(@NonNull List<? extends IAllowedValuesConstraint> constraints,
      @NonNull IDefinitionNodeItem item) {
    for (IAllowedValuesConstraint constraint : constraints) {
      ISequence<? extends IDefinitionNodeItem> targets = constraint.matchTargets(item, getMetapathContext());
      validateAllowedValues(constraint, targets);
    }
  }

  protected void validateAllowedValues(@NonNull IAllowedValuesConstraint constraint,
      ISequence<? extends IDefinitionNodeItem> targets) {
    targets.asStream().forEachOrdered(item -> {
      assert item != null;
      if (item.hasValue()) {
        try {
          updateValueStatus(item, constraint);
        } catch (MetapathException ex) {
          rethrowConstraintError(constraint, item, ex);
        }
      }
    });
  }

  private static void rethrowConstraintError(@NonNull IConstraint constraint, INodeItem item,
      MetapathException ex) {
    StringBuilder builder = new StringBuilder(128);
    builder.append("A ")
        .append(constraint.getClass().getName())
        .append(" constraint");

    String id = constraint.getId();
    if (id == null) {
      builder.append(" targeting the metapath '")
          .append(constraint.getTarget().getPath())
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
   *          the set of allowed values
   */
  protected void updateValueStatus(@NonNull INodeItem targetItem, @NonNull IAllowedValuesConstraint allowedValues) {
    // constraint.getAllowedValues().containsKey(value)

    @Nullable ValueStatus valueStatus = valueMap.get(targetItem);
    if (valueStatus == null) {
      valueStatus = new ValueStatus(targetItem);
      valueMap.put(targetItem, valueStatus);
    }

    valueStatus.registerAllowedValue(allowedValues);
  }

  protected void handleAllowedValues(@NonNull INodeItem targetItem) {
    ValueStatus valueStatus = valueMap.remove(targetItem);
    if (valueStatus != null) {
      valueStatus.validate();
    }
  }

  @Override
  public void finalizeValidation() {
    // key references
    for (Map.Entry<String, List<KeyRef>> entry : indexNameToKeyRefMap.entrySet()) {
      String indexName = entry.getKey();
      IIndex index = indexNameToIndexMap.get(indexName);

      List<KeyRef> keyRefs = entry.getValue();

      for (KeyRef keyRef : keyRefs) {
        IIndexHasKeyConstraint constraint = keyRef.getConstraint();
        for (INodeItem item : keyRef.getTargets()) {
          assert item != null;

          try {
            INodeItem referencedItem = index.get(item, constraint.getKeyFields(), getMetapathContext());

            if (referencedItem == null) {
              getConstraintValidationHandler().handleIndexMiss(constraint, keyRef.getNode(), item);
            }
          } catch (MetapathException ex) {
            getConstraintValidationHandler().handleKeyMatchError(constraint, keyRef.getNode(), item, ex);
          }
        }
      }
    }
  }

  private class ValueStatus {
    @NonNull
    private final List<IAllowedValuesConstraint> constraints = new LinkedList<>();
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

    public void registerAllowedValue(@NonNull IAllowedValuesConstraint allowedValues) {
      this.constraints.add(allowedValues);
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
        for (IAllowedValuesConstraint allowedValues : constraints) {
          IAllowedValue matchingValue = allowedValues.getAllowedValue(value);
          if (matchingValue != null) {
            match = true;
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
          getConstraintValidationHandler().handleAllowedValuesViolation(failedConstraints, item);
        }
      }
    }
  }

  class Visitor
      extends AbstractNodeItemVisitor<Void, Void> {
    @Override
    public Void visitDocument(@NonNull IDocumentNodeItem item, Void context) {
      return super.visitDocument(item, context);
    }

    @Override
    public Void visitFlag(@NonNull IFlagNodeItem item, Void context) {
      validateFlag(item);
      super.visitFlag(item, context);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitField(@NonNull IFieldNodeItem item, Void context) {
      validateField(item);
      super.visitField(item, context);
      handleAllowedValues(item);
      return null;
    }

    @Override
    public Void visitAssembly(@NonNull IAssemblyNodeItem item, Void context) {
      validateAssembly(item);
      super.visitAssembly(item, context);
      return null;
    }

    @Override
    public Void visitMetaschema(@NonNull IMetaschemaNodeItem item, Void context) {
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
