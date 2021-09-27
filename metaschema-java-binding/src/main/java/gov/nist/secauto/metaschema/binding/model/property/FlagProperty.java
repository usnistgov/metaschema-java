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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.FlagDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.format.IFlagPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FlagProperty extends NamedProperty, IFlagInstance {

  @Override
  FlagDefinition getDefinition();

  /**
   * Retrieve the flag item for this property, who's value is retrieved from the parent node item.
   * 
   * @param parentItem
   *          the parent node item on which this flag property may exist
   * @return a stream containing the flag item or an empty stream if the property doesn't exist
   */
  Stream<IFlagNodeItem> getNodeItemFromParentInstance(IModelNodeItem parentItem);

  /**
   * Retrieve the flag item for the provided property value as a stream.
   * 
   * @param parentItem
   *          the parent node item on which this property's value exists
   * @param value
   *          the property's value
   * @return a stream containing the flag item
   */
  Stream<IFlagNodeItem> getNodeItemFromValue(IModelNodeItem parentItem, Object value);

  /**
   * Create a new node item for a specific instance of this property. The value of {@code value} is
   * expected to be a singleton instance. The {@code precedingPath} argument must not include the
   * current node, as this will result in duplication of this node in the path.
   * 
   * @param parent
   *          the parent item the value exists on
   * @param value
   *          the instance
   * @return the new node item
   */
  default IFlagNodeItem newNodeItem(IModelNodeItem parent, Object value) {
    IFlagPathSegment segment = newPathSegment(parent.getPathSegment());
    return newNodeItem(segment, value, parent);
  }

  IFlagNodeItem newNodeItem(IFlagPathSegment pathSegment, Object itemValue, IModelNodeItem parent);

  Object readValueFromString(String value) throws IOException;

  Supplier<?> readValueAndSupply(String value) throws IOException;

  Supplier<?> readValueAndSupply(JsonParsingContext context) throws IOException;

  String getValueAsString(Object value) throws IOException;

  void writeValue(Object value, JsonWritingContext context) throws IOException;
}
