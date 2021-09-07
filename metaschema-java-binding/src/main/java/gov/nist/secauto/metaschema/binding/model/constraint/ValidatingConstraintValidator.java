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

package gov.nist.secauto.metaschema.binding.model.constraint;

import gov.nist.secauto.metaschema.binding.io.context.ParsingContext;
import gov.nist.secauto.metaschema.binding.metapath.Functions;
import gov.nist.secauto.metaschema.binding.metapath.type.INodeItem;
import gov.nist.secauto.metaschema.binding.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.binding.model.FieldDefinition;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.FieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.datatypes.DataTypes;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatypes.metaschema.DataTypeException;
import gov.nist.secauto.metaschema.datatypes.metaschema.IItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IMetapathResult;
import gov.nist.secauto.metaschema.datatypes.metaschema.ISequence;
import gov.nist.secauto.metaschema.datatypes.metaschema.MetapathException;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IKeyField;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.IPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.MetapathFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatingConstraintValidator implements ConstraintValidator {
  private static final Logger logger = LogManager.getLogger(ValidatingConstraintValidator.class);

  private final Map<Object, ValueStatus> valueMap = new LinkedHashMap<>();
  private final Map<String, Map<String, INodeItem>> indexToKeyToItemMap = new LinkedHashMap<>();
  private final Map<String, Map<String, List<INodeItem>>> indexToKeyRefToItemMap = new LinkedHashMap<>();

  public ValidatingConstraintValidator() {
  }

  @Override
  public void validateValue(AssemblyProperty property, Object value, ParsingContext<?, ?> context) {
    List<INodeItem> items
        = property.newNodeItems(value, context.getPathBuilder().getPathSegments()).collect(Collectors.toList());

    AssemblyDefinition definition = property.getDefinition();

    validateHasCardinality(definition.getHasCardinalityConstraints(), items);
    validateIndex(definition.getIndexConstraints(), items);
    validateUnique(definition.getUniqueConstraints(), items);
  }

  @Override
  public void validateItem(AssemblyProperty property, List<IPathSegment> pathSegments, Object itemValue,
      ParsingContext<?, ?> context) {
    INodeItem item = property.newNodeItem(itemValue, context.getPathBuilder().getLeadingPathSegments());

    AssemblyDefinition definition = property.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validateValue(FieldProperty property, Object value, ParsingContext<?, ?> context) {
    // do nothing
  }

  @Override
  public void validateItem(FieldProperty property, List<IPathSegment> pathSegments, Object itemValue,
      ParsingContext<?, ?> context) {
    INodeItem item = property.newNodeItem(itemValue, context.getPathBuilder().getLeadingPathSegments());

    FieldDefinition definition = property.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  @Override
  public void validateValue(FlagProperty property, List<IPathSegment> pathSegments, Object itemValue,
      ParsingContext<?, ?> context) {
    INodeItem item = property.newNodeItem(itemValue, context.getPathBuilder().getLeadingPathSegments());

    IFlagDefinition definition = property.getDefinition();

    validateExpect(definition.getExpectConstraints(), item);
    validateAllowedValues(definition.getAllowedValuesContraints(), item);
    validateIndexHasKey(definition.getIndexHasKeyConstraints(), item);
    validateMatches(definition.getMatchesConstraints(), item);
  }

  protected void validateHasCardinality(List<? extends ICardinalityConstraint> constraints, List<INodeItem> items) {

    items.stream().forEachOrdered(item -> {
      for (ICardinalityConstraint constraint : constraints) {
        MetapathExpression metapath = constraint.getTarget();
        IMetapathResult targets = item.evaluateMetapath(metapath);
        validateHasCardinality(constraint, item, targets.toSequence());
      }
    });
  }

  protected void validateIndex(List<? extends IIndexConstraint> constraints, List<INodeItem> items) {

    items.stream().forEachOrdered(item -> {
      for (IIndexConstraint constraint : constraints) {
        MetapathExpression metapath = constraint.getTarget();
        IMetapathResult targets = item.evaluateMetapath(metapath);
        validateIndex(constraint, item, targets.toSequence());
      }
    });
  }

  protected void validateUnique(List<? extends IUniqueConstraint> constraints, List<INodeItem> items) {

    items.stream().forEachOrdered(item -> {
      for (IUniqueConstraint constraint : constraints) {
        MetapathExpression metapath = constraint.getTarget();
        IMetapathResult targets = item.evaluateMetapath(metapath);
        validateUnique(constraint, item, targets.toSequence());
      }
    });
  }

  protected void validateMatches(List<? extends IMatchesConstraint> constraints, INodeItem item) {

    for (IMatchesConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      IMetapathResult targets = item.evaluateMetapath(metapath);
      validateMatches(constraint, item, targets.toSequence());
    }
  }

  protected void validateIndexHasKey(List<? extends IIndexHasKeyConstraint> constraints, INodeItem item) {

    for (IIndexHasKeyConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      IMetapathResult targets = item.evaluateMetapath(metapath);
      validateIndexHasKey(constraint, item, targets.toSequence());
    }
  }

  protected void validateExpect(List<? extends IExpectConstraint> constraints, INodeItem item) {
    for (IExpectConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      IMetapathResult targets = item.evaluateMetapath(metapath);
      validateExpect(constraint, item, targets.toSequence());
    }
  }

  protected void validateAllowedValues(List<? extends IAllowedValuesConstraint> constraints, INodeItem item) {
    for (IAllowedValuesConstraint constraint : constraints) {
      MetapathExpression metapath = constraint.getTarget();
      IMetapathResult targets = item.evaluateMetapath(metapath);
      validateAllowedValues(constraint, item, targets.toSequence());
    }
  }

  protected void validateHasCardinality(ICardinalityConstraint constraint, INodeItem node, ISequence targets) {
    @SuppressWarnings("unchecked")
    List<? extends INodeItem> items = (List<? extends INodeItem>) targets.asList();
    int itemCount = items.size();
    Integer minOccurs = constraint.getMinOccurs();
    if (minOccurs != null && itemCount < minOccurs) {
      logger.error(String.format("Expected minimum cardinality '%d' for path '%s', but found '%d' at path '%s'",
          minOccurs, constraint.getTarget().getPath(), itemCount, node.toPath(MetapathFormatter.instance())));
    }
    Integer maxOccurs = constraint.getMaxOccurs();
    if (maxOccurs != null && itemCount > maxOccurs) {
      logger.error(String.format("Expected maximum cardinality '%d' for path '%s', but found '%d' at path '%s'",
          maxOccurs, constraint.getTarget().getPath(), itemCount, node.toPath(MetapathFormatter.instance())));
    }
  }

  protected void validateAllowedValues(IAllowedValuesConstraint constraint, @SuppressWarnings("unused") INodeItem node, ISequence targets) {
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      String value = item.asString();
      if (!constraint.getAllowedValues().containsKey(value)) {
        if (constraint.isAllowedOther()) {
          updateValueStatus(item, false);
        } else {
          logger.error(String.format("Value '%s' did not match on of the required allowed values at path '%s'", value,
              item.toPath(MetapathFormatter.instance())));
        }
      } else {
        updateValueStatus(item, true);
      }
    });
  }

  protected void updateValueStatus(INodeItem item, boolean newStatus) {
    Object value = item.getValue();
    ValueStatus status = valueMap.get(value);

    if (status == null) {
      status = new ValueStatus(item, newStatus);
      valueMap.put(value, status);
    }

    if (!status.isValid() && newStatus) {
      status.updateStatus(newStatus);
    }
  }

  protected void validateMatches(IMatchesConstraint constraint, @SuppressWarnings("unused") INodeItem node, ISequence targets) {
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      String value = item.asString();

      Pattern pattern = constraint.getPattern();
      if (pattern != null) {
        // validate pattern
        Predicate<String> predicate = pattern.asMatchPredicate();
        if (!predicate.test(value)) {
          logger.error(String.format("Value '%s' did not match the pattern '%s' at path '%s'", value, pattern.pattern(),
              item.toPath(MetapathFormatter.instance())));
        }
      }

      DataTypes dataType = constraint.getDataType();
      if (dataType != null) {
        JavaTypeAdapter<?> adapter = dataType.getJavaTypeAdapter();
        try {
          adapter.parse(value);
        } catch (DataTypeException ex) {
          logger.error(String.format("Value '%s' did not conform to the data type '%s' at path '%s'", value,
              dataType.name(), item.toPath(MetapathFormatter.instance())), ex);
        }
      }
    });
  }

  protected void validateExpect(IExpectConstraint constraint, @SuppressWarnings("unused") INodeItem node, ISequence targets) {
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      MetapathExpression metapath = constraint.getTest();
      IMetapathResult result = item.evaluateMetapath(metapath);
      if (!Functions.fnBoolean(result).toBoolean()) {
        logger.error(String.format("Expect constraint '%s' did not match the data at path '%s'", metapath.getPath(),
            item.toPath(MetapathFormatter.instance())));
      }
    });
  }

  protected void validateUnique(IUniqueConstraint constraint, @SuppressWarnings("unused") INodeItem node, ISequence targets) {
    Map<String, INodeItem> keyToItemMap = new HashMap<>();

    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      String key = buildKey(constraint.getKeyFields(), item);

      if (keyToItemMap.containsKey(key)) {
        INodeItem oldItem = keyToItemMap.get(key);
        logger.error(String.format("Unique constraint violation at path '%s' and '%s'",
            item.toPath(MetapathFormatter.instance()), oldItem.toPath(MetapathFormatter.instance())));
      } else {
        keyToItemMap.put(key, item);
      }
    });
  }

  protected String buildKey(List<? extends IKeyField> keyFields, INodeItem item) {
    StringBuilder key = new StringBuilder();
    for (IKeyField keyField : keyFields) {
      MetapathExpression keyPath = keyField.getTarget();

      @SuppressWarnings("unchecked")
      List<? extends INodeItem> result
          = (List<? extends INodeItem>) item.evaluateMetapath(keyPath).toSequence().asList();
      if (result.size() != 1) {
        throw new MetapathException("Key resulted in multiple nodes: " + result);
      }

      INodeItem keyItem = result.iterator().next();

      String keyValue = keyItem.asString();
      Pattern pattern = keyField.getPattern();
      if (pattern != null) {
        Matcher matcher = pattern.matcher(keyValue);
        if (!matcher.matches()) {
          throw new MetapathException("Key field declares a pattern which does not match");
        }

        if (matcher.groupCount() != 1) {
          throw new MetapathException("Key field declares a pattern for which the first group was not a match");
        }
        keyValue = matcher.group(1);
      }

      if (keyValue != null) {
        key.append(keyValue);
      }
      key.append("|||");
    }
    return key.toString();
  }

  protected void validateIndex(IIndexConstraint constraint, INodeItem node, ISequence targets) {
    String indexName = constraint.getName();
    if (indexToKeyToItemMap.containsKey(indexName)) {
      throw new MetapathException(String.format("Duplicate index named '%s' found at path '%s'", indexName,
          node.toPath(MetapathFormatter.instance())));
    }

    Map<String, INodeItem> indexItems = new HashMap<>();
    targets.asStream().map(item -> (INodeItem) item).forEachOrdered(item -> {
      String key = buildKey(constraint.getKeyFields(), item);

      // logger.info("key: {} {}", key, item);
      //
      INodeItem oldItem = indexItems.put(key, item);
      if (oldItem != null) {
        throw new MetapathException(String.format("Index '%s' has duplicate key for item at path '%s'", indexName,
            item.toPath(MetapathFormatter.instance())));
      }
    });
    indexToKeyToItemMap.put(indexName, indexItems);
  }

  protected void validateIndexHasKey(IIndexHasKeyConstraint constraint, @SuppressWarnings("unused") INodeItem node, ISequence targets) {
    String indexName = constraint.getIndexName();

    Map<String, List<INodeItem>> keyRefItems = indexToKeyRefToItemMap.get(indexName);
    if (keyRefItems == null) {
      keyRefItems = new LinkedHashMap<>();
      indexToKeyRefToItemMap.put(indexName, keyRefItems);
    }

    for (IItem target : targets.asList()) {
      INodeItem item = (INodeItem) target;
      String key = buildKey(constraint.getKeyFields(), item);

      // logger.info("key-ref: {} {}", key, item);
      //
      List<INodeItem> items = keyRefItems.get(key);
      if (items == null) {
        items = new LinkedList<>();
        keyRefItems.put(key, items);
      }

      items.add(item);
    }
  }

  @Override
  public void finalizeValidation(ParsingContext<?, ?> context) {
    for (Map.Entry<Object, ValueStatus> entry : valueMap.entrySet()) {
      ValueStatus status = entry.getValue();
      if (!status.isValid()) {
        logger.warn(String.format("Value '%s' did not match one of the required allowed values at path '%s'",
            status.getValue(), status.getItem().toPath(MetapathFormatter.instance())));
      }
    }

    for (Map.Entry<String, Map<String, List<INodeItem>>> entry : indexToKeyRefToItemMap.entrySet()) {
      String indexName = entry.getKey();

      Map<String, INodeItem> indexItems = indexToKeyToItemMap.get(indexName);

      for (Map.Entry<String, List<INodeItem>> keyRefEntry : entry.getValue().entrySet()) {
        String key = keyRefEntry.getKey();
        List<INodeItem> items = keyRefEntry.getValue();

        if (!indexItems.containsKey(key)) {
          for (INodeItem item : items) {
            logger.error(String.format("Key reference not found in index '%s' for item at path '%s'", indexName,
                item.toPath(MetapathFormatter.instance())));
          }
        }
      }
    }
  }

  private static class ValueStatus {
    private final INodeItem item;
    private boolean valid;

    public ValueStatus(INodeItem item, boolean initialStatus) {
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
