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

package gov.nist.secauto.metaschema.databind.io;

import gov.nist.secauto.metaschema.core.configuration.AbstractConfigurationFeature;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class DeserializationFeature<V>
    extends AbstractConfigurationFeature<V> {
  public static final int YAML_CODEPOINT_LIMIT_DEFAULT = Integer.MAX_VALUE - 1; // 2 GB
  public static final int FORMAT_DETECTION_LOOKAHEAD = 32_768; // 2 GB

  /**
   * If enabled, perform constraint validation on the deserialized bound objects.
   */
  @NonNull
  public static final DeserializationFeature<Boolean> DESERIALIZE_VALIDATE_CONSTRAINTS
      = new DeserializationFeature<>(Boolean.class, true);

  /**
   * If enabled, process the next JSON node as a field, whose name must match the
   * {@link IAssemblyDefinition#getRootJsonName()}. If not enabled, the next JSON
   * node is expected to be an object containing the data of the
   * {@link IAssemblyDefinition}.
   */
  public static final DeserializationFeature<Boolean> DESERIALIZE_JSON_ROOT_PROPERTY
      = new DeserializationFeature<>(Boolean.class, true);

  /**
   * Determines the max YAML codepoints that can be read.
   */
  @NonNull
  public static final DeserializationFeature<Integer> YAML_CODEPOINT_LIMIT
      = new DeserializationFeature<>(Integer.class, YAML_CODEPOINT_LIMIT_DEFAULT);

  /**
   * Determines how many bytes can be looked at to identify the format of a
   * document.
   */
  @NonNull
  public static final DeserializationFeature<Integer> FORMAT_DETECTION_LOOKAHEAD_LIMIT
      = new DeserializationFeature<>(Integer.class, FORMAT_DETECTION_LOOKAHEAD);

  private DeserializationFeature(
      @NonNull Class<V> valueClass,
      @NonNull V defaultValue) {
    super(valueClass, defaultValue);
  }
}
