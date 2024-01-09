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

package gov.nist.secauto.metaschema.schemagen.json.impl.builder;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.json.IJsonGenerationState;

import java.util.List;

public class SingletonOrListBuilder implements IModelInstanceBuilder<SingletonOrListBuilder> {
  private int minOccurrence = MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;
  private final ArrayBuilder arrayBuilder;
  private final SingletonBuilder singletonBuilder;

  public SingletonOrListBuilder() {
    this.arrayBuilder = new ArrayBuilder();
    // the array must minimally have 2 items or else the singleton will be used
    this.arrayBuilder.minItems(2);
    this.singletonBuilder = new SingletonBuilder();
  }

  @Override
  public List<IType> getTypes() {
    return arrayBuilder.getTypes();
  }

  @Override
  public SingletonOrListBuilder addItemType(INamedModelInstanceGrouped itemType) {
    arrayBuilder.addItemType(itemType);
    singletonBuilder.addItemType(itemType);
    return this;
  }

  @Override
  public SingletonOrListBuilder addItemType(INamedModelInstanceAbsolute itemType) {
    arrayBuilder.addItemType(itemType);
    singletonBuilder.addItemType(itemType);
    return this;
  }

  @Override
  public void build(
      ObjectNode object,
      IJsonGenerationState state) {
    ArrayNode oneOf = object.putArray("oneOf");
    singletonBuilder.build(ObjectUtils.notNull(oneOf.addObject()), state);
    arrayBuilder.build(ObjectUtils.notNull(oneOf.addObject()), state);
  }

  @Override
  public SingletonOrListBuilder minItems(int min) {
    this.minOccurrence = min;
    arrayBuilder.minItems(Integer.max(2, min));
    if (min > 0) {
      singletonBuilder.minItems(1);
    }
    return this;
  }

  @Override
  public SingletonOrListBuilder maxItems(int max) {
    arrayBuilder.maxItems(max);
    return this;
  }

  @Override
  public int getMinOccurrence() {
    return minOccurrence;
  }

  @Override
  public int getMaxOccurrence() {
    return arrayBuilder.getMaxOccurrence();
  }

}
