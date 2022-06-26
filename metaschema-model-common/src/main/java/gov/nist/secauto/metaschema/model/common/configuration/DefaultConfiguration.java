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

package gov.nist.secauto.metaschema.model.common.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provides immutable access to configuration state.
 * 
 * @param <T>
 *          the type of managed features
 */
public class DefaultConfiguration<T extends Enum<T> & IConfigurationFeature>
    implements IMutableConfiguration<T> {
  @NotNull
  private Set<@NotNull T> featureSet;

  /**
   * Create a new configuration based on the provided feature enumeration.
   * 
   * @param enumClass
   *          the feature enumeration class
   */
  @SuppressWarnings("null")
  public DefaultConfiguration(@NotNull Class<T> enumClass) {
    this.featureSet = EnumSet.noneOf(enumClass);

    for (T feature : enumClass.getEnumConstants()) {
      if (feature.isEnabledByDefault()) {
        // enable default features
        this.featureSet.add(feature);
      }
    }
  }

  /**
   * Create a new configuration based on the provided feature enumeration.
   * 
   * @param featureSet
   *          the set of enabled features
   */
  public DefaultConfiguration(@NotNull Set<@NotNull T> featureSet) {
    this.featureSet = featureSet;
  }

  /**
   * Create a new configuration based on the provided configuration.
   * 
   * @param original
   *          the original configuration
   */
  @SuppressWarnings("null")
  public DefaultConfiguration(@NotNull DefaultConfiguration<T> original) {
    this.featureSet = EnumSet.copyOf(original.featureSet);
  }

  @Override
  public Set<@NotNull T> getFeatureSet() {
    return featureSet;
  }

  @Override
  public boolean isFeatureEnabled(@NotNull T feature) {
    return featureSet.contains(feature);
  }

  @Override
  public IMutableConfiguration<T> enableFeature(@NotNull T feature) {
    getFeatureSet().add(feature);
    return this;
  }

  @Override
  public IMutableConfiguration<T> disableFeature(@NotNull T feature) {
    getFeatureSet().remove(feature);
    return this;
  }
  
  @Override
  @SuppressWarnings("null")
  public IMutableConfiguration<T> applyConfiguration(@NotNull IConfiguration<T> original) {
    this.featureSet = EnumSet.copyOf(original.getFeatureSet());
    return this;
  }
}
