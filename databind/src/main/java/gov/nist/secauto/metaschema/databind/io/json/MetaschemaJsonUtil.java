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

package gov.nist.secauto.metaschema.databind.io.json;

import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class MetaschemaJsonUtil {

  private MetaschemaJsonUtil() {
    // disable construction
  }

  /**
   * Generates a mapping of property names to associated Module instances.
   * <p>
   * If {@code requiresJsonKey} is {@code true} then the instance used as the JSON
   * key is not included in the mapping.
   * <p>
   * If the {@code targetDefinition} is an instance of {@link IFieldDefinition}
   * and a JSON value key property is configured, then the value key flag and
   * value are also omitted from the mapping. Otherwise, the value is included in
   * the mapping.
   *
   * @param targetDefinition
   *          the definition to get JSON instances from
   * @param jsonKey
   *          the flag instance used as the JSON key, or {@code null} otherwise
   * @return a mapping of JSON property to related Module instance
   */
  @NonNull
  public static Map<String, ? extends IBoundProperty> getJsonInstanceMap(
      @NonNull IBoundDefinitionModel targetDefinition,
      @Nullable IBoundInstanceFlag jsonKey) {
    Collection<? extends IBoundInstanceFlag> flags = targetDefinition.getFlagInstances();
    int flagCount = flags.size() - (jsonKey == null ? 0 : 1);

    Stream<? extends IBoundProperty> instanceStream;
    if (targetDefinition instanceof IBoundDefinitionModelAssembly) {
      // use all child instances
      instanceStream = ((IBoundDefinitionModelAssembly) targetDefinition).getModelInstances().stream()
          .map(instance -> (IBoundProperty) instance);
    } else if (targetDefinition instanceof IBoundDefinitionModelFieldComplex) {
      IBoundDefinitionModelFieldComplex targetFieldDefinition = (IBoundDefinitionModelFieldComplex) targetDefinition;

      IBoundInstanceFlag jsonValueKeyFlag = targetFieldDefinition.getJsonValueKeyFlagInstance();
      if (jsonValueKeyFlag == null && flagCount > 0) {
        // the field value is handled as named field
        IBoundFieldValue fieldValue = targetFieldDefinition.getFieldValue();
        instanceStream = Stream.of(fieldValue);
      } else {
        // only the value, with no flags or a JSON value key flag
        instanceStream = Stream.empty();
      }
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
    }

    if (jsonKey != null) {
      instanceStream = Stream.concat(
          flags.stream().filter(flag -> !jsonKey.equals(flag)),
          instanceStream);
    } else {
      instanceStream = Stream.concat(
          flags.stream(),
          instanceStream);
    }
    return CollectionUtil.unmodifiableMap(ObjectUtils.notNull(instanceStream.collect(
        Collectors.toMap(
            IBoundProperty::getJsonName,
            Function.identity(),
            (v1, v2) -> v2,
            LinkedHashMap::new))));
  }
}
