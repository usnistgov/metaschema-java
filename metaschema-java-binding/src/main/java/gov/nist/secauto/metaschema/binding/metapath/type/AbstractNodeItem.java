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

package gov.nist.secauto.metaschema.binding.metapath.type;

import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;
import gov.nist.secauto.metaschema.datatypes.DataTypes;
import gov.nist.secauto.metaschema.datatypes.metaschema.IAtomicItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IStringItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.InvalidTypeException;
import gov.nist.secauto.metaschema.datatypes.metaschema.StringItem;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.FlagPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.IPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.MetapathFormatter;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.context.ModelPositionalPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.type.AbstractPathItem;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractNodeItem extends AbstractPathItem implements INodeItem {
  /**
   * The current node.
   */
  private final Object value;

  /**
   * Used to cache this object as a string.
   */
  private IStringItem stringItem;
  /**
   * Used to cache this object as an atomic item.
   */
  private IAtomicItem atomicItem;

  public AbstractNodeItem(Object value, IPathSegment segment) {
    super(segment);
    this.value = value;
  }

  @Override
  public INodeItem getNodeItem() {
    return this;
  }

  @Override
  public Object getValue() {
    return value;
  }

  protected synchronized void initAtomicItem() {
    if (atomicItem == null) {
      IDefinition definition = getDefinition();

      if (definition instanceof IValuedDefinition) {
        DataTypes dataType = ((IValuedDefinition) definition).getDatatype();
        atomicItem = dataType.getAtomicItem(getValue());
      } else {
        throw new InvalidTypeException(String.format("the node with path '%s' is not a value typed node",
            MetapathFormatter.instance().format(getPath())));
      }
    }
  }

  @Override
  public IAtomicItem toAtomicItem() {
    initAtomicItem();
    return atomicItem;
  }

  protected synchronized void initStringItem() {
    if (stringItem == null) {
      IDefinition definition = getDefinition();
      if (definition instanceof IValuedDefinition) {
        String string = ((IValuedDefinition) definition).getDatatype().getJavaTypeAdapter().asString(value);
        stringItem = new StringItem(string);
      } else {
        throw new UnsupportedOperationException();
      }
      stringItem = new StringItem(asString());
    }
  }

  @Override
  public IStringItem toStringItem() {
    IStringItem retval;
    if (value != null) {
      initStringItem();
      retval = stringItem;
    } else {
      retval = null;
    }
    return retval;
  }

  @Override
  public INodeItem newChildNodeItem(FlagProperty instance, Object item) {
    return new IntermediateNodeItem(item, new FlagPathSegment(instance), this);
  }

  @Override
  public Stream<INodeItem> newChildNodeItems(NamedModelProperty instance, Stream<?> items) {
    AtomicInteger index = new AtomicInteger();
    return items.map(x -> {
      return new IntermediateNodeItem(x, new ModelPositionalPathSegment(instance, index.incrementAndGet()), this);
    });
  }

  @Override
  public String toString() {
    return super.toString() + " " + getValue();
  }

  @Override
  public Stream<INodeItem> getChildFlags(Flag flag) {
    Stream<INodeItem> retval = Stream.empty();

    Object value = getValue();
    IDefinition definition = getPathSegment().getDefinition();
    if (definition instanceof ClassBinding) {
      ClassBinding classBinding = (ClassBinding) definition;
      if (flag.isName()) {
        String name = ((Name) flag.getNode()).getValue();
        FlagProperty instance = classBinding.getFlagInstanceByName(name);
        if (instance != null) {
          retval = Stream.of(newChildNodeItem(instance, instance.getValue(value)));
        }
      } else {
        Predicate<IInstance> instanceMatcher = flag.getInstanceMatcher();
        for (FlagProperty instance : classBinding.getFlagInstances().values()) {
          if (instanceMatcher.test(instance)) {
            retval = Stream.concat(retval, Stream.of(newChildNodeItem(instance, instance.getValue(value))));
          }
        }
      }
    }
    return retval;
  }

  @Override
  public Stream<INodeItem> getChildModelInstances(ModelInstance modelInstance) {

    Stream<INodeItem> retval = Stream.empty();
    IDefinition definition = getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding classBinding = (AssemblyClassBinding) definition;

      Object value = getValue();
      if (modelInstance.isName()) {
        String name = ((Name) modelInstance.getNode()).getValue();
        NamedModelProperty instance = classBinding.getModelInstanceByName(name);
        if (instance != null) {
          retval = newChildNodeItems(instance, instance.getItemsFromParentInstance(value));
        }
      } else {
        Predicate<IInstance> instanceMatcher = modelInstance.getInstanceMatcher();
        for (NamedModelProperty instance : classBinding.getModelInstances()) {
          if (instanceMatcher.test(instance)) {
            retval = Stream.concat(retval, newChildNodeItems(instance, instance.getItemsFromParentInstance(value)));
          }
        }
      }
    }
    return retval;
  }
}
