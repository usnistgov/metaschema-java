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

package gov.nist.secauto.metaschema.core.configuration;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides a basic configuration management implementation that allows mutable
 * access to configuration state.
 *
 * @param <T>
 *          the type of managed features
 */
public class DefaultConfiguration<T extends IConfigurationFeature<?>>
    implements IMutableConfiguration<T> {
  @NonNull
  private Map<T, Object> featureValues;

  /**
   * Create a new configuration.
   *
   */
  public DefaultConfiguration() {
    this.featureValues = new HashMap<>();
  }

  /**
   * Create a new configuration based on the provided feature value map.
   *
   * @param featureValues
   *          the set of enabled features
   */
  public DefaultConfiguration(@NonNull Map<T, Object> featureValues) {
    this.featureValues = new HashMap<>(featureValues);
  }

  /**
   * Create a new configuration based on the provided configuration.
   *
   * @param original
   *          the original configuration
   */
  public DefaultConfiguration(@NonNull DefaultConfiguration<T> original) {
    this(original.getFeatureValues());
  }

  @Override
  public Map<T, Object> getFeatureValues() {
    return CollectionUtil.unmodifiableMap(featureValues);
  }

  private void ensureBooleanValue(@NonNull T feature) {
    Class<?> valueClass = feature.getValueClass();
    if (!Boolean.class.isAssignableFrom(valueClass)) {
      throw new UnsupportedOperationException(
          String.format("Feature value class '%s' is boolean valued.", valueClass.getName()));
    }
  }

  @Override
  public boolean isFeatureEnabled(@NonNull T feature) {
    ensureBooleanValue(feature);
    return get(feature);
  }

  @Override
  public IMutableConfiguration<T> enableFeature(@NonNull T feature) {
    ensureBooleanValue(feature);
    featureValues.put(feature, true);
    return this;
  }

  @Override
  public IMutableConfiguration<T> disableFeature(@NonNull T feature) {
    ensureBooleanValue(feature);
    featureValues.put(feature, false);
    return this;
  }

  @Override
  public IMutableConfiguration<T> applyConfiguration(@NonNull IConfiguration<T> original) {
    this.featureValues.putAll(original.getFeatureValues());
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V get(T feature) {
    V value = (V) featureValues.get(feature);
    if (value == null) {
      value = (V) feature.getDefault();
    }
    return value;
  }

  @Override
  public IMutableConfiguration<T> set(T feature, Object value) {
    Class<?> featureValueClass = feature.getValueClass();
    Class<?> valueClass = value.getClass();
    if (!featureValueClass.isAssignableFrom(valueClass)) {
      throw new UnsupportedOperationException(
          String.format("Provided value of class '%s' is not assignment compatible with feature value class '%s'.",
              valueClass.getName(),
              featureValueClass.getName()));
    }
    featureValues.put(feature, value);
    return this;
  }
}
